package com.elitechampions;

import com.elitechampions.champion.ChampionRank;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EliteChampions.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EliteChampions.MODID);

    public static final Map<ChampionRank, DeferredItem<? extends Item>> ESSENCE = new EnumMap<>(ChampionRank.class);

    static {
        for (ChampionRank rank : ChampionRank.champions()) {
            ESSENCE.put(rank, ITEMS.registerItem(
                    "champion_essence_" + rank.id(),
                    props -> new EssenceItem(props),
                    new Item.Properties().rarity(rank.itemRarity())));
        }
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + EliteChampions.MODID))
                    .icon(() -> new ItemStack(ESSENCE.get(ChampionRank.LEGENDARY).get()))
                    .displayItems((params, output) -> {
                        for (ChampionRank rank : ChampionRank.champions()) {
                            output.accept(ESSENCE.get(rank).get());
                        }
                    })
                    .build());

    private ModItems() {}
}
