package com.pedrorok.ami.config;

import com.pedrorok.ami.ProjectAmi;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ProjectAmi.MOD_ID)
public class Config {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	
	// CONFIGS
	
	public static final ModConfigSpec SPEC = BUILDER.build();
	
	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
	}
}
