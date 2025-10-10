package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.blocks.robot_part.RobotPartBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ProjectAmi.MOD_ID);
	
	public static final DeferredHolder<Block, RobotPartBlock> ROBOT_PART = BLOCKS.register("robot_part",
		() -> new RobotPartBlock(Block.Properties.of().strength(1.5f).noOcclusion())
	);
}
