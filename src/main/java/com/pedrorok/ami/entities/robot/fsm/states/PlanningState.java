package com.pedrorok.ami.entities.robot.fsm.states;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.fsm.ActionContext;
import com.pedrorok.ami.entities.robot.fsm.ActionState;
import com.pedrorok.ami.entities.robot.fsm.StateHandler;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.pathfinding.mining.MiningPathfinder;
import com.pedrorok.ami.pathfinding.mining.MiningPathPlan;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.Component;

@Slf4j
@RequiredArgsConstructor
public class PlanningState implements StateHandler {
    private final RobotEntity robot;
    
    @Override
    public void enter(ActionContext context) {
        log.info("Entering PLANNING state");
    }
    
    @Override
    public ActionState tick(ActionContext context) {
        if (needsTool()) {
            return ActionState.REQUEST_TOOL;
        }
        
        MiningTaskData miningTask = robot.getBrain()
            .getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
            .filter(task -> task instanceof MiningTaskData)
            .map(task -> (MiningTaskData) task)
            .orElse(null);
            
        if (miningTask == null) {
            return ActionState.IDLE;
        }
        
        if (miningTask.getPhase() != MiningTaskData.MiningPhase.PLANNING) {
            return ActionState.IDLE;
        }
        
        MiningPathfinder pathfinder = new MiningPathfinder();
        MiningPathPlan plan = pathfinder.planMining(miningTask, context.getLevel(), robot);
        
        if (!plan.isViable()) {
            String message = "<red>[A.M.I.]</red> <gray>" + plan.getFailureReason() + "</gray>";
            sendMessageToOwner(message);
            
            robot.getBrain().eraseMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
            robot.getBrain().eraseMemory(ModMemoryModuleTypes.MINING_PLAN.get());
            return ActionState.IDLE;
        }
        
        robot.getBrain().setMemory(ModMemoryModuleTypes.MINING_PLAN.get(), plan);
        miningTask.setPhase(MiningTaskData.MiningPhase.NAVIGATING);
        
        String message = String.format("<green>[A.M.I.]</green> <gray>Plan ready! Mining %d blocks.</gray>", 
            plan.getTotalBlocks());
        sendMessageToOwner(message);
        
        return ActionState.NAVIGATING;
    }
    
    @Override
    public void exit(ActionContext context) {
        log.debug("Exiting PLANNING state");
    }
    
    private boolean needsTool() {
        return !(robot.getMainHandItem().getItem() instanceof net.minecraft.world.item.DiggerItem);
    }
    
    private void sendMessageToOwner(String message) {
        if (robot.getOwner() != null) {
	        ProjectAmi.adventure().audience(robot.getOwner()).sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }
}
