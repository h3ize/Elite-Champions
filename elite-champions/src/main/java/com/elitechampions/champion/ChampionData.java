package com.elitechampions.champion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/** Champion state lives in the entity's persistent data, so it survives save/load without extra registries. */
public final class ChampionData {
    public static final String KEY = "elitechampions";

    private ChampionData() {}

    public static boolean isChampion(Entity e) {
        return e.getPersistentData().contains(KEY, Tag.TAG_COMPOUND);
    }

    private static CompoundTag root(Entity e) {
        return e.getPersistentData().getCompound(KEY);
    }

    public static ChampionRank getRank(Entity e) {
        return isChampion(e) ? ChampionRank.byId(root(e).getString("Rank")) : ChampionRank.COMMON;
    }

    public static boolean isPending(Entity e) {
        return isChampion(e) && root(e).getBoolean("Pending");
    }

    /** Marks a mob as a champion whose stats/gear get applied on its first tick (after vanilla finalizeSpawn). */
    public static void init(Entity e, ChampionRank rank) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Rank", rank.id());
        tag.putBoolean("Pending", true);
        e.getPersistentData().put(KEY, tag);
    }

    public static void finish(Entity e, List<ChampionAbility> abilities) {
        CompoundTag tag = root(e);
        tag.putBoolean("Pending", false);
        ListTag list = new ListTag();
        for (ChampionAbility a : abilities) list.add(StringTag.valueOf(a.id()));
        tag.put("Abilities", list);
    }

    public static List<ChampionAbility> abilities(Entity e) {
        List<ChampionAbility> out = new ArrayList<>();
        if (!isChampion(e)) return out;
        ListTag list = root(e).getList("Abilities", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ChampionAbility a = ChampionAbility.byId(list.getString(i));
            if (a != null) out.add(a);
        }
        return out;
    }

    public static boolean has(Entity e, ChampionAbility ability) {
        return abilities(e).contains(ability);
    }
}
