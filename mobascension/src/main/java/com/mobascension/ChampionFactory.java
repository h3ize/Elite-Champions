package com.mobascension;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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

    public static double statScale(Rank rank) {
        return AscensionConfig.rank(rank).statLevel.get() / 5.0;
    }

    public static double damageMultiplier(Rank rank) {
        return 1.0 + AscensionConfig.rank(rank).damage.get() * statScale(rank);
    }

    // ------------------------------------------------------------------ apply

    public static void apply(Mob mob, ServerLevel level, Rank rank) {
        AscensionConfig.RankSettings cfg = AscensionConfig.rank(rank);
        double s = statScale(rank);
        RandomSource random = mob.getRandom();

        // abilities first (Aegis needs an attribute)
        List<Ability> abilities = rollAbilities(mob, rank, cfg);
        ChampionData.setAbilities(mob, abilities);

        // stats
        mod(mob, Attributes.MAX_HEALTH, "health", cfg.health.get() * s, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mod(mob, Attributes.ARMOR, "armor", cfg.armor.get() * s, AttributeModifier.Operation.ADD_VALUE);
        mod(mob, Attributes.ARMOR_TOUGHNESS, "toughness", cfg.toughness.get() * s, AttributeModifier.Operation.ADD_VALUE);
        mod(mob, Attributes.MOVEMENT_SPEED, "speed", cfg.speed.get() * s, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mod(mob, Attributes.FLYING_SPEED, "flying_speed", cfg.speed.get() * s, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
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
        if (random.nextDouble() < cfg.wind.get()) infinite(mob, MobEffects.WIND_CHARGED);
        if (random.nextDouble() < cfg.weaving.get()) infinite(mob, MobEffects.WEAVING);
        if (random.nextDouble() < cfg.infested.get()) infinite(mob, MobEffects.INFESTED);
        if (random.nextDouble() < cfg.oozing.get()) infinite(mob, MobEffects.OOZING);
        if (cfg.glowSeconds.get() > 0) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, cfg.glowSeconds.get() * 20, 0, false, false));
        }

        equip(mob, level, rank, cfg);

        // name + outline color
        if (AscensionConfig.SHOW_NAMES.get() && !mob.hasCustomName()) {
            Component name = Component.literal("[" + cfg.displayName.get() + "] ")
                    .append(mob.getType().getDescription().copy())
                    .withStyle(rank.format);
            mob.setCustomName(name);
            mob.setCustomNameVisible(true);
        }
        if (AscensionConfig.GLOW_COLORS.get()) applyTeam(mob, level, rank);

        // announcement
        if (cfg.announce.get()) {
            double r = AscensionConfig.ANNOUNCE_RADIUS.get();
            StringBuilder ab = new StringBuilder();
            for (Ability a : abilities) {
                if (ab.length() > 0) ab.append(", ");
                ab.append(a.displayName);
            }
            Component msg = Component.literal("A " + cfg.displayName.get() + " ")
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
        mob.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, 0, false, false));
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
        ItemStack stack = pickItem(cfg, slot, random);
        if (stack == null) return;
        if (random.nextDouble() < cfg.relicEquip.get()) {
            stack = RelicFactory.create(stack, rank, slot, level.registryAccess(), random);
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(GEAR_TAG, true));
        mob.setItemSlot(slot, stack);
        mob.setDropChance(slot, 0.0F); // drops are handled by Mob Ascension (see ChampionEvents)
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
