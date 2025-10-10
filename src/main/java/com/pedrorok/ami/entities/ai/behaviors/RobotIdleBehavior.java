package com.pedrorok.ami.entities.ai.behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class RobotIdleBehavior extends Behavior<LivingEntity> {
	private static final int IDLE_DURATION = 200; // 10 seconds
	
	public RobotIdleBehavior() {
		super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT), IDLE_DURATION);
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
		// Only idle if we're not doing anything else
		return !entity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && !entity.getBrain().hasMemoryValue(MemoryModuleType.LOOK_TARGET);
	}
	
	@Override
	protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
		// Robot just stands still and looks around occasionally
		// This is a passive behavior, no specific actions needed
	}
	
	@Override
	protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
		return checkExtraStartConditions(level, entity);
	}
}
