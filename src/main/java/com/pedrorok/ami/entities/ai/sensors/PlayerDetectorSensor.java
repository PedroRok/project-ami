package com.pedrorok.ami.entities.ai.sensors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public class PlayerDetectorSensor extends Sensor<LivingEntity> {
	private static final int DETECTION_RADIUS = 16;
	
	public PlayerDetectorSensor() {
		super();
	}
	
	@Override
	protected void doTick(ServerLevel level, LivingEntity entity) {
		Player nearestPlayer = level.getNearestPlayer(entity, DETECTION_RADIUS);
		if (nearestPlayer != null) {
			entity.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, nearestPlayer);
		} else {
			entity.getBrain().eraseMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
		}
	}
	
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return Set.of(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
	}
}
