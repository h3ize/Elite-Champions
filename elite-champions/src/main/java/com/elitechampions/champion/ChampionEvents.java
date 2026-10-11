package com.elitechampions.champion;

import com.elitechampions.EliteChampions;
import com.elitechampions.config.ChampionConfig;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

@EventBusSubscriber(modid = EliteChampions.MODID)
public final class ChampionEvents {
    private ChampionEvents() {}

    /** Decide rank at spawn. Stats/gear are applied on the first tick, after vanilla finalizeSpawn has run. */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (mob.level().isClientSide) return;
        ChampionManager.tryAssign(mob, event.getSpawnType());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        if (!mob.isAlive() || !ChampionData.isChampion(mob)) return;
        ChampionManager.tick(mob, level);
    }

    /** Sniper: aim arrows precisely at the target; Piercing/Flame Archer: tweak vanilla arrow properties. */
    @SubscribeEvent
    public static void onArrowJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof AbstractArrow arrow) || arrow.tickCount > 0) return;
        if (!(arrow.getOwner() instanceof Mob shooter) || !ChampionData.isChampion(shooter)) return;
        List<ChampionAbility> abilities = ChampionData.abilities(shooter);

        if (abilities.contains(ChampionAbility.SKELETON_SNIPER)) {
            LivingEntity target = shooter.getTarget();
            if (target != null) {
                double speed = arrow.getDeltaMovement().length();
                double dx = target.getX() - shooter.getX();
                double dy = target.getY(0.3333333333333333) - arrow.getY();
                double dz = target.getZ() - shooter.getZ();
                double horizontal = Math.sqrt(dx * dx + dz * dz);
                arrow.shoot(dx, dy + horizontal * 0.2, dz, (float) speed, 0.0F);
            }
        }
        if (abilities.contains(ChampionAbility.SKELETON_PIERCING)) {
            CompoundTag arrowTag = arrow.saveWithoutId(new CompoundTag());
            arrowTag.putByte("PierceLevel", (byte) (int) ChampionConfig.PIERCING_LEVEL.get());
            arrow.load(arrowTag);
        }
        if (abilities.contains(ChampionAbility.SKELETON_FLAME_ARCHER)) {
            arrow.setRemainingFireTicks(100);
        }
    }

    /** Berserker and Sniper damage scaling. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Mob attacker) || !ChampionData.isChampion(attacker)) return;
        List<ChampionAbility> abilities = ChampionData.abilities(attacker);
        float amount = event.getAmount();

        if (abilities.contains(ChampionAbility.ZOMBIE_BERSERKER)
                && attacker.getHealth() < attacker.getMaxHealth() * ChampionConfig.BERSERKER_THRESHOLD.get()) {
            amount *= ChampionConfig.BERSERKER_MULT.get().floatValue();
        }
        if (abilities.contains(ChampionAbility.SKELETON_SNIPER) && event.getSource().getDirectEntity() instanceof AbstractArrow) {
            amount *= ChampionConfig.SNIPER_DAMAGE_MULT.get().floatValue();
        }
        event.setAmount(amount);
    }

    /** On-hit vanilla effects. */
    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0) return;
        Entity attackerEntity = event.getSource().getEntity();
        if (!(attackerEntity instanceof Mob attacker) || attacker.level().isClientSide || !ChampionData.isChampion(attacker)) return;
        if (event.getSource().getDirectEntity() != attackerEntity) return; // melee only
        LivingEntity victim = event.getEntity();

        for (ChampionAbility ability : ChampionData.abilities(attacker)) {
            switch (ability) {
                case ZOMBIE_INFECTIOUS -> {
                    Holder<MobEffect> effect = attacker.getRandom().nextBoolean() ? MobEffects.POISON : MobEffects.WEAKNESS;
                    ChampionManager.effect(victim, effect, ChampionConfig.INFECTIOUS_SECONDS.get() * 20, 0);
                }
                case SPIDER_VENOMOUS -> ChampionManager.effect(victim, MobEffects.POISON, ChampionConfig.VENOM_SECONDS.get() * 20, 0);
                case SPIDER_WEB_WEAVER -> ChampionManager.effect(victim, MobEffects.MOVEMENT_SLOWDOWN, ChampionConfig.WEB_SLOW_SECONDS.get() * 20, 1);
                case HUSK_STARVING -> ChampionManager.effect(victim, MobEffects.HUNGER, ChampionConfig.HUSK_HUNGER_SECONDS.get() * 20, 1);
                default -> { }
            }
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) return;
        ChampionRank rank = ChampionData.getRank(mob);
        if (rank == ChampionRank.COMMON || !event.isRecentlyHit()) return;
        ChampionManager.addLoot(mob, rank, event.getDrops());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        ChampionRank rank = ChampionData.getRank(mob);
        if (rank == ChampionRank.COMMON || !ChampionConfig.FIREWORKS.get()) return;
        if (rank.ordinal() < ChampionConfig.FIREWORKS_MIN_RANK.get().ordinal()) return;

        Integer color = rank.color().getColor();
        int c = color == null ? 0xFFAA00 : color;
        for (int i = 0; i < 3; i++) {
            ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
            rocket.set(DataComponents.FIREWORKS, new Fireworks(1, List.of(
                    new FireworkExplosion(FireworkExplosion.Shape.STAR, IntList.of(c, 0xFFFFFF), IntList.of(), true, true))));
            level.addFreshEntity(new FireworkRocketEntity(level,
                    mob.getX() + (mob.getRandom().nextDouble() - 0.5) * 2, mob.getY() + 0.5,
                    mob.getZ() + (mob.getRandom().nextDouble() - 0.5) * 2, rocket));
        }
    }
}
