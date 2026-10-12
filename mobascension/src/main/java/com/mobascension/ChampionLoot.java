package com.mobascension;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the per-rank kill rewards from the config:
 * guaranteedLoot (always), rewardPool (guaranteed weighted rolls) and bonusLoot (chance based).
 * Line format: item_id|chanceOrWeight|min|max[|enchant=level,enchant=level]
 */
public final class ChampionLoot {
    private ChampionLoot() {}

    public static List<ItemStack> roll(ServerLevel level, Rank rank, RandomSource random) {
        AscensionConfig.RankSettings cfg = AscensionConfig.rank(rank);
        RegistryAccess access = level.registryAccess();
        List<ItemStack> out = new ArrayList<>();

        for (String line : cfg.guaranteedLoot.get()) {
            Entry e = Entry.parse(line);
            if (e == null) continue;
            ItemStack stack = e.create(access, random);
            if (!stack.isEmpty()) out.add(stack);
        }

        List<Entry> pool = new ArrayList<>();
        double total = 0;
        for (String line : cfg.rewardPool.get()) {
            Entry e = Entry.parse(line);
            if (e == null || e.value <= 0) continue;
            pool.add(e);
            total += e.value;
        }
        int min = cfg.rewardRollsMin.get();
        int max = Math.max(min, cfg.rewardRollsMax.get());
        int rolls = min + random.nextInt(max - min + 1);
        if (total > 0) {
            for (int i = 0; i < rolls; i++) {
                double pick = random.nextDouble() * total;
                Entry chosen = pool.get(pool.size() - 1);
                for (Entry e : pool) {
                    pick -= e.value;
                    if (pick < 0) { chosen = e; break; }
                }
                ItemStack stack = chosen.create(access, random);
                if (!stack.isEmpty()) out.add(stack);
            }
        }

        for (String line : cfg.bonusLoot.get()) {
            Entry e = Entry.parse(line);
            if (e == null) continue;
            if (random.nextDouble() < e.value) {
                ItemStack stack = e.create(access, random);
                if (!stack.isEmpty()) out.add(stack);
            }
        }
        return out;
    }

    /** The champion's own head, rolled against the rank's headDropChance. Empty if the mob has no head item. */
    public static ItemStack head(Mob mob, Rank rank, RandomSource random) {
        if (random.nextDouble() >= AscensionConfig.rank(rank).headChance.get()) return ItemStack.EMPTY;
        EntityType<?> type = mob.getType();
        if (type == EntityType.ZOMBIE) return new ItemStack(Items.ZOMBIE_HEAD);
        if (type == EntityType.SKELETON) return new ItemStack(Items.SKELETON_SKULL);
        if (type == EntityType.WITHER_SKELETON) return new ItemStack(Items.WITHER_SKELETON_SKULL);
        if (type == EntityType.CREEPER) return new ItemStack(Items.CREEPER_HEAD);
        if (type == EntityType.PIGLIN) return new ItemStack(Items.PIGLIN_HEAD);
        return ItemStack.EMPTY;
    }

    /** Apex weapon drop alternative: a bow with Power 5 (sometimes 6), Unbreaking 3 and Mending. */
    public static ItemStack apexBow(ServerLevel level, RandomSource random) {
        ItemStack bow = new ItemStack(Items.BOW);
        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        registry.getHolder(net.minecraft.world.item.enchantment.Enchantments.POWER)
                .ifPresent(h -> bow.enchant(h, 5 + random.nextInt(2)));
        registry.getHolder(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING).ifPresent(h -> bow.enchant(h, 3));
        registry.getHolder(net.minecraft.world.item.enchantment.Enchantments.MENDING).ifPresent(h -> bow.enchant(h, 1));
        return bow;
    }

    private static final class Entry {
        final Item item;
        final double value;
        final int min;
        final int max;
        final Map<String, Integer> enchants;

        private Entry(Item item, double value, int min, int max, Map<String, Integer> enchants) {
            this.item = item;
            this.value = value;
            this.min = min;
            this.max = max;
            this.enchants = enchants;
        }

        static Entry parse(String line) {
            try {
                String[] p = line.split("\\|");
                if (p.length < 4) return null;
                ResourceLocation id = ResourceLocation.tryParse(p[0].trim());
                if (id == null) return null;
                Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
                if (item == Items.AIR) return null;
                double value = Double.parseDouble(p[1].trim());
                int min = Integer.parseInt(p[2].trim());
                int max = Math.max(min, Integer.parseInt(p[3].trim()));
                Map<String, Integer> enchants = new LinkedHashMap<>();
                if (p.length >= 5 && !p[4].isBlank()) {
                    for (String part : p[4].split(",")) {
                        String[] kv = part.split("=");
                        if (kv.length != 2) continue;
                        enchants.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
                    }
                }
                return new Entry(item, value, min, max, enchants);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        ItemStack create(RegistryAccess access, RandomSource random) {
            int amount = min + random.nextInt(max - min + 1);
            if (amount <= 0) return ItemStack.EMPTY;
            ItemStack stack = new ItemStack(item, 1);
            stack.setCount(Math.min(amount, stack.getMaxStackSize()));
            if (!enchants.isEmpty()) applyEnchants(stack, access);
            return stack;
        }

        private void applyEnchants(ItemStack stack, RegistryAccess access) {
            Registry<Enchantment> registry = access.registryOrThrow(Registries.ENCHANTMENT);
            boolean book = stack.is(Items.ENCHANTED_BOOK);
            List<Holder<Enchantment>> applied = new ArrayList<>();
            for (Map.Entry<String, Integer> en : enchants.entrySet()) {
                String name = en.getKey().contains(":") ? en.getKey() : "minecraft:" + en.getKey();
                ResourceLocation id = ResourceLocation.tryParse(name);
                if (id == null) continue;
                Optional<Holder.Reference<Enchantment>> ref =
                        registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, id));
                if (ref.isEmpty()) continue;
                Holder<Enchantment> holder = ref.get();
                if (!book && !holder.value().canEnchant(stack)) continue;
                boolean compatible = true;
                for (Holder<Enchantment> other : applied) {
                    if (!Enchantment.areCompatible(holder, other)) { compatible = false; break; }
                }
                if (!compatible) continue;
                int lvl = Math.max(1, Math.min(en.getValue(), holder.value().getMaxLevel()));
                stack.enchant(holder, lvl);
                applied.add(holder);
            }
        }
    }
}
