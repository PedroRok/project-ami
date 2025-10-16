package com.pedrorok.ami.entities.robot.fsm.states;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.fsm.ActionContext;
import com.pedrorok.ami.entities.robot.fsm.ActionState;
import com.pedrorok.ami.entities.robot.fsm.StateHandler;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.pathfinding.mining.MiningPathPlan;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class MiningState implements StateHandler {
    private final RobotEntity robot;

    private int breakingProgress = 0;
    private BlockPos lastAttemptedBlock = null;
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int BREAK_TIME = 20;
    private Set<BlockPos> temporarilyBlocked = new HashSet<>();
    private Map<BlockPos, Boolean> lineOfSightCache = new HashMap<>();
    private int cacheInvalidationTick = 0;
    private int lastCompletedCount = 0;
    private int consecutiveBlockedAttempts = 0;
    private static final int MAX_CONSECUTIVE_BLOCKED = 15;
    
    @Override
    public void enter(ActionContext context) {
        log.info("Entering MINING state");
        breakingProgress = 0;
        failedAttempts = 0;
    }
    
    @Override
    public ActionState tick(ActionContext context) {
        if (needsTool()) {
            return ActionState.REQUEST_TOOL;
        }
        
        MiningTaskData miningTask = context.getMiningTask();
        if (miningTask == null || miningTask.isComplete()) {
            completeMining();
            return ActionState.IDLE;
        }
        
        MiningPathPlan plan = context.getMiningPlan();
        if (plan == null) {
            log.warn("No mining plan available, returning to planning");
            miningTask.setPhase(MiningTaskData.MiningPhase.PLANNING);
            return ActionState.PLANNING;
        }
        
        if (plan.isComplete()) {
            completeMining();
            return ActionState.IDLE;
        }
        
        BlockPos currentTarget = plan.getNextBlockForRobot(robot, 20, temporarilyBlocked);

        if (currentTarget == null) {
            log.warn("No accessible blocks remaining");
            completeMining();
            return ActionState.IDLE;
        }

        if (!currentTarget.equals(lastAttemptedBlock)) {
            if (!hasLineOfSight(currentTarget)) {
                log.debug("Block {} has no line of sight, marking as temporarily blocked", currentTarget);
                temporarilyBlocked.add(currentTarget);
                consecutiveBlockedAttempts++;

                if (consecutiveBlockedAttempts >= MAX_CONSECUTIVE_BLOCKED) {
                    log.info("Too many consecutive blocked blocks ({}), clearing blacklist to retry", consecutiveBlockedAttempts);
                    temporarilyBlocked.clear();
                    consecutiveBlockedAttempts = 0;
                    lineOfSightCache.clear();
                }

                if (temporarilyBlocked.size() > 50) {
                    temporarilyBlocked.clear();
                }

                return ActionState.MINING;
            }

            breakingProgress = 0;
            lastAttemptedBlock = currentTarget;
            failedAttempts = 0;
            consecutiveBlockedAttempts = 0;
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(currentTarget, 1.0F, 0));
        }
        
        double distance = robot.distanceToSqr(
            currentTarget.getX() + 0.5, 
            currentTarget.getY() + 0.5, 
            currentTarget.getZ() + 0.5
        );
        
        if (distance > 9.0) {
            breakingProgress = 0;
            failedAttempts++;
            if (failedAttempts > 100) {
                log.warn("Robot stuck trying to reach {}, marking as temporarily blocked", currentTarget);
                temporarilyBlocked.add(currentTarget);
                lastAttemptedBlock = null;
                failedAttempts = 0;
                
                if (temporarilyBlocked.size() > 50) {
                    temporarilyBlocked.clear();
                }
            }
            return ActionState.MINING;
        }
        
        if (!hasLineOfSight(currentTarget)) {
            log.debug("No line of sight to {}, marking as temporarily blocked", currentTarget);
            temporarilyBlocked.add(currentTarget);
            lastAttemptedBlock = null;
            breakingProgress = 0;
            
            if (temporarilyBlocked.size() > 50) {
                temporarilyBlocked.clear();
            }
            
            return ActionState.MINING;
        }
        
        failedAttempts = 0;
        breakingProgress++;
        
        if (breakingProgress == BREAK_TIME - 4) {
            robot.playDialogueAnimation("use-tool");
        }
        
        if (breakingProgress >= BREAK_TIME) {
            log.info("Breaking block: {}", currentTarget);
            robot.level().destroyBlock(currentTarget, true);

            consumeToolDurability();
            plan.markBlockCompletedAt(currentTarget);
            miningTask.incrementProgress(robot);

            breakingProgress = 0;
            lastAttemptedBlock = null;
            temporarilyBlocked.remove(currentTarget);

            int currentCompleted = plan.getCompletedBlocks();
            if (currentCompleted > 0 && currentCompleted % 9 == 0 && currentCompleted != lastCompletedCount) {
                log.info("Completed layer ({} blocks), clearing blacklist for next layer", currentCompleted);
                temporarilyBlocked.clear();
                lineOfSightCache.clear();
                consecutiveBlockedAttempts = 0;
                lastCompletedCount = currentCompleted;
            }

            if (plan.getCompletedBlocks() % 5 == 0) {
                String message = String.format("<green>[A.M.I.]</green> <gray>Progress: %d/%d blocks</gray>",
                    plan.getCompletedBlocks(), plan.getTotalBlocks());
                sendMessageToOwner(message);
            }
        }
        
        return ActionState.MINING;
    }
    
    @Override
    public void exit(ActionContext context) {
        log.debug("Exiting MINING state");
    }
    
    private boolean needsTool() {
        return !(robot.getMainHandItem().getItem() instanceof DiggerItem);
    }
    
    
    private void consumeToolDurability() {
        if (robot.getMainHandItem().getItem() instanceof DiggerItem) {
            log.debug("Consuming tool durability");
        }
    }
    
    private boolean hasLineOfSight(BlockPos target) {
        if (robot.tickCount - cacheInvalidationTick > 40) {
            lineOfSightCache.clear();
            cacheInvalidationTick = robot.tickCount;
        }

        return lineOfSightCache.computeIfAbsent(target, pos -> {
            Vec3 robotEyes = robot.getEyePosition();
            Vec3 targetCenter = Vec3.atCenterOf(pos);

            ClipContext clipContext = new ClipContext(
                robotEyes,
                targetCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                robot
            );

            BlockHitResult hit = robot.level().clip(clipContext);
            BlockPos hitPos = hit.getBlockPos();

            return hitPos.equals(target);
        });
    }
    
    private void completeMining() {
        log.info("Mining task completed");
        robot.getBrain().eraseMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
        robot.getBrain().eraseMemory(ModMemoryModuleTypes.MINING_PLAN.get());
        
        String message = "<green>[A.M.I.]</green> <gray>Mining completed!</gray>";
        sendMessageToOwner(message);
    }
    
    private void sendMessageToOwner(String message) {
        if (robot.getOwner() != null) {
	        ProjectAmi.adventure().audience(robot.getOwner()).sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }
}
