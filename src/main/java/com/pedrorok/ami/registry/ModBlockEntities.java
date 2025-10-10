package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.blocks.robot_part.RobotPartBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ProjectAmi.MOD_ID);
	
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RobotPartBlockEntity>> ROBOT_PART = BLOCK_ENTITIES.register("robot_part",
		() -> BlockEntityType.Builder.of(RobotPartBlockEntity::new, ModBlocks.ROBOT_PART.get()).build(null)
	);
}
