package com.pedrorok.ami.entities.robot.fsm.states;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.fsm.ActionContext;
import com.pedrorok.ami.entities.robot.fsm.ActionState;
import com.pedrorok.ami.entities.robot.fsm.StateHandler;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class IdleState implements StateHandler {
    private final RobotEntity robot;
    
    @Override
    public void enter(ActionContext context) {
        log.debug("Entering IDLE state");
        robot.getBrain().setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.IDLE);
    }
    
    @Override
    public ActionState tick(ActionContext context) {
        if (robot.getBrain().hasMemoryValue(ModMemoryModuleTypes.CURRENT_TASK.get())) {
            return ActionState.PLANNING;
        }
        
        if (needsTool()) {
            return ActionState.REQUEST_TOOL;
        }
        
        return ActionState.IDLE;
    }
    
    @Override
    public void exit(ActionContext context) {
        log.debug("Exiting IDLE state");
    }
    
    private boolean needsTool() {
        return !(robot.getMainHandItem().getItem() instanceof net.minecraft.world.item.DiggerItem);
    }
}
