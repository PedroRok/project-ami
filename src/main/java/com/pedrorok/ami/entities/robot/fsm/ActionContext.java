package com.pedrorok.ami.entities.robot.fsm;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.pathfinding.mining.MiningPathPlan;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ActionContext {
    private final ServerLevel level;
    private final long gameTime;
    private final MiningTaskData miningTask;
    private final MiningPathPlan miningPlan;
    private final BlockPos currentTarget;
    private final int ticksInState;
    private final boolean needsTool;
    private final RobotEntity robot;
    
    public MiningPathPlan getMiningPlan() {
        return robot.getBrain()
            .getMemory(ModMemoryModuleTypes.MINING_PLAN.get())
            .map(plan -> (MiningPathPlan) plan)
            .orElse(null);
    }
    
    public MiningTaskData getMiningTask() {
        return robot.getBrain()
            .getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
            .map(task -> (MiningTaskData) task)
            .orElse(null);
    }
    
    public ActionContext withTicksInState(int ticks) {
        return ActionContext.builder()
			.robot(robot)
            .level(this.level)
            .gameTime(this.gameTime)
            .miningTask(this.miningTask)
            .miningPlan(this.miningPlan)
            .currentTarget(this.currentTarget)
            .ticksInState(ticks)
            .needsTool(this.needsTool)
            .build();
    }
    
    public ActionContext withCurrentTarget(BlockPos target) {
        return ActionContext.builder()
			.robot(robot)
            .level(this.level)
            .gameTime(this.gameTime)
            .miningTask(this.miningTask)
            .miningPlan(this.miningPlan)
            .currentTarget(target)
            .ticksInState(this.ticksInState)
            .needsTool(this.needsTool)
            .build();
    }
}
