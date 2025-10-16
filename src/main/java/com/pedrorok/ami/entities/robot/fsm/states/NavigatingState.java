package com.pedrorok.ami.entities.robot.fsm.states;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.fsm.ActionContext;
import com.pedrorok.ami.entities.robot.fsm.ActionState;
import com.pedrorok.ami.entities.robot.fsm.StateHandler;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

@Slf4j
@RequiredArgsConstructor
public class NavigatingState implements StateHandler {
    private final RobotEntity robot;

    private int ticksInNavigating = 0;
    private BlockPos lastPos = null;
    private int ticksStuck = 0;
    private static final int MAX_NAVIGATION_TIME = 600;
    private static final int STUCK_CHECK_INTERVAL = 20;

    @Override
    public void enter(ActionContext context) {
        log.info("Entering NAVIGATING state");

        ticksInNavigating = 0;
        lastPos = robot.blockPosition();
        ticksStuck = 0;

        MiningTaskData miningTask = robot.getBrain()
            .getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
            .filter(task -> task instanceof MiningTaskData)
            .map(task -> (MiningTaskData) task)
            .orElse(null);

        if (miningTask != null && miningTask.getStartPos() != null) {
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(miningTask.getStartPos(), 1.0F, 0));
        }
    }
    
    @Override
    public ActionState tick(ActionContext context) {
        if (needsTool()) {
            return ActionState.REQUEST_TOOL;
        }

        ticksInNavigating++;

        MiningTaskData miningTask = robot.getBrain()
            .getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
            .filter(task -> task instanceof MiningTaskData)
            .map(task -> (MiningTaskData) task)
            .orElse(null);

        if (miningTask == null) {
            return ActionState.IDLE;
        }

        if (miningTask.getPhase() != MiningTaskData.MiningPhase.NAVIGATING) {
            return ActionState.IDLE;
        }

        BlockPos startPos = miningTask.getStartPos();
        if (startPos == null) {
            return ActionState.IDLE;
        }

        if (ticksInNavigating % STUCK_CHECK_INTERVAL == 0) {
            BlockPos currentPos = robot.blockPosition();
            if (currentPos.equals(lastPos)) {
                ticksStuck += STUCK_CHECK_INTERVAL;
                log.debug("Robot hasn't moved in {} ticks", ticksStuck);
            } else {
                ticksStuck = 0;
            }
            lastPos = currentPos;
        }

        if (ticksInNavigating >= MAX_NAVIGATION_TIME || ticksStuck >= 100) {
            log.warn("Navigation timeout or stuck ({} ticks total, {} stuck), starting mining from current position",
                ticksInNavigating, ticksStuck);
            miningTask.setPhase(MiningTaskData.MiningPhase.MINING);
            return ActionState.MINING;
        }

        double distance = robot.distanceToSqr(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5);
        if (distance <= 64.0) {
            log.info("Robot reached start position (distance: {}), transitioning to MINING", Math.sqrt(distance));
            miningTask.setPhase(MiningTaskData.MiningPhase.MINING);
            return ActionState.MINING;
        }

        return ActionState.NAVIGATING;
    }
    
    @Override
    public void exit(ActionContext context) {
        log.debug("Exiting NAVIGATING state");
    }
    
    private boolean needsTool() {
        return !(robot.getMainHandItem().getItem() instanceof net.minecraft.world.item.DiggerItem);
    }
}
