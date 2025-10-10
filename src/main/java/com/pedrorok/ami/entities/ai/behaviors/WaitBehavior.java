package com.pedrorok.ami.entities.ai.behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class WaitBehavior extends Behavior<LivingEntity> {
    private int waitTicks = 0;
    private int maxWaitTicks = 100; // 5 seconds default
    
    public WaitBehavior() {
        super(Map.of(
            MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }
    
    public WaitBehavior(int maxWaitTicks) {
        this();
        this.maxWaitTicks = maxWaitTicks;
    }
    
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
        return true; // Always can wait
    }
    
    @Override
    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        waitTicks = 0;
    }
    
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        waitTicks++;
        
        // Just stand still and do nothing
        // This is a passive behavior
    }
    
    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        return waitTicks < maxWaitTicks;
    }
    
    @Override
    protected void stop(ServerLevel level, LivingEntity entity, long gameTime) {
        waitTicks = 0;
    }
}
