package com.elitechampions;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Registers the in-game config GUI (Mods -> Elite Champions -> Config). */
@Mod(value = EliteChampions.MODID, dist = Dist.CLIENT)
public class EliteChampionsClient {
    public EliteChampionsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
