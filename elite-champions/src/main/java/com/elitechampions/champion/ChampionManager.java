package com.elitechampions.champion;

import com.elitechampions.EliteChampions;
import com.elitechampions.ModItems;
import com.elitechampions.config.ChampionConfig;
import com.elitechampions.config.ChampionConfig.RankSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class ChampionManager {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private ChampionManager() {}

    // ------------------------------------------------------------------ assignment

    /** Called from FinalizeSpawnEvent. Only decides the rank; everything is applied on the first tick. */
    public static void tryAssign(Mob mob, MobSpawnType spawnType) {
        if (!ChampionConfig.ENABLED.get() || ChampionData.isChampion(mob)) return;
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (!ChampionConfig.WHITELIST.get().contains(key.toString())) return;
        if (ChampionConfig.isSpawnTypeExcluded(spawnType)) return;
        ChampionRank rank = roll(mob.getRandom());
        if (rank != ChampionRank.COMMON) ChampionData.init(mob, rank);
    }

    public static ChampionRank roll(RandomSource rand) {
        double common = ChampionConfig.COMMON_WEIGHT.get();
        double total = common;
        for (ChampionRank r : ChampionRank.champions()) total += ChampionConfig.RANKS.get(r).weight.get();
        if (total <= 0) return ChampionRank.COMMON;
        double roll = rand.nextDouble() * total;
        if (roll < common) return ChampionRank.COMMON;
        roll -= common;
        for (ChampionRank r : ChampionRank.champions()) {
            roll -= ChampionConfig.RANKS.get(r).weight.get();
            if (roll < 0) return r;
        }
        return ChampionRank.COMMON;
    }

    // ------------------------------------------------------------------ application

    private static void apply(Mob mob, ServerLevel level) {
        ChampionRank rank = ChampionData.getRank(mob);
        RankSettings s = ChampionConfig.RANKS.get(rank);
        if (s == null) {
            ChampionData.finish(mob, List.of());
            return;
        }
        RandomSource rand = mob.getRandom();

        List<ChampionAbility> pool = new ArrayList<>();
        for (ChampionAbility a : ChampionAbility.values()) {
            if (a.appliesTo(mob) && !ChampionConfig.DISABLED_ABILITIES.get().contains(a.id())) pool.add(a);
        }
        Collections.shuffle(pool, new java.util.Random(rand.nextLong()));
        List<ChampionAbility> chosen = new ArrayList<>(pool.subList(0, Math.min(s.abilityCount.get(), pool.size())));
        ChampionData.finish(mob, chosen);

        applyStats(mob, s);
        equip(mob, level, rank, s, rand);
        for (ChampionAbility a : chosen) onApply(mob, level, rank, a);
        applyEffects(mob, rank, s, rand);

        mob.setHealth(mob.getMaxHealth());

        if (ChampionConfig.SHOW_NAMES.get()) {
            mob.setCustomName(Component.translatable("elitechampions.name",
                    Component.translatable(rank.translationKey()), mob.getType().getDescription()).withStyle(rank.color()));
            mob.setCustomNameVisible(true);
        }

        if (ChampionConfig.ANNOUNCE.get() && rank.ordinal() >= ChampionConfig.ANNOUNCE_MIN_RANK.get().ordinal()) {
            double r = ChampionConfig.ANNOUNCE_RADIUS.get();
            Component msg = Component.translatable("elitechampions.message.spawn",
                    Component.translatable("elitechampions.name", Component.translatable(rank.translationKey()), mob.getType().getDescription()))
                    .withStyle(rank.color());
            for (ServerPlayer p : level.players()) {
                if (p.distanceToSqr(mob) <= r * r) p.sendSystemMessage(msg);
            }
        }
    }

    private static void applyStats(Mob mob, RankSettings s) {
        addModifier(mob, Attributes.MAX_HEALTH, "rank_health", s.healthBonus(), Operation.ADD_MULTIPLIED_BASE);
        addModifier(mob, Attributes.ARMOR, "rank_armor", s.armorBonus(), Operation.ADD_VALUE);
        addModifier(mob, Attributes.MOVEMENT_SPEED, "rank_speed", s.speedBonus(), Operation.ADD_MULTIPLIED_BASE);
        addModifier(mob, Attributes.ATTACK_DAMAGE, "rank_damage", s.damageBonus(), Operation.ADD_MULTIPLIED_BASE);
        addModifier(mob, Attributes.KNOCKBACK_RESISTANCE, "rank_knockback", s.knockbackBonus(), Operation.ADD_VALUE);
        addModifier(mob, Attributes.FALL_DAMAGE_MULTIPLIER, "rank_fall", -ChampionConfig.FALL_DAMAGE_REDUCTION.get(), Operation.ADD_VALUE);
    }

    public static void addModifier(LivingEntity e, Holder<Attribute> attr, String name, double amount, Operation op) {
        AttributeInstance inst = e.getAttribute(attr);
        if (inst == null || amount == 0) return;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(EliteChampions.MODID, name);
        inst.removeModifier(id);
        inst.addPermanentModifier(new AttributeModifier(id, amount, op));
    }

    private static void applyEffects(Mob mob, ChampionRank rank, RankSettings s, RandomSource rand) {
        if (ChampionConfig.FIRE_RESISTANCE.get()) {
            mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
        }
        if (rand.nextDouble() < s.trialEffectChance.get()) {
            List<? extends String> pool = ChampionConfig.TRIAL_EFFECTS.get();
            if (!pool.isEmpty()) {
                ResourceLocation id = ResourceLocation.tryParse(pool.get(rand.nextInt(pool.size())));
                if (id != null) {
                    BuiltInRegistries.MOB_EFFECT.getHolder(id).ifPresent(h ->
                            mob.addEffect(new MobEffectInstance(h, MobEffectInstance.INFINITE_DURATION, 0, false, false)));
                }
            }
        }
        int glow = ChampionConfig.GLOW_SECONDS.get();
        if (glow > 0 && rank.ordinal() >= ChampionConfig.GLOW_MIN_RANK.get().ordinal()) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, glow * 20, 0, false, false));
        }
    }

    // ------------------------------------------------------------------ equipment

    private static void equip(Mob mob, ServerLevel level, ChampionRank rank, RankSettings s, RandomSource rand) {
        RegistryAccess access = level.registryAccess();
        float stdDrop = ChampionConfig.DROP_STANDARD_GEAR.get() ? 0.085F : 0.0F;
        float specialDrop = s.specialDropChance.get().floatValue();

        if (mob instanceof Zombie || mob instanceof AbstractSkeleton) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack stack = ChampionGear.standardArmor(s.armorMaterial.get(), slot);
                if (stack.isEmpty()) continue;
                boolean special = rand.nextDouble() < s.specialGearChance.get();
                if (special) ChampionGear.decorate(access, rank, stack, slot, rand);
                mob.setItemSlot(slot, stack);
                mob.setDropChance(slot, special ? specialDrop : stdDrop);
            }
        }

        ItemStack weapon;
        if (mob instanceof Drowned || !(mob instanceof Zombie)) {
            weapon = mob.getMainHandItem().copy();           // keep vanilla weapon (bow, crossbow, axe, trident...)
        } else {
            weapon = ChampionGear.itemById(s.weapon.get());  // zombie-types get the configured melee weapon
        }
        if (!weapon.isEmpty()) {
            boolean special = rand.nextDouble() < s.specialGearChance.get();
            if (special) ChampionGear.decorate(access, rank, weapon, EquipmentSlot.MAINHAND, rand);
            mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            mob.setDropChance(EquipmentSlot.MAINHAND, special ? specialDrop : stdDrop);
        }
    }

    // ------------------------------------------------------------------ one-time ability setup

    private static void onApply(Mob mob, ServerLevel level, ChampionRank rank, ChampionAbility ability) {
        RegistryAccess access = level.registryAccess();
        switch (ability) {
            case CREEPER_VOLATILE -> editCreeper((Creeper) mob, tag -> tag.putByte("ExplosionRadius", (byte) (int) ChampionConfig.VOLATILE_RADIUS.get()));
            case CREEPER_QUICK_FUSE -> editCreeper((Creeper) mob, tag -> tag.putShort("Fuse", (short) (int) ChampionConfig.QUICK_FUSE_TICKS.get()));
            case CREEPER_PERSISTENT -> {
                addModifier(mob, Attributes.MAX_HEALTH, "persistent_health", 1.0, Operation.ADD_MULTIPLIED_BASE);
                addModifier(mob, Attributes.KNOCKBACK_RESISTANCE, "persistent_kb", 0.6, Operation.ADD_VALUE);
            }
            case ZOMBIE_REINFORCED -> {
                addModifier(mob, Attributes.MAX_HEALTH, "reinforced_health", 1.0, Operation.ADD_MULTIPLIED_BASE);
                addModifier(mob, Attributes.ARMOR, "reinforced_armor", 4.0, Operation.ADD_VALUE);
            }
            case ZOMBIE_SPRINTER -> addModifier(mob, Attributes.MOVEMENT_SPEED, "sprinter_speed", ChampionConfig.SPRINTER_BONUS.get(), Operation.ADD_MULTIPLIED_BASE);
            case ZOMBIE_REINFORCEMENTS -> addModifier(mob, Attributes.SPAWN_REINFORCEMENTS_CHANCE, "reinforcements", 0.5, Operation.ADD_VALUE);
            case SPIDER_POUNCING -> addModifier(mob, Attributes.MOVEMENT_SPEED, "pouncing_speed", ChampionConfig.POUNCING_BONUS.get(), Operation.ADD_MULTIPLIED_BASE);
            case SPIDER_CLIMBER -> {
                addModifier(mob, Attributes.FOLLOW_RANGE, "climber_range", 16.0, Operation.ADD_VALUE);
                addModifier(mob, Attributes.MOVEMENT_SPEED, "climber_speed", 0.1, Operation.ADD_MULTIPLIED_BASE);
            }
            case SKELETON_FLAME_ARCHER -> {
                ItemStack held = mob.getMainHandItem();
                if (held.is(Items.BOW)) {
                    access.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(Enchantments.FLAME)
                            .ifPresent(h -> held.enchant(h, 1));
                }
            }
            case ENDERMAN_BLINK -> {
                addModifier(mob, Attributes.MAX_HEALTH, "blink_health", 0.5, Operation.ADD_MULTIPLIED_BASE);
                addModifier(mob, Attributes.ATTACK_DAMAGE, "blink_damage", 0.5, Operation.ADD_MULTIPLIED_BASE);
            }
            case SLIME_GIANT -> {
                Slime slime = (Slime) mob;
                int size = Math.min(ChampionConfig.SLIME_MAX_SIZE.get(), slime.getSize() * ChampionConfig.SLIME_SIZE_MULT.get());
                slime.setSize(Math.max(1, size), true);
            }
            case DROWNED_TRIDENT -> {
                ItemStack trident = new ItemStack(Items.TRIDENT);
                access.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(Enchantments.IMPALING)
                        .ifPresent(h -> trident.enchant(h, rank.ordinal() + 1));
                mob.setItemSlot(EquipmentSlot.MAINHAND, trident);
                mob.setDropChance(EquipmentSlot.MAINHAND, 0.1F);
            }
            case PILLAGER_ARBALEST -> {
                ItemStack crossbow = new ItemStack(Items.CROSSBOW);
                ChampionGear.decorate(access, rank, crossbow, EquipmentSlot.MAINHAND, mob.getRandom());
                mob.setItemSlot(EquipmentSlot.MAINHAND, crossbow);
                mob.setDropChance(EquipmentSlot.MAINHAND, ChampionConfig.RANKS.get(rank).specialDropChance.get().floatValue());
            }
            case PHANTOM_FRENZY -> {
                addModifier(mob, Attributes.ATTACK_DAMAGE, "frenzy_damage", 0.5, Operation.ADD_MULTIPLIED_BASE);
                addModifier(mob, Attributes.MAX_HEALTH, "frenzy_health", 0.25, Operation.ADD_MULTIPLIED_BASE);
            }
            case VINDICATOR_RAGE -> {
                addModifier(mob, Attributes.MOVEMENT_SPEED, "rage_speed", ChampionConfig.VINDICATOR_BONUS.get(), Operation.ADD_MULTIPLIED_BASE);
                addModifier(mob, Attributes.ATTACK_DAMAGE, "rage_damage", ChampionConfig.VINDICATOR_BONUS.get(), Operation.ADD_MULTIPLIED_BASE);
            }
            default -> { }
        }
    }

    /** Creeper fuse/radius are private; round-trip them through the entity's own NBT instead of an access transformer. */
    private static void editCreeper(Creeper creeper, Consumer<CompoundTag> edit) {
        CompoundTag tag = new CompoundTag();
        creeper.addAdditionalSaveData(tag);
        edit.accept(tag);
        creeper.readAdditionalSaveData(tag);
    }

    // ------------------------------------------------------------------ per-tick behaviour

    public static void tick(Mob mob, ServerLevel level) {
        if (ChampionData.isPending(mob)) {
            apply(mob, level);
            return;
        }
        ChampionRank rank = ChampionData.getRank(mob);
        RankSettings s = ChampionConfig.RANKS.get(rank);
        if (s == null) return;
        int t = mob.tickCount;

        if (t % 20 == 0) {
            double regen = s.regenPerSecond();
            if (regen > 0 && mob.getHealth() < mob.getMaxHealth()) mob.heal((float) regen);
        }
        if (t % 10 == 0 && ChampionConfig.PARTICLES.get() && rank.particle() != null) {
            level.sendParticles(rank.particle(), mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                    2, mob.getBbWidth() / 2, mob.getBbHeight() / 3, mob.getBbWidth() / 2, 0.02);
        }

        List<ChampionAbility> abilities = ChampionData.abilities(mob);
        if (abilities.isEmpty()) return;
        LivingEntity target = mob.getTarget();

        for (ChampionAbility ability : abilities) {
            switch (ability) {
                case CREEPER_LAST_STAND -> {
                    if (t % 10 == 0 && mob.getHealth() < mob.getMaxHealth() * ChampionConfig.LAST_STAND_THRESHOLD.get()) {
                        effect(mob, MobEffects.MOVEMENT_SPEED, 40, 1);
                    }
                }
                case SKELETON_RAPID_FIRE -> {
                    if (t % 20 == 0) {
                        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
                            if (wrapped.getGoal() instanceof RangedBowAttackGoal<?> bow) {
                                bow.setMinAttackInterval(ChampionConfig.RAPID_FIRE_INTERVAL.get());
                            }
                        }
                    }
                }
                case SKELETON_DEADEYE -> {
                    if (t % 10 == 0) {
                        Player p = level.getNearestPlayer(mob, ChampionConfig.DEADEYE_RANGE.get());
                        if (p != null && !p.isCreative() && !p.isSpectator()) effect(mob, MobEffects.MOVEMENT_SPEED, 30, 1);
                    }
                }
                case SPIDER_NIGHT_HUNTER -> {
                    if (t % 20 == 0 && level.getMaxLocalRawBrightness(mob.blockPosition()) <= 4) {
                        effect(mob, MobEffects.MOVEMENT_SPEED, 60, 1);
                        effect(mob, MobEffects.DAMAGE_BOOST, 60, 1);
                    }
                }
                case ENDERMAN_BLINK -> {
                    if (target != null && t % ChampionConfig.ENDERMAN_BLINK_INTERVAL.get() == 0
                            && mob.distanceToSqr(target) < 32 * 32 && mob.getRandom().nextFloat() < 0.6F) {
                        double x = target.getX() + (mob.getRandom().nextDouble() - 0.5) * 8.0;
                        double z = target.getZ() + (mob.getRandom().nextDouble() - 0.5) * 8.0;
                        mob.randomTeleport(x, target.getY(), z, true);
                    }
                }
                case WITCH_BARRAGE -> {
                    if (target != null && t % ChampionConfig.WITCH_INTERVAL.get() == 0
                            && mob.distanceToSqr(target) < 14 * 14 && mob.hasLineOfSight(target)) {
                        throwPotion(mob, level, target);
                    }
                }
                case BLAZE_BARRAGE -> {
                    if (target != null && t % ChampionConfig.BLAZE_INTERVAL.get() == 0
                            && mob.distanceToSqr(target) < 20 * 20 && mob.hasLineOfSight(target)) {
                        shootFireball(mob, level, target);
                    }
                }
                case BREEZE_GALE -> {
                    if (target != null && t % ChampionConfig.BREEZE_INTERVAL.get() == 0
                            && mob.distanceToSqr(target) < 20 * 20 && mob.hasLineOfSight(target)) {
                        shootWindCharge((Breeze) mob, level, target);
                    }
                }
                case PHANTOM_FRENZY -> {
                    if (target != null && t % ChampionConfig.PHANTOM_INTERVAL.get() == 0
                            && mob.getBoundingBox().inflate(0.5).intersects(target.getBoundingBox())) {
                        mob.swing(InteractionHand.MAIN_HAND);
                        mob.doHurtTarget(target);
                    }
                }
                default -> { }
            }
        }
    }

    public static void effect(LivingEntity e, Holder<MobEffect> effect, int ticks, int amplifier) {
        e.addEffect(new MobEffectInstance(effect, ticks, amplifier, false, true));
    }

    private static void throwPotion(Mob witch, ServerLevel level, LivingEntity target) {
        List<Holder<Potion>> options = List.of(Potions.HARMING, Potions.POISON, Potions.SLOWNESS, Potions.WEAKNESS);
        Holder<Potion> chosen = options.get(witch.getRandom().nextInt(options.size()));
        ThrownPotion potion = new ThrownPotion(level, witch);
        potion.setItem(PotionContents.createItemStack(Items.SPLASH_POTION, chosen));
        Vec3 v = target.getDeltaMovement();
        double dx = target.getX() + v.x - witch.getX();
        double dy = target.getEyeY() - 1.1 - potion.getY();
        double dz = target.getZ() + v.z - witch.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        potion.setXRot(potion.getXRot() + 20.0F);
        potion.shoot(dx, dy + dist * 0.2, dz, 0.75F, 8.0F);
        level.addFreshEntity(potion);
    }

    private static void shootFireball(Mob blaze, ServerLevel level, LivingEntity target) {
        double dx = target.getX() - blaze.getX();
        double dy = target.getY(0.5) - blaze.getY(0.5);
        double dz = target.getZ() - blaze.getZ();
        double spread = Math.sqrt(Math.sqrt(blaze.distanceToSqr(target))) * 0.5;
        Vec3 dir = new Vec3(blaze.getRandom().triangle(dx, 2.297 * spread), dy, blaze.getRandom().triangle(dz, 2.297 * spread));
        SmallFireball fireball = new SmallFireball(level, blaze, dir.normalize());
        fireball.setPos(fireball.getX(), blaze.getY(0.5) + 0.5, fireball.getZ());
        level.levelEvent(null, 1018, blaze.blockPosition(), 0);
        level.addFreshEntity(fireball);
    }

    private static void shootWindCharge(Breeze breeze, ServerLevel level, LivingEntity target) {
        BreezeWindCharge charge = new BreezeWindCharge(breeze, level);
        double dx = target.getX() - charge.getX();
        double dy = target.getY(0.3333333333333333) - charge.getY();
        double dz = target.getZ() - charge.getZ();
        charge.shoot(dx, dy, dz, 0.7F, 5.0F);
        level.addFreshEntity(charge);
    }

    // ------------------------------------------------------------------ loot

    public static void addLoot(Mob mob, ChampionRank rank, Collection<ItemEntity> drops) {
        RankSettings s = ChampionConfig.RANKS.get(rank);
        if (s == null) return;
        RandomSource rand = mob.getRandom();

        if (rand.nextDouble() < s.essenceChance.get()) {
            int min = s.essenceMin.get();
            int max = Math.max(min, s.essenceMax.get());
            int n = min + rand.nextInt(max - min + 1);
            if (n > 0) drops.add(drop(mob, new ItemStack(ModItems.ESSENCE.get(rank).get(), n)));
        }

        for (String entry : s.bonusLoot.get()) {
            String[] parts = entry.split(";");
            if (parts.length < 4) continue;
            try {
                double chance = Double.parseDouble(parts[1].trim());
                int min = Integer.parseInt(parts[2].trim());
                int max = Math.max(min, Integer.parseInt(parts[3].trim()));
                if (rand.nextDouble() >= chance) continue;
                ItemStack stack = ChampionGear.itemById(parts[0].trim());
                if (stack.isEmpty()) continue;
                stack.setCount(min + rand.nextInt(max - min + 1));
                drops.add(drop(mob, stack));
            } catch (NumberFormatException ignored) {
                // malformed entry in config; skip it
            }
        }
    }

    private static ItemEntity drop(Mob mob, ItemStack stack) {
        return new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), stack);
    }
}
