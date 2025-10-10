package com.pedrorok.ami.entities.ai.sensors;

import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public class ChatCommandSensor extends Sensor<LivingEntity> {
    private static final int DETECTION_RADIUS = 8;
    
    public ChatCommandSensor() {
        super();
    }
    
    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        // Find nearest player
        Player nearestPlayer = level.getNearestPlayer(entity, DETECTION_RADIUS);
        
        if (nearestPlayer != null) {
            // Store player in memory for potential interaction
            entity.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, nearestPlayer);
            
            // TODO: Check for chat commands from this player
            // This would integrate with the chat system to detect commands
            // For now, we'll use a simple proximity-based approach
        } else {
            entity.getBrain().eraseMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        }
    }
    
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
    }
}
