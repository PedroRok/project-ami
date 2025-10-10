package com.pedrorok.ami.entities.ai.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class MiningBehavior extends Behavior<LivingEntity> {
    private static final int MINING_RADIUS = 3;
    private static final int MAX_BLOCKS_TO_MINE = 10;
    
    private int blocksMined = 0;
    private Direction miningDirection = Direction.NORTH;
    
    public MiningBehavior() {
        super(Map.of(
            MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }
    
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
        // Check if entity has a pickaxe
        ItemStack heldItem = entity.getMainHandItem();
        if (!(heldItem.getItem() instanceof PickaxeItem)) {
            return false;
        }
        
        // Check if we have energy (if robot has energy system)
        if (entity instanceof com.pedrorok.ami.entities.RobotEntity robot) {
            if (robot.isOutOfEnergy()) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        blocksMined = 0;
        
        // Find a good mining spot
        BlockPos startPos = entity.blockPosition();
        BlockPos targetPos = findMiningTarget(level, startPos);
        
        if (targetPos != null) {
            // Set walk target to mining location
            entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, 
                new WalkTarget(targetPos, 1.0f, 8));
        }
    }
    
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        if (blocksMined >= MAX_BLOCKS_TO_MINE) {
            return; // Stop mining
        }
        
        BlockPos currentPos = entity.blockPosition();
        BlockPos targetPos = currentPos.relative(miningDirection);
        
        // Check if we're close enough to mine
        if (entity.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) <= 4.0) {
            BlockState targetState = level.getBlockState(targetPos);
            
            if (!targetState.isAir() && !targetState.is(Blocks.BEDROCK)) {
                // Mine the block
                if (level.destroyBlock(targetPos, true)) {
                    blocksMined++;
                    
                    // Consume energy if robot has energy system
                    if (entity instanceof com.pedrorok.ami.entities.RobotEntity robot) {
                        robot.consumeEnergy(5); // 5 energy per block
                    }
                    
                    // Move to next block
                    BlockPos nextPos = targetPos.relative(miningDirection);
                    entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, 
                        new WalkTarget(nextPos, 1.0f, 8));
                }
            }
        }
    }
    
    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        return blocksMined < MAX_BLOCKS_TO_MINE && checkExtraStartConditions(level, entity);
    }
    
    @Override
    protected void stop(ServerLevel level, LivingEntity entity, long gameTime) {
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        blocksMined = 0;
    }
    
    private BlockPos findMiningTarget(ServerLevel level, BlockPos startPos) {
        // Simple algorithm to find a good mining spot
        for (int x = -MINING_RADIUS; x <= MINING_RADIUS; x++) {
            for (int z = -MINING_RADIUS; z <= MINING_RADIUS; z++) {
                BlockPos pos = startPos.offset(x, 0, z);
                BlockState state = level.getBlockState(pos);
                
                if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                    return pos;
                }
            }
        }
        
        return null;
    }
}
