package com.mobascension;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Rolling ranks and applying stats, effects, gear, name and abilities to a mob. */
public final class ChampionFactory {
    public static final String GEAR_TAG = "mobascension_gear";
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private ChampionFactory() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobAscension.MODID, path);
    }

    // ------------------------------------------------------------------ eligibility / rolling

    public static boolean isEligible(Mob mob) {
        if (!AscensionConfig.ENABLED.get()) return false;
        // Hard guarantee: these three are never affected, whatever the config says.
        if (mob instanceof Warden || mob instanceof EnderDragon || mob instanceof WitherBoss) return false;
        if (ChampionData.isMinion(mob)) return false;

        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        String keyStr = key.toString();
        if (AscensionConfig.BLACKLIST.get().contains(keyStr)) return false;
        List<? extends String> whitelist = AscensionConfig.WHITELIST.get();
        if (!whitelist.isEmpty() && !whitelist.contains(keyStr)) return false;
        if (AscensionConfig.VANILLA_ONLY.get() && !key.getNamespace().equals("minecraft")) return false;
        if (!AscensionConfig.INCLUDE_PASSIVE.get() && !(mob instanceof Enemy)) return false;
        return true;
    }

    /** Returns a rank, or null for a normal mob. */
    public static Rank roll(RandomSource random) {
        double total = AscensionConfig.COMMON_WEIGHT.get();
        for (Rank r : Rank.values()) total += AscensionConfig.rank(r).weight.get();
        if (total <= 0) return null;
        double pick = random.nextDouble() * total;
        if (pick < AscensionConfig.COMMON_WEIGHT.get()) return null;
        pick -= AscensionConfig.COMMON_WEIGHT.get();
        for (Rank r : Rank.values()) {
            pick -= AscensionConfig.rank(r).weight.get();
            if (pick < 0) return r;
        }
        return null;
    }

    /** Scale from the rank's MAX level (config), used as a fallback when a mob has no rolled level. */
    public static double statScale(Rank rank) {
        return AscensionConfig.rank(rank).statLevel.get() / 5.0;
    }

    /** Scale from this champion's own randomly rolled level (1..rank max). Level 5 = 1.0x. */
    public static double statScale(Mob mob, Rank rank) {
        int level = ChampionData.getLevel(mob);
        if (level <= 0) return statScale(rank);
        return level / 5.0;
    }

    public static double damageMultiplier(Mob mob, Rank rank) {
        return 1.0 + AscensionConfig.rank(rank).damage.get() * statScale(mob, rank);
    }

    // ------------------------------------------------------------------ apply

    public static void apply(Mob mob, ServerLevel level, Rank rank) {
        AscensionConfig.RankSettings cfg = AscensionConfig.rank(rank);
        RandomSource random = mob.getRandom();

        // every champion rolls its own stat level between the rank's min and max
        // (Veteran 3-5, Elite 14-20, Mythic 7-13, Apex 25-30)
        int maxLevel = Math.max(1, cfg.statLevel.get());
        int minLevel = Math.max(1, Math.min(cfg.statLevelMin.get(), maxLevel));
        int statLevel = minLevel + random.nextInt(maxLevel - minLevel + 1);
        ChampionData.setLevel(mob, statLevel);
        double s = statLevel / 5.0;

        // abilities first (Aegis needs an attribute)
        List<Ability> abilities = rollAbilities(mob, rank, cfg);
        ChampionData.setAbilities(mob, abilities);

        // stats
        mod(mob, Attributes.MAX_HEALTH, "health", cfg.health.get() * s, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mod(mob, Attributes.ARMOR, "armor", cfg.armor.get() * s, AttributeModifier.Operation.ADD_VALUE);
        mod(mob, Attributes.ARMOR_TOUGHNESS, "toughness", cfg.toughness.get() * s, AttributeModifier.Operation.ADD_VALUE);
        double speedBonus = Math.min(1.0, cfg.speed.get() * s); // capped at +100% so high levels stay fair
        mod(mob, Attributes.MOVEMENT_SPEED, "speed", speedBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mod(mob, Attributes.FLYING_SPEED, "flying_speed", speedBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mod(mob, Attributes.ATTACK_DAMAGE, "damage", cfg.damage.get() * s, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mod(mob, Attributes.KNOCKBACK_RESISTANCE, "knockback", Math.min(1.0, cfg.knockback.get() * s), AttributeModifier.Operation.ADD_VALUE);
        mod(mob, Attributes.SAFE_FALL_DISTANCE, "fall", cfg.fall.get() * s, AttributeModifier.Operation.ADD_VALUE);
        mod(mob, Attributes.SCALE, "size", cfg.size.get() * s, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mod(mob, Attributes.FOLLOW_RANGE, "follow", 4.0 * (rank.ordinal() + 1), AttributeModifier.Operation.ADD_VALUE);
        if (abilities.contains(Ability.AEGIS)) {
            double shield = AscensionConfig.ability(Ability.AEGIS).power.get();
            mod(mob, Attributes.MAX_ABSORPTION, "aegis", shield, AttributeModifier.Operation.ADD_VALUE);
            mob.setAbsorptionAmount((float) shield);
        }
        mob.setHealth(mob.getMaxHealth());

        // effects
        if (cfg.fireResistant.get()) infinite(mob, MobEffects.FIRE_RESISTANCE);
        if (random.nextDouble() < cfg.weaving.get()) infinite(mob, MobEffects.WEAVING);
        if (random.nextDouble() < cfg.infested.get()) infinite(mob, MobEffects.INFESTED);
        if (random.nextDouble() < cfg.oozing.get()) infinite(mob, MobEffects.OOZING);
        if (rank == Rank.APEX) {
            infinite(mob, MobEffects.DAMAGE_BOOST, 1);        // Strength II
            infinite(mob, MobEffects.DAMAGE_RESISTANCE, 0);   // Resistance I
            // plus two random harmless-to-it buffs; never regeneration, poison, wither, instant health/damage etc.
            List<Holder<net.minecraft.world.effect.MobEffect>> extras = new ArrayList<>(List.of(
                    MobEffects.MOVEMENT_SPEED, MobEffects.JUMP, MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING,
                    MobEffects.SLOW_FALLING, MobEffects.DIG_SPEED, MobEffects.ABSORPTION));
            Collections.shuffle(extras, new Random(random.nextLong()));
            int added = 0;
            for (Holder<net.minecraft.world.effect.MobEffect> e : extras) {
                if (added >= 2) break;
                if (mob.hasEffect(e)) continue;
                infinite(mob, e, 0);
                added++;
            }
        }
        int glow = cfg.glowSeconds.get();
        if (glow != 0) {
            int ticks = glow < 0 ? MobEffectInstance.INFINITE_DURATION : glow * 20;
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, true, true));
        }

        if (rank == Rank.APEX && (!(mob instanceof Enemy) || random.nextDouble() < cfg.rangedChance.get())) {
            // only Apex ever shoots; passive Apex mobs always do (it is their only attack)
            mob.getPersistentData().putBoolean(ChampionData.RANGED, true);
        }

        equip(mob, level, rank, cfg);

        // glow outline color (no name tag, so nothing is visible through walls except the outline while glowing)
        if (AscensionConfig.GLOW_COLORS.get()) applyTeam(mob, level, rank);

        // announcement
        if (cfg.announce.get()) {
            double r = AscensionConfig.ANNOUNCE_RADIUS.get();
            StringBuilder ab = new StringBuilder();
            for (Ability a : abilities) {
                if (ab.length() > 0) ab.append(", ");
                ab.append(a.displayName);
            }
            String rankName = cfg.displayName.get();
            String article = "AEIOUaeiou".indexOf(rankName.isEmpty() ? 'x' : rankName.charAt(0)) >= 0 ? "An " : "A ";
            Component msg = Component.literal(article + rankName + " ")
                    .append(mob.getType().getDescription().copy())
                    .append(Component.literal(" has ascended nearby!"))
                    .append(ab.length() > 0 ? Component.literal(" [" + ab + "]") : CommonComponents.EMPTY)
                    .withStyle(rank.format);
            for (ServerPlayer p : level.players()) {
                if (p.distanceToSqr(mob) <= r * r) p.sendSystemMessage(msg);
            }
        }
    }

    private static List<Ability> rollAbilities(Mob mob, Rank rank, AscensionConfig.RankSettings cfg) {
        List<Ability> pool = new ArrayList<>();
        for (Ability a : Ability.values()) {
            AscensionConfig.AbilitySettings as = AscensionConfig.ability(a);
            if (as.enabled.get() && as.minRank.get() <= rank.ordinal() + 1) pool.add(a);
        }
        Collections.shuffle(pool, new Random(mob.getRandom().nextLong()));
        int n = Math.min(cfg.abilityCount.get(), pool.size());
        return new ArrayList<>(pool.subList(0, n));
    }

    private static void infinite(Mob mob, Holder<net.minecraft.world.effect.MobEffect> effect) {
        infinite(mob, effect, 0);
    }

    private static void infinite(Mob mob, Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        // ambient=false, particles visible=true, HUD/status icon showIcon=true -> effect is never hidden
        mob.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, amplifier, false, true, true));
    }

    private static void mod(Mob mob, Holder<Attribute> attr, String name, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = mob.getAttribute(attr);
        if (inst == null || amount == 0) return;
        ResourceLocation id = id(name);
        inst.removeModifier(id);
        inst.addPermanentModifier(new AttributeModifier(id, amount, op));
    }

    private static void applyTeam(Mob mob, ServerLevel level, Rank rank) {
        if (mob.getTeam() != null) return;
        Scoreboard sb = level.getScoreboard();
        String teamName = "ma_" + rank.id;
        PlayerTeam team = sb.getPlayerTeam(teamName);
        if (team == null) {
            team = sb.addPlayerTeam(teamName);
        }
        team.setColor(rank.format);
        sb.addPlayerToTeam(mob.getScoreboardName(), team);
    }

    // ------------------------------------------------------------------ gear

    private static void equip(Mob mob, ServerLevel level, Rank rank, AscensionConfig.RankSettings cfg) {
        if (!(mob instanceof Enemy)) return;
        RandomSource random = mob.getRandom();
        boolean replace = AscensionConfig.REPLACE_VANILLA_GEAR.get();

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (random.nextDouble() >= cfg.gearChance.get()) continue;
            if (!mob.getItemBySlot(slot).isEmpty() && !replace) continue;
            giveGear(mob, level, rank, cfg, slot, random);
        }

        if (!(mob instanceof RangedAttackMob) && random.nextDouble() < cfg.gearChance.get()) {
            ItemStack current = mob.getMainHandItem();
            boolean ranged = current.getItem() instanceof ProjectileWeaponItem || current.getItem() instanceof TridentItem;
            if (!ranged && (current.isEmpty() || replace)) {
                giveGear(mob, level, rank, cfg, EquipmentSlot.MAINHAND, random);
            }
        }
    }

    private static void giveGear(Mob mob, ServerLevel level, Rank rank, AscensionConfig.RankSettings cfg,
                                 EquipmentSlot slot, RandomSource random) {
        ItemStack stack = makeGear(level, rank, slot, random);
        if (stack == null) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(GEAR_TAG, true));
        mob.setItemSlot(slot, stack);
        mob.setDropChance(slot, 0.0F); // drops are handled by Mob Ascension (see ChampionEvents)
    }

    /** Builds one piece of champion gear: base item from config, vanilla enchants, vanilla trim. Null if none configured. */
    public static ItemStack makeGear(ServerLevel level, Rank rank, EquipmentSlot slot, RandomSource random) {
        AscensionConfig.RankSettings cfg = AscensionConfig.rank(rank);
        ItemStack stack = pickItem(cfg, slot, random);
        if (stack == null) return null;
        if (slot != EquipmentSlot.MAINHAND && random.nextDouble() < cfg.trim.get()) {
            applyTrim(stack, level, random);
        }
        if (random.nextDouble() < cfg.enchantChance.get()) {
            GearEnchanter.enchant(stack, rank, level.registryAccess(), random);
        }
        return stack;
    }

    /** Random trim, restricted to vanilla (minecraft:) patterns and materials. */
    private static void applyTrim(ItemStack stack, ServerLevel level, RandomSource random) {
        var patterns = level.registryAccess().registryOrThrow(Registries.TRIM_PATTERN).holders()
                .filter(h -> h.key().location().getNamespace().equals("minecraft")).toList();
        var materials = level.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL).holders()
                .filter(h -> h.key().location().getNamespace().equals("minecraft")).toList();
        if (patterns.isEmpty() || materials.isEmpty()) return;
        stack.set(DataComponents.TRIM, new ArmorTrim(
                materials.get(random.nextInt(materials.size())),
                patterns.get(random.nextInt(patterns.size()))));
    }

    /** Picks a random configured base item for the slot, or null if none is valid. */
    public static ItemStack pickItem(AscensionConfig.RankSettings cfg, EquipmentSlot slot, RandomSource random) {
        List<? extends String> list = cfg.gear.get(slot).get();
        if (list.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(list.get(random.nextInt(list.size())));
        if (id == null) return null;
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        return item == Items.AIR ? null : new ItemStack(item);
    }
}
