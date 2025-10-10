package com.pedrorok.ami.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RobotPartPlacedFeature {
	public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registries.PLACED_FEATURE, "project_ami");
	
	/*public static final DeferredHolder<PlacedFeature, PlacedFeature> ROBOT_PART_RARE = PLACED_FEATURES.register("robot_part_rare",
		() -> new PlacedFeature(
			ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath("project_ami", "robot_part")),
			java.util.List.of(
				net.minecraft.world.level.levelgen.placement.CountPlacement.of(1), // 1 per chunk
				net.minecraft.world.level.levelgen.placement.InSquarePlacement.spread(),
				net.minecraft.world.level.levelgen.placement.HeightRangePlacement.uniform(
					net.minecraft.world.level.levelgen.VerticalAnchor.absolute(10),
					net.minecraft.world.level.levelgen.VerticalAnchor.absolute(60)
				),
				net.minecraft.world.level.levelgen.placement.BiomeFilter.biome()
			)
		)
	);*/
}
