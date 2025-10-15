package com.pedrorok.ami.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RobotPartFeature extends Feature<NoneFeatureConfiguration> {

    public RobotPartFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // Generate a random robot part
        //PartType partType = PartType.values()[random.nextInt(PartType.values().length)];

        // Create block state with the part type
        //BlockState blockState = ModBlocks.ROBOT_PART.get().defaultBlockState()
        //	.setValue(RobotPartBlock.PART_TYPE, partType);

        // Place the block
        //if (level.setBlock(pos, blockState, 2)) {
        // Set the block entity's part type
        //	if (level.getBlockEntity(pos) instanceof com.pedrorok.ami.blocks.robot_part.RobotPartBlockEntity blockEntity) {
        //		blockEntity.setPartType(partType);
        //	}
        //	return true;
        //}

        return false;
    }
}
