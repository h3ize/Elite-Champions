package com.mobascension;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(MobAscension.MODID)
public class MobAscension {
    public static final String MODID = "mobascension";

    public MobAscension(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, AscensionConfig.SPEC);
        NeoForge.EVENT_BUS.register(new ChampionEvents());
        NeoForge.EVENT_BUS.addListener(MobAscension::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        AscensionCommand.register(event.getDispatcher());
    }
}
