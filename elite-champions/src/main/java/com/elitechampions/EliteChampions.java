package com.elitechampions;

import com.elitechampions.config.ChampionConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(EliteChampions.MODID)
public class EliteChampions {
    public static final String MODID = "elitechampions";

    public EliteChampions(IEventBus modBus, ModContainer container) {
        ModItems.ITEMS.register(modBus);
        ModItems.TABS.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, ChampionConfig.SPEC);
    }
}
