package com.pedrorok.ami.entities.ai.behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class RobotWanderBehavior extends Behavior<LivingEntity> {
	private static final int WANDER_RADIUS = 8;
	private static final int WANDER_INTERVAL = 1200; // 60 seconds
	
	public RobotWanderBehavior() {
		super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), WANDER_INTERVAL);
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
		// Don't wander if we're already following a player
		return !entity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET);
	}
	
	@Override
	protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
		// Simple wander behavior - just set a random walk target
		RandomStroll.stroll((float) WANDER_RADIUS, false);
	}
	
	@Override
	protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
		return checkExtraStartConditions(level, entity);
	}
}