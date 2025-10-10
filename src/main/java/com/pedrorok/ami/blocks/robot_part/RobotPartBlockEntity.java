package com.pedrorok.ami.blocks.robot_part;

import com.pedrorok.ami.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RobotPartBlockEntity extends BlockEntity {
	public RobotPartBlockEntity(BlockPos pos, BlockState blockState) {
		super(ModBlockEntities.ROBOT_PART.get(), pos, blockState);
	}
}
