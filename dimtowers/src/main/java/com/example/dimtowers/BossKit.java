package com.example.dimtowers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared boss logic: gear, phases, theme-aware minion waves and the per-boss signature moves.
 * Everything here uses vanilla projectiles, entities, blocks and particles only.
 */
public final class BossKit {
    private BossKit() {}

    public enum Weapon { SWORD, BOW, AXE, TRIDENT, NONE }

    /** One entry per signature attack. Each boss class picks one or two of these. */
    public enum Move {
        WITHER_SKULLS,   // Ashen Tyrant: fan of wither skulls, centre one is the blue (dangerous) skull
        ARROW_VOLLEY,    // Bone Archon: arrow fan, plus a sky-fall of arrows when enraged
        GROUND_SLAM,     // Rotting Colossus / Slime King: telegraphed shockwave
        CALL_HOGLINS,    // Gilded Warlord: summons hoglins
        LEAP,            // Axe Warlord: telegraphed leap with a landing shockwave
        BLAZE_BARRAGE,   // Blaze King: large fireballs
        FIRE_NOVA,       // Blaze King: ring of small fireballs
        WEB_SHOT,        // Spider Queen: temporary cobwebs around the target
        BROOD,           // Spider Queen: cave spider brood
        BLINK,           // Void Stalker: teleports next to the target
        DRAGON_BREATH,   // Void Stalker: lingering dragon's-breath cloud
        TRIDENT_FAN      // Tidal Tyrant: fan of thrown tridents
    }

    public static final double DMG_ZOMBIE = 18.0, DMG_SKELETON = 10.0, DMG_WITHER = 18.0, DMG_PIGLIN = 20.0,
            DMG_VINDICATOR = 20.0, DMG_BLAZE = 14.0, DMG_SPIDER = 16.0, DMG_ENDER = 22.0, DMG_SLIME = 18.0,
            DMG_DROWNED = 20.0;
    public static final int XP = 1000; // ~ level 26 in one go

    public static AttributeSupplier.Builder boost(AttributeSupplier.Builder b, double damage) {
        return boost(b, damage, 2.0);
    }

