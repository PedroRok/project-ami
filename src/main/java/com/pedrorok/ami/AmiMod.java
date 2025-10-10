package com.pedrorok.ami;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(AmiMod.MOD_ID)
public class AmiMod {
    public static final String MOD_ID = "project_ami";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public AmiMod(IEventBus modEventBus, ModContainer modContainer) {
		
	    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
