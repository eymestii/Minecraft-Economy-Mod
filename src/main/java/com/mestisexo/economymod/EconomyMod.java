package com.mestisexo.economymod;

import com.mestisexo.economymod.command.EconomyCommands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(EconomyMod.MOD_ID)
public class EconomyMod {
    public static final String MOD_ID = "economymod";

    public EconomyMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.addListener(EconomyCommands::register);
    }
}
