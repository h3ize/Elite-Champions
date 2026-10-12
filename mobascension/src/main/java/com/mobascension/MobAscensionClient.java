package com.mobascension;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client-only entry point: adds the in-game config GUI (Mods -> Mob Ascension -> Config). */
@Mod(value = MobAscension.MODID, dist = Dist.CLIENT)
public class MobAscensionClient {
    public MobAscensionClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
