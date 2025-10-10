package com.pedrorok.ami;

import com.mojang.logging.LogUtils;
import com.pedrorok.ami.config.Config;
import com.pedrorok.ami.registry.ModActivities;
import com.pedrorok.ami.registry.ModBlockEntities;
import com.pedrorok.ami.registry.ModBlocks;
import com.pedrorok.ami.registry.ModEntities;
import com.pedrorok.ami.registry.ModItems;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import com.pedrorok.ami.registry.ModSensorTypes;
import com.pedrorok.ami.worldgen.RobotPartPlacedFeature;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

@Mod(ProjectAmi.MOD_ID)
public class ProjectAmi {
    public static final String MOD_ID = "project_ami";
    private static final Logger LOGGER = LogUtils.getLogger();
	
	private static volatile MinecraftServerAudiences adventure;
    
    public ProjectAmi(IEventBus modEventBus, ModContainer container) {
	    container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
		
	    ModBlocks.BLOCKS.register(modEventBus);
	    ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
		ModEntities.ENTITIES.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);
		RobotPartPlacedFeature.PLACED_FEATURES.register(modEventBus);
		ModActivities.ACTIVITIES.register(modEventBus);
		ModMemoryModuleTypes.MEMORY_MODULES.register(modEventBus);
		ModSensorTypes.SENSOR_TYPES.register(modEventBus);
	 
		NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> adventure = MinecraftServerAudiences.of(event.getServer()));
	    NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> adventure = null);
    }
	
	public static MinecraftServerAudiences adventure() {
		if (adventure == null) {
			throw new IllegalStateException("Tried to access Adventure without a running server!");
		}
		return adventure;
	}
	
	public static ResourceLocation resource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
