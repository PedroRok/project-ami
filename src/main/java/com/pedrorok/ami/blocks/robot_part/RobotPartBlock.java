package com.pedrorok.ami.blocks.robot_part;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobotPartBlock extends BaseEntityBlock {
	public static final MapCodec<RobotPartBlock> CODEC = simpleCodec(RobotPartBlock::new);
	
	public RobotPartBlock(Properties properties) {
		super(properties);
	}
	
	@Override
	protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new RobotPartBlockEntity(blockPos, blockState);
	}
	
	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
}
