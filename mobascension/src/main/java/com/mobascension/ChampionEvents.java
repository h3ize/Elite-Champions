package com.mobascension;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ChampionEvents {

    // ------------------------------------------------------------------ spawning

    /** Decides (once) whether a freshly spawned mob becomes a champion. */
    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (mob.level().isClientSide) return;
        if (ChampionData.getPendingRank(mob) != null) return; // already decided (e.g. command)
        if (AscensionConfig.EXCLUDED_SPAWN_TYPES.get().contains(event.getSpawnType().name())) return;
        if (!ChampionFactory.isEligible(mob)) return;

        Rank rank = ChampionFactory.roll(mob.getRandom());
        if (rank != null) {
            mob.getPersistentData().putString(ChampionData.RANK, rank.id);
        }
    }

    /** Applies the champion once the mob actually enters the world (after vanilla equipment is set). */
    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) return;
        Rank rank = ChampionData.getPendingRank(mob);
        if (rank == null || mob.getPersistentData().getBoolean(ChampionData.APPLIED)) return;
        mob.getPersistentData().putBoolean(ChampionData.APPLIED, true);
        ChampionFactory.apply(mob, level, rank);
    }

    // ------------------------------------------------------------------ ticking

    @SubscribeEvent
    public void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(mob.level() instanceof ServerLevel level) || !mob.isAlive()) return;
        Rank rank = ChampionData.getRank(mob);
        if (rank == null) return;
        AbilityLogic.tick(mob, level, rank, ChampionData.getAbilities(mob));
    }

    // ------------------------------------------------------------------ combat

    @SubscribeEvent
    public void onDamagePre(LivingDamageEvent.Pre event) {
        if (!AscensionConfig.SCALE_RANGED_DAMAGE.get()) return;
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Mob attacker)) return;
        if (source.getDirectEntity() == attacker || !source.is(DamageTypeTags.IS_PROJECTILE)) return;
        Rank rank = ChampionData.getRank(attacker);
        if (rank == null) return;
        event.setNewDamage((float) (event.getNewDamage() * ChampionFactory.damageMultiplier(rank)));
    }

    @SubscribeEvent
    public void onDamagePost(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getNewDamage();
        if (damage <= 0 || victim.level().isClientSide) return;

        // champion attacking something
        if (source.getEntity() instanceof Mob attacker && ChampionData.getRank(attacker) != null) {
            AbilityLogic.onAttack(attacker, victim, damage, ChampionData.getAbilities(attacker));
        }

        // champion being hit by melee
        if (victim instanceof Mob champion && ChampionData.getRank(champion) != null
                && source.getEntity() instanceof LivingEntity attacker
                && source.getDirectEntity() == attacker && !source.is(DamageTypes.THORNS)) {
            AbilityLogic.onDefend(champion, attacker, damage, ChampionData.getAbilities(champion));
        }
    }

    // ------------------------------------------------------------------ death

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        Rank rank = ChampionData.getRank(mob);
        if (rank == null) return;

        AbilityLogic.onDeath(mob, level, ChampionData.getAbilities(mob));

        // experience levels straight to the killer
        if (!ChampionData.isMinion(mob) && mob.getKillCredit() instanceof ServerPlayer killer) {
            int levels = AscensionConfig.rank(rank).killLevels.get();
            if (levels > 0) killer.giveExperienceLevels(levels);
        }

        if (AscensionConfig.rank(rank).fireworks.get() && mob.getKillCredit() instanceof Player) {
            launchFireworks(level, mob, rank);
        }
    }

    private static void launchFireworks(ServerLevel level, Mob mob, Rank rank) {
        for (int i = 0; i < 3; i++) {
            ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
            IntList colors = new IntArrayList(new int[]{rank.color, 0xFFFFFF});
            FireworkExplosion explosion = new FireworkExplosion(
                    i == 1 ? FireworkExplosion.Shape.STAR : FireworkExplosion.Shape.LARGE_BALL,
                    colors, new IntArrayList(), true, true);
            rocket.set(DataComponents.FIREWORKS, new Fireworks(1 + i % 2, List.of(explosion)));
            double dx = (mob.getRandom().nextDouble() - 0.5) * 3;
            double dz = (mob.getRandom().nextDouble() - 0.5) * 3;
            level.addFreshEntity(new FireworkRocketEntity(level, mob.getX() + dx, mob.getY(), mob.getZ() + dz, rocket));
        }
    }

    @SubscribeEvent
    public void onXp(LivingExperienceDropEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        Rank rank = ChampionData.getRank(mob);
        if (ChampionData.isMinion(mob) && !AscensionConfig.MINIONS_DROP_LOOT.get()) {
            event.setDroppedExperience(0);
            return;
        }
        if (rank == null) return;
        event.setDroppedExperience((int) (event.getDroppedExperience() * AscensionConfig.rank(rank).xp.get()));
    }

    // ------------------------------------------------------------------ loot

    @SubscribeEvent
    public void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;

        if (ChampionData.isMinion(mob)) {
            if (!AscensionConfig.MINIONS_DROP_LOOT.get()) event.getDrops().clear();
            return;
        }
        Rank rank = ChampionData.getRank(mob);
        if (rank == null) return;
        if (AscensionConfig.REQUIRE_PLAYER_KILL.get() && !(mob.getKillCredit() instanceof Player)) return;

        AscensionConfig.RankSettings cfg = AscensionConfig.rank(rank);
        RandomSource random = mob.getRandom();

        // 1a) the held weapon (only gear given by this mod)
        ItemStack weapon = mob.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!weapon.isEmpty() && isModGear(weapon)) {
            double chance = RelicFactory.isRelic(weapon) ? cfg.relicDrop.get() : cfg.weaponDrop.get();
            if (random.nextDouble() < chance) drop(event, mob, prepareDrop(weapon, random));
        }

        // 1b) at most ONE random worn armor piece
        List<EquipmentSlot> worn = new ArrayList<>();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack piece = mob.getItemBySlot(slot);
            if (!piece.isEmpty() && isModGear(piece)) worn.add(slot);
        }
        if (!worn.isEmpty()) {
            ItemStack piece = mob.getItemBySlot(worn.get(random.nextInt(worn.size())));
            double chance = RelicFactory.isRelic(piece) ? cfg.relicDrop.get() : cfg.armorDrop.get();
            if (random.nextDouble() < chance) drop(event, mob, prepareDrop(piece, random));
        }

        // 2) a bonus relic, independent of what was worn
        if (random.nextDouble() < cfg.bonusRelic.get()) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                    EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
            EquipmentSlot slot = slots[random.nextInt(slots.length)];
            ItemStack base = ChampionFactory.pickItem(cfg, slot, random);
            if (base != null) {
                drop(event, mob, RelicFactory.create(base, rank, slot, level.registryAccess(), random));
            }
        }

        // 3) configured bonus loot
        for (String line : cfg.bonusLoot.get()) {
            String[] p = line.split("\\|");
            if (p.length < 4) continue;
            try {
                ResourceLocation id = ResourceLocation.tryParse(p[0].trim());
                if (id == null) continue;
                Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
                if (item == Items.AIR) continue;
                double chance = Double.parseDouble(p[1].trim());
                int min = Integer.parseInt(p[2].trim());
                int max = Math.max(min, Integer.parseInt(p[3].trim()));
                if (random.nextDouble() < chance) {
                    int amount = min + random.nextInt(max - min + 1);
                    if (amount > 0) drop(event, mob, new ItemStack(item, amount));
                }
            } catch (NumberFormatException ignored) {
                // malformed config line, skip it
            }
        }
    }

    /** Copy of the worn item; normal gear is cleaned of marker data and dropped partly used, like vanilla. */
    private static ItemStack prepareDrop(ItemStack worn, RandomSource random) {
        ItemStack copy = worn.copy();
        if (!RelicFactory.isRelic(copy)) {
            copy.remove(DataComponents.CUSTOM_DATA);
            if (copy.isDamageableItem()) {
                copy.setDamageValue((int) (copy.getMaxDamage() * (0.2 + 0.6 * random.nextDouble())));
            }
        }
        return copy;
    }

    private static boolean isModGear(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains(ChampionFactory.GEAR_TAG);
    }

    private static void drop(LivingDropsEvent event, Entity mob, ItemStack stack) {
        event.getDrops().add(new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), stack));
    }
}
