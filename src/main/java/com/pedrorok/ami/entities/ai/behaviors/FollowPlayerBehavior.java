package com.pedrorok.ami.entities.ai.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class FollowPlayerBehavior extends Behavior<LivingEntity> {
	private static final double FOLLOW_DISTANCE = 3.0;
	private static final double MAX_DISTANCE = 16.0;
	
	public FollowPlayerBehavior() {
		super(Map.of(MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
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
		// This will be called when the behavior starts
	}
	
	@Override
	protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
		Player nearestPlayer = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
		if (nearestPlayer == null) {
			return;
		}
		double distance = entity.distanceTo(nearestPlayer);
		// Check if player is too far away
		if (distance > MAX_DISTANCE) {
			return;
		}
		// Check if we're close enough
		if (distance <= FOLLOW_DISTANCE) {
			return;
		}
		// Move towards the player
		BlockPos targetPos = nearestPlayer.blockPosition();
		entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 1.0f, 8));
		// Consume energy if robot has energy system
		if (entity instanceof com.pedrorok.ami.entities.RobotEntity robot) {
			robot.consumeEnergy(1); // 1 energy per tick when following
		}
	}
	
	@Override
	protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
		return checkExtraStartConditions(level, entity);
	}
	
	@Override
	protected void stop(ServerLevel level, LivingEntity entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
	}
}