    public static AttributeSupplier.Builder boost(AttributeSupplier.Builder b, double damage, double scale) {
        return b.add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0)
                .add(Attributes.SCALE, scale);
    }

    /** Which tower this boss belongs to, read from the tag the boss spawner puts in its spawn data. */
    public static Theme theme(Entity e) {
        for (String s : e.getTags()) if (s.startsWith("dt_theme_")) return Theme.byName(s.substring(9));
        return Theme.OVERWORLD;
    }

    /**
     * Idempotent. Called from finalizeSpawn (eggs, /summon) AND from the first AI tick, because a Trial Spawner only
     * calls finalizeSpawn when the spawn data is just {id:...}, and ours also carries the theme tag.
     */
    public static void setup(Mob mob, Weapon weaponType, double damage) {
        if (mob.getTags().contains("dt_ready")) return;
        mob.addTag("dt_ready");
        float frac = mob.getMaxHealth() <= 0 ? 1f : mob.getHealth() / mob.getMaxHealth();
        mob.setCustomName(mob.getType().getDescription());
        mob.setCustomNameVisible(true);
        if (weaponType == Weapon.NONE) {
            mob.addTag("dt_nogear");
        } else {
            var enchants = mob.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            ItemStack weapon;
            switch (weaponType) {
                case BOW -> {
                    weapon = new ItemStack(Items.BOW);
                    weapon.enchant(enchants.getOrThrow(Enchantments.POWER), 8);
                    weapon.enchant(enchants.getOrThrow(Enchantments.PUNCH), 2);
                }
                case AXE -> {
                    weapon = new ItemStack(Items.NETHERITE_AXE);
                    weapon.enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 5);
                }
                case TRIDENT -> {
                    weapon = new ItemStack(Items.TRIDENT);
                    weapon.enchant(enchants.getOrThrow(Enchantments.IMPALING), 5);
                }
                default -> {
                    weapon = new ItemStack(Items.NETHERITE_SWORD);
                    weapon.enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 5);
                    weapon.enchant(enchants.getOrThrow(Enchantments.KNOCKBACK), 3);
                }
            }
            mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) mob.setDropChance(slot, 0f);
        // vanilla finalizeSpawn (e.g. wither skeleton) resets damage, so set it again here
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        if (mob instanceof AbstractPiglin p) p.setImmuneToZombification(true);
        mob.setPersistenceRequired();
        mob.setHealth(mob.getMaxHealth() * frac);
    }

    @Nullable
    public static LivingEntity target(Mob mob) {
        LivingEntity t = mob.getTarget();
        // Only Brain-based mobs (piglins) register ATTACK_TARGET. Goal-based mobs such as
        // zombies and skeletons throw "Unregistered memory fetched" if asked for it.
        if (t == null && mob instanceof AbstractPiglin) {
            t = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }
        return t;
    }

    private static void announce(ServerLevel sl, Mob boss, String key) {
        Component msg = Component.translatable(key, boss.getDisplayName());
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(boss) < 64.0 * 64.0) p.displayClientMessage(msg, true);
    }

    /** Per-tick boss behaviour: gear upkeep, enrage at 50%, minion waves (from the tower's own mob list), lightning while enraged. */
    public static void tick(Mob mob) {
        if (!(mob.level() instanceof ServerLevel sl)) return;
        if (mob.tickCount % 20 == 0 && !mob.getTags().contains("dt_nogear")) {
            ItemStack h = mob.getItemBySlot(EquipmentSlot.HEAD);
            if (h.isEmpty() || h.getDamageValue() > 0) mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        }
        float frac = mob.getHealth() / mob.getMaxHealth();
        if (frac <= 0.75f) wave(mob, sl, "dt_wave1", 2, 0);
        if (frac <= 0.40f) wave(mob, sl, "dt_wave2", 3, 1);
        if (frac <= 0.15f) wave(mob, sl, "dt_wave3", 4, 2);
        if (frac <= 0.5f && !mob.getTags().contains("dt_enraged")) {
            mob.addTag("dt_enraged");
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MobEffectInstance.INFINITE_DURATION, 1, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, MobEffectInstance.INFINITE_DURATION, 1, false, true));
            sl.playSound(null, mob.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 1.0f);
            announce(sl, mob, "message.dimtowers.enrage");
        }
        if (mob.getTags().contains("dt_enraged") && mob.tickCount % 140 == 0) {
            LivingEntity t = target(mob);
            if (t != null && t.isAlive()) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(t.blockPosition()));
                    sl.addFreshEntity(bolt);
                }
            }
        }
    }

    private static void wave(Mob boss, ServerLevel sl, String tag, int count, int boostLevel) {
        if (boss.getTags().contains(tag)) return;
        boss.addTag(tag);
        List<EntityType<?>> pool = theme(boss).waveMobs();
        for (int i = 0; i < count; i++) {
            spawnMinion(sl, boss, pool.get(boss.getRandom().nextInt(pool.size())), boostLevel);
        }
        sl.playSound(null, boss.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2.0f, 0.8f);
        announce(sl, boss, "message.dimtowers.wave");
    }

    /** Spawns one persistent, loot-less minion near the boss and points it at the boss's target. */
    @Nullable
    public static Mob spawnMinion(ServerLevel sl, Mob boss, EntityType<?> type, int boostLevel) {
        Entity e = type.create(sl);
        if (!(e instanceof Mob m)) return null;
        RandomSource r = boss.getRandom();
        m.moveTo(boss.getX() + (r.nextDouble() - 0.5) * 8, boss.getY(), boss.getZ() + (r.nextDouble() - 0.5) * 8,
                r.nextFloat() * 360f, 0f);
        m.finalizeSpawn(sl, sl.getCurrentDifficultyAt(m.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        for (EquipmentSlot slot : EquipmentSlot.values()) m.setDropChance(slot, 0f);
        m.setPersistenceRequired();
        m.addTag("dt_minion");
        m.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
        if (boostLevel > 0) {
            m.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, MobEffectInstance.INFINITE_DURATION, boostLevel - 1, false, false));
        }
        if (m instanceof AbstractPiglin p) p.setImmuneToZombification(true);
        if (m instanceof Hoglin h) h.setImmuneToZombification(true);
        LivingEntity t = target(boss);
        if (t != null) m.setTarget(t);
        sl.addFreshEntity(m);
        return m;
    }

    public static int countMinions(ServerLevel sl, Mob boss) {
        return sl.getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(40),
                m -> m.getTags().contains("dt_minion") && m.isAlive()).size();
    }

    private interface Proj { @Nullable Entity make(Level level, Mob boss, Vec3 dir, int index); }

    /** Signature moves. Attach with goalSelector.addGoal(2, new SignatureGoal(this, Move.X, Move.Y)). */
    public static class SignatureGoal extends Goal {
        private final Mob boss;
        private final Move[] moves;
        private final int[] cd;
        private int windup = -1;
        private int windupIdx = -1;
        private int leapTicks = 0;
        private final List<BlockPos> webs = new ArrayList<>();
        private final List<Long> webExpiry = new ArrayList<>();

        public SignatureGoal(Mob boss, Move... moves) {
            this.boss = boss;
            this.moves = moves;
            this.cd = new int[moves.length];
            for (int i = 0; i < cd.length; i++) cd[i] = 60 + 50 * i;
        }

        @Override public boolean canUse() { LivingEntity t = target(boss); return t != null && t.isAlive(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void stop() {
            windup = -1;
            if (boss.level() instanceof ServerLevel sl) clearWebs(sl, true);
        }

        @Override
        public void tick() {
            if (!(boss.level() instanceof ServerLevel sl)) return;
            LivingEntity t = target(boss);
            if (t == null) return;
            clearWebs(sl, false);
            boolean enraged = boss.getTags().contains("dt_enraged");

            if (leapTicks > 0) {
                leapTicks--;
                if (leapTicks < 34 && boss.onGround()) { leapTicks = 0; slam(sl, 6.0, 12f); }
            }
            if (windup > 0) {
                windup--;
                if (windup % 4 == 0) {
                    sl.sendParticles(ParticleTypes.CRIT, boss.getX(), boss.getY() + 0.2, boss.getZ(), 12, 1.5, 0.1, 1.5, 0.1);
                }
                if (windup == 0) { windup = -1; execute(windupIdx, sl, t, enraged); }
                return;
            }
            for (int i = 0; i < cd.length; i++) if (cd[i] > 0) cd[i]--;
            for (int i = 0; i < moves.length; i++) if (cd[i] <= 0 && start(i, sl, t, enraged)) break;
        }

        private void begin(int idx, int windupTicks, int cooldown) {
            windup = windupTicks;
            windupIdx = idx;
            cd[idx] = cooldown;
        }

        private void sound(ServerLevel sl, SoundEvent s, float vol, float pitch) {
            sl.playSound(null, boss.blockPosition(), s, SoundSource.HOSTILE, vol, pitch);
        }

        private boolean start(int i, ServerLevel sl, LivingEntity t, boolean enraged) {
            double d2 = boss.distanceToSqr(t);
            switch (moves[i]) {
                case GROUND_SLAM -> {
                    if (d2 > 144) return false;
                    begin(i, 20, enraged ? 90 : 150);
                    sound(sl, SoundEvents.ANVIL_LAND, 1.5f, 0.6f);
                    return true;
                }
                case LEAP -> {
                    if (d2 < 36 || d2 > 576 || !boss.onGround() || !boss.hasLineOfSight(t)) return false;
                    begin(i, 14, enraged ? 100 : 160);
                    sound(sl, SoundEvents.RAVAGER_ROAR, 1.5f, 1.0f);
                    return true;
                }
                case CALL_HOGLINS -> {
                    if (countMinions(sl, boss) >= 8) { cd[i] = 100; return false; }
                    int n = enraged ? 3 : 2;
                    for (int j = 0; j < n; j++) spawnMinion(sl, boss, EntityType.HOGLIN, enraged ? 1 : 0);
                    sound(sl, SoundEvents.HOGLIN_ANGRY, 2.0f, 0.7f);
                    sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, boss.getX(), boss.getY(1.0), boss.getZ(), 15, 1, 1, 1, 0.0);
                    cd[i] = enraged ? 260 : 400;
                    return true;
                }
                case BROOD -> {
                    if (countMinions(sl, boss) >= 8) { cd[i] = 100; return false; }
                    int n = enraged ? 4 : 3;
                    for (int j = 0; j < n; j++) spawnMinion(sl, boss, EntityType.CAVE_SPIDER, enraged ? 1 : 0);
                    sound(sl, SoundEvents.SPIDER_AMBIENT, 2.0f, 0.5f);
                    cd[i] = enraged ? 280 : 420;
                    return true;
                }
                case BLINK -> {
                    if (d2 < 49) return false;
                    blink(sl, t);
                    cd[i] = enraged ? 70 : 110;
                    return true;
                }
                default -> { }
            }

            // ranged moves: throttle the line-of-sight check and keep a minimum range
            if (boss.tickCount % 4 != 0) return false;
            if (d2 < 16 || !boss.hasLineOfSight(t)) return false;
            Level level = boss.level();
            switch (moves[i]) {
                case WITHER_SKULLS -> {
                    fan(level, t, enraged ? 3 : 1, 0.2f, (l, b, d, idx) -> {
                        WitherSkull s = new WitherSkull(l, b, d);
                        if (idx == 0) s.setDangerous(true);
                        return s;
                    });
                    sound(sl, SoundEvents.WITHER_SHOOT, 2.0f, 1.0f);
                    cd[i] = enraged ? 45 : 90;
                    return true;
                }
                case ARROW_VOLLEY -> {
                    fan(level, t, enraged ? 4 : 2, 0.12f, (l, b, d, idx) -> {
                        double h = Math.sqrt(d.x * d.x + d.z * d.z);
                        return arrow(l, b, d.x, d.y + h * 0.2, d.z, 1.8f, 4f);
                    });
                    if (enraged) {
                        RandomSource r = boss.getRandom();
                        for (int j = 0; j < 6; j++) {
                            Entity a = arrow(level, boss, 0, -1, 0, 1.6f, 3f);
                            if (a == null) continue;
                            a.setPos(t.getX() + (r.nextDouble() - 0.5) * 8, t.getY() + 14, t.getZ() + (r.nextDouble() - 0.5) * 8);
                            level.addFreshEntity(a);
                        }
                    }
                    sound(sl, SoundEvents.SKELETON_SHOOT, 2.0f, 0.7f);
                    cd[i] = enraged ? 50 : 100;
                    return true;
                }
                case BLAZE_BARRAGE -> {
                    final int power = enraged ? 2 : 1;
                    fan(level, t, enraged ? 2 : 1, 0.2f, (l, b, d, idx) -> new LargeFireball(l, b, d, power));
                    sound(sl, SoundEvents.BLAZE_SHOOT, 2.0f, 0.7f);
                    cd[i] = enraged ? 50 : 90;
                    return true;
                }
                case FIRE_NOVA -> {
                    for (int j = 0; j < 12; j++) {
                        double a = j * Math.PI / 6.0;
                        SmallFireball f = new SmallFireball(level, boss, new Vec3(Math.cos(a), -0.1, Math.sin(a)));
                        f.setPos(boss.getX() + Math.cos(a) * 1.5, boss.getY(0.5), boss.getZ() + Math.sin(a) * 1.5);
                        level.addFreshEntity(f);
                    }
                    sound(sl, SoundEvents.BLAZE_SHOOT, 2.0f, 0.5f);
                    cd[i] = enraged ? 100 : 170;
                    return true;
                }
                case WEB_SHOT -> {
                    BlockPos c = t.blockPosition();
                    long expiry = sl.getGameTime() + (enraged ? 200 : 140);
                    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                        if (!enraged && dx != 0 && dz != 0) continue;
                        BlockPos p = c.offset(dx, 0, dz);
                        if (sl.getBlockState(p).isAir()) {
                            sl.setBlock(p, Blocks.COBWEB.defaultBlockState(), 3);
                            webs.add(p);
                            webExpiry.add(expiry);
                        }
                    }
                    sl.sendParticles(ParticleTypes.CLOUD, t.getX(), t.getY(0.5), t.getZ(), 20, 0.8, 0.5, 0.8, 0.02);
                    sound(sl, SoundEvents.SPIDER_AMBIENT, 1.5f, 1.4f);
                    cd[i] = enraged ? 90 : 150;
                    return true;
                }
                case DRAGON_BREATH -> {
                    fan(level, t, enraged ? 1 : 0, 0.25f, (l, b, d, idx) -> new DragonFireball(l, b, d));
                    sound(sl, SoundEvents.ENDER_DRAGON_SHOOT, 1.5f, 1.0f);
                    cd[i] = enraged ? 100 : 160;
                    return true;
                }
                case TRIDENT_FAN -> {
                    fan(level, t, enraged ? 2 : 1, 0.14f, (l, b, d, idx) -> {
                        ThrownTrident tr = new ThrownTrident(l, b, new ItemStack(Items.TRIDENT));
                        tr.pickup = AbstractArrow.Pickup.DISALLOWED;
                        double h = Math.sqrt(d.x * d.x + d.z * d.z);
                        tr.shoot(d.x, d.y + h * 0.15, d.z, 2.0f, 3f);
                        return tr;
                    });
                    sound(sl, SoundEvents.DROWNED_SHOOT, 2.0f, 0.8f);
                    cd[i] = enraged ? 60 : 110;
                    return true;
                }
                default -> { return false; }
            }
        }

        private void execute(int idx, ServerLevel sl, LivingEntity t, boolean enraged) {
            switch (moves[idx]) {
                case GROUND_SLAM -> slam(sl, enraged ? 11.0 : 8.0, enraged ? 18f : 12f);
                case LEAP -> {
                    Vec3 d = new Vec3(t.getX() - boss.getX(), 0, t.getZ() - boss.getZ());
                    double len = d.length();
                    if (len < 1.0E-3) return;
                    double speed = Math.min(1.6, 0.35 + len * 0.07);
                    d = d.scale(speed / len);
                    boss.setDeltaMovement(d.x, 0.85, d.z);
                    leapTicks = 40;
                }
                default -> { }
            }
        }

        /** Shockwave: damages and launches everything that is not a monster within the radius. */
        private void slam(ServerLevel sl, double radius, float dmg) {
            Vec3 c = boss.position();
            List<LivingEntity> list = sl.getEntitiesOfClass(LivingEntity.class,
                    boss.getBoundingBox().inflate(radius, 2.0, radius),
                    e -> e != boss && e.isAlive() && !(e instanceof Enemy));
            for (LivingEntity e : list) {
                if (e.distanceToSqr(boss) > radius * radius) continue;
                e.hurt(sl.damageSources().mobAttack(boss), dmg);
                Vec3 away = e.position().subtract(c).multiply(1, 0, 1);
                if (away.lengthSqr() < 1.0E-4) away = new Vec3(1, 0, 0);
                away = away.normalize();
                e.setDeltaMovement(e.getDeltaMovement().add(away.x * 1.2, 0.7, away.z * 1.2));
                e.hurtMarked = true;
            }
            for (int i = 0; i < 36; i++) {
                double a = i * Math.PI / 18.0;
                for (double r = 2; r <= radius; r += 2) {
                    sl.sendParticles(ParticleTypes.CLOUD, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 1, 0, 0.1, 0, 0.02);
                }
            }
            sl.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.5, c.z, 6, radius * 0.3, 0.2, radius * 0.3, 0.0);
            sound(sl, SoundEvents.IRON_GOLEM_ATTACK, 2.0f, 0.5f);
        }

        private void blink(ServerLevel sl, LivingEntity t) {
            RandomSource r = boss.getRandom();
            for (int a = 0; a < 10; a++) {
                double ang = r.nextDouble() * Math.PI * 2;
                double nx = t.getX() + Math.cos(ang) * 2.5, nz = t.getZ() + Math.sin(ang) * 2.5;
                AABB bb = boss.getBoundingBox().move(nx - boss.getX(), t.getY() - boss.getY(), nz - boss.getZ());
                if (!sl.noCollision(boss, bb)) continue;
                sl.sendParticles(ParticleTypes.PORTAL, boss.getX(), boss.getY(1.0), boss.getZ(), 40, 0.5, 1.0, 0.5, 0.3);
                boss.moveTo(nx, t.getY(), nz, boss.getYRot(), boss.getXRot());
                boss.getNavigation().stop();
                boss.getLookControl().setLookAt(t, 30f, 30f);
                sl.sendParticles(ParticleTypes.PORTAL, nx, boss.getY(1.0), nz, 40, 0.5, 1.0, 0.5, 0.3);
                sound(sl, SoundEvents.ENDERMAN_TELEPORT, 2.0f, 0.6f);
                break;
            }
        }

        private void fan(Level level, LivingEntity t, int n, float step, Proj p) {
            Vec3 base = new Vec3(t.getX() - boss.getX(), t.getY(0.5) - boss.getY(0.5), t.getZ() - boss.getZ());
            for (int i = -n; i <= n; i++) {
                Entity e = p.make(level, boss, base.yRot(i * step), i);
                if (e == null) continue;
                e.setPos(boss.getX(), boss.getY(0.6), boss.getZ());
                level.addFreshEntity(e);
            }
        }

        @Nullable
        private static Entity arrow(Level level, Mob boss, double dx, double dy, double dz, float speed, float inaccuracy) {
            Arrow a = EntityType.ARROW.create(level);
            if (a == null) return null;
            a.setOwner(boss);
            a.pickup = AbstractArrow.Pickup.DISALLOWED;
            a.setBaseDamage(3.5);
            a.setCritArrow(true);
            a.shoot(dx, dy, dz, speed, inaccuracy);
            return a;
        }

        private void clearWebs(ServerLevel sl, boolean force) {
            for (int i = webs.size() - 1; i >= 0; i--) {
                if (force || sl.getGameTime() >= webExpiry.get(i)) {
                    BlockPos p = webs.get(i);
                    if (sl.getBlockState(p).is(Blocks.COBWEB)) sl.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    webs.remove(i);
                    webExpiry.remove(i);
                }
            }
        }
    }
}
