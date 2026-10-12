package com.mobascension;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Behaviour of each champion ability. Called from ChampionEvents. */
public final class AbilityLogic {
    private static final net.minecraft.resources.ResourceLocation BERSERK_SPEED = ChampionFactory.id("berserk_speed");
    private static final net.minecraft.resources.ResourceLocation BERSERK_DAMAGE = ChampionFactory.id("berserk_damage");

    private AbilityLogic() {}

    private static double power(Ability a) { return AscensionConfig.ability(a).power.get(); }

    private static int cooldown(Ability a) { return Math.max(1, AscensionConfig.ability(a).cooldown.get()); }

    private static boolean ready(Mob mob, Ability a) {
        return (mob.tickCount + mob.getId()) % cooldown(a) == 0;
    }

    public static void tick(Mob mob, ServerLevel level, Rank rank, EnumSet<Ability> abilities) {
        AscensionConfig.RankSettings cfg = AscensionConfig.rank(rank);

        if (mob.tickCount % 20 == 0) {
            double regen = cfg.regen.get() * ChampionFactory.statScale(mob, rank);
            if (regen > 0 && mob.getHealth() < mob.getMaxHealth()) mob.heal((float) regen);
        }

        if (AscensionConfig.PARTICLES.get() && mob.tickCount % 10 == 0) {
            double w = mob.getBbWidth();
            level.sendParticles(rank.particle(), mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    2 + rank.ordinal(), w * 0.5, mob.getBbHeight() * 0.4, w * 0.5, 0.02);
        }

        if (rank == Rank.APEX && mob.tickCount % 20 == 0) {
            int radius = AscensionConfig.APEX_AGGRO_RADIUS.get();
            if (radius > 0) aggro(mob, level, radius);
        }

        if (rank == Rank.APEX && mob.getPersistentData().getBoolean(ChampionData.RANGED)
                && (mob.tickCount + mob.getId()) % Math.max(1, cfg.rangedCooldown.get()) == 0) {
            LivingEntity t = mob.getTarget();
            if (t != null && t.isAlive()) {
                double d = mob.distanceTo(t);
                if (d >= 4 && d <= 32 && mob.hasLineOfSight(t)) shoot(mob, level, rank, t);
            }
        }

        if (abilities.isEmpty()) return;

        if (abilities.contains(Ability.BERSERKER) && mob.tickCount % 10 == 0) {
            berserk(mob, level);
        }
        if (abilities.contains(Ability.AEGIS) && ready(mob, Ability.AEGIS)) {
            float max = mob.getMaxAbsorption();
            if (mob.getAbsorptionAmount() < max) {
                mob.setAbsorptionAmount(Math.min(max, mob.getAbsorptionAmount() + (float) power(Ability.AEGIS) * 0.25F));
            }
        }
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return;

        if (abilities.contains(Ability.BLINKER) && ready(mob, Ability.BLINKER)) {
            blink(mob, level, target);
        }
        if (abilities.contains(Ability.STORMCALLER) && target instanceof Player && ready(mob, Ability.STORMCALLER)) {
            if (mob.distanceTo(target) <= 24 && mob.hasLineOfSight(target)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(target.getX(), target.getY(), target.getZ());
                    bolt.setDamage((float) power(Ability.STORMCALLER));
                    level.addFreshEntity(bolt);
                }
            }
        }
    }

    /** Targets the nearest survival/adventure player within the radius (also works for passive mobs). */
    private static void aggro(Mob mob, ServerLevel level, int radius) {
        LivingEntity current = mob.getTarget();
        if (current instanceof Player cp && cp.isAlive() && mob.distanceTo(cp) <= radius) return;
        Player best = null;
        double bestDist = (double) radius * radius;
        for (Player p : level.players()) {
            if (!p.isAlive() || p.isCreative() || p.isSpectator()) continue;
            double d = mob.distanceToSqr(p);
            if (d <= bestDist) {
                bestDist = d;
                best = p;
            }
        }
        if (best != null) mob.setTarget(best);
    }

    private static void shoot(Mob mob, ServerLevel level, Rank rank, LivingEntity target) {
        Vec3 direction = new Vec3(
                target.getX() - mob.getX(),
                target.getY(0.5) - mob.getY(0.5),
                target.getZ() - mob.getZ()).normalize();
        if (rank != Rank.APEX) return;
        boolean skull = mob.getRandom().nextBoolean();
        AbstractHurtingProjectile projectile = skull
                ? new WitherSkull(level, mob, direction)
                : new LargeFireball(level, mob, direction, 1);
        projectile.setPos(mob.getX(), mob.getY(0.5) + 0.3, mob.getZ());
        level.addFreshEntity(projectile);
        level.playSound(null, mob.blockPosition(),
                skull ? SoundEvents.WITHER_SHOOT : SoundEvents.GHAST_SHOOT,
                SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    private static void berserk(Mob mob, ServerLevel level) {
        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        boolean enraged = mob.getHealth() / mob.getMaxHealth() <= power(Ability.BERSERKER);
        boolean active = speed != null && speed.hasModifier(BERSERK_SPEED);
        if (enraged && !active) {
            if (speed != null) speed.addTransientModifier(new AttributeModifier(BERSERK_SPEED, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            if (dmg != null) dmg.addTransientModifier(new AttributeModifier(BERSERK_DAMAGE, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, mob.getX(), mob.getEyeY(), mob.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
            level.playSound(null, mob.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0F, 1.2F);
        } else if (!enraged && active) {
            speed.removeModifier(BERSERK_SPEED);
            if (dmg != null) dmg.removeModifier(BERSERK_DAMAGE);
        }
    }

    private static void blink(Mob mob, ServerLevel level, LivingEntity target) {
        double dist = mob.distanceTo(target);
        if (dist < 5 || dist > power(Ability.BLINKER)) return;
        double ox = mob.getX(), oy = mob.getY(), oz = mob.getZ();
        double ang = mob.getRandom().nextDouble() * Math.PI * 2;
        double nx = target.getX() + Math.cos(ang) * 2.5;
        double nz = target.getZ() + Math.sin(ang) * 2.5;
        if (mob.randomTeleport(nx, target.getY(), nz, true)) {
            level.sendParticles(ParticleTypes.PORTAL, ox, oy + 1, oz, 20, 0.3, 0.6, 0.3, 0.3);
            level.sendParticles(ParticleTypes.PORTAL, mob.getX(), mob.getY() + 1, mob.getZ(), 20, 0.3, 0.6, 0.3, 0.3);
            level.playSound(null, mob.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    public static void onAttack(Mob attacker, LivingEntity victim, float damage, EnumSet<Ability> abilities) {
        if (abilities.contains(Ability.VAMPIRIC)) {
            attacker.heal(damage * (float) power(Ability.VAMPIRIC));
        }
        if (abilities.contains(Ability.FROSTBITE)) {
            int ticks = (int) (power(Ability.FROSTBITE) * 20);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 1, false, true, true));
            if (victim.canFreeze()) victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 20);
        }
        if (abilities.contains(Ability.VENOMOUS)) {
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, (int) (power(Ability.VENOMOUS) * 20), 1, false, true, true));
        }
    }

    public static void onDefend(Mob champion, LivingEntity attacker, float damage, EnumSet<Ability> abilities) {
        if (abilities.contains(Ability.THORNED)) {
            attacker.hurt(champion.damageSources().thorns(champion), damage * (float) power(Ability.THORNED));
        }
    }

    public static void onDeath(Mob mob, ServerLevel level, EnumSet<Ability> abilities) {
        if (abilities.contains(Ability.VOLATILE)) {
            Level.ExplosionInteraction mode = Level.ExplosionInteraction.NONE;
            level.explode(mob, mob.getX(), mob.getY(0.5), mob.getZ(), (float) power(Ability.VOLATILE), mode);
        }
        if (abilities.contains(Ability.SPLITTING) && !(mob instanceof Slime)) {
            int count = (int) Math.round(power(Ability.SPLITTING));
            for (int i = 0; i < count; i++) {
                Entity e = mob.getType().create(level);
                if (!(e instanceof Mob minion)) continue;
                minion.moveTo(mob.getX() + (mob.getRandom().nextDouble() - 0.5) * 1.5, mob.getY(),
                        mob.getZ() + (mob.getRandom().nextDouble() - 0.5) * 1.5,
                        mob.getRandom().nextFloat() * 360F, 0F);
                minion.getPersistentData().putBoolean(ChampionData.MINION, true);
                minion.finalizeSpawn(level, level.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                level.addFreshEntity(minion);
            }
        }
    }
}
