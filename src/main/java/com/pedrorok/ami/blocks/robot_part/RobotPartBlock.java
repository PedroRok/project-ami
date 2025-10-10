package com.pedrorok.ami.blocks.robot_part;

import com.mojang.serialization.MapCodec;
import com.pedrorok.ami.blocks.robot_parts.PartType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobotPartBlock extends BaseEntityBlock {
	public static final MapCodec<RobotPartBlock> CODEC = simpleCodec(RobotPartBlock::new);
	public static final EnumProperty<PartType> PART_TYPE = EnumProperty.create("part_type", PartType.class);
	
	public RobotPartBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(PART_TYPE, PartType.HEAD));
	}
	
	@Override
	protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PART_TYPE);
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
