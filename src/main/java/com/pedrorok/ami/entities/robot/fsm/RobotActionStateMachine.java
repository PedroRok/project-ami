package com.pedrorok.ami.entities.robot.fsm;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.fsm.states.IdleState;
import com.pedrorok.ami.entities.robot.fsm.states.MiningState;
import com.pedrorok.ami.entities.robot.fsm.states.NavigatingState;
import com.pedrorok.ami.entities.robot.fsm.states.PlanningState;
import com.pedrorok.ami.entities.robot.fsm.states.RequestToolState;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RobotActionStateMachine {
    @Getter private ActionState currentState;
    @Setter private ActionContext context;
    private int ticksInCurrentState;
    
    private final IdleState idleState;
    private final PlanningState planningState;
    private final NavigatingState navigatingState;
    private final MiningState miningState;
    private final RequestToolState requestToolState;
    
    public RobotActionStateMachine(RobotEntity robot) {
        this.currentState = ActionState.IDLE;
        this.ticksInCurrentState = 0;
        
        this.idleState = new IdleState(robot);
        this.planningState = new PlanningState(robot);
        this.navigatingState = new NavigatingState(robot);
        this.miningState = new MiningState(robot);
        this.requestToolState = new RequestToolState(robot);
    }
    
    public void tick(RobotEntity robot) {
        if (context == null) {
            log.warn("ActionContext is null, cannot tick state machine");
            return;
        }
        
        ActionContext updatedContext = context.withTicksInState(ticksInCurrentState);
        
        ActionState nextState = getCurrentStateHandler().tick(updatedContext);
        
        if (nextState != currentState) {
            if (currentState.canTransitionTo(nextState)) {
                log.info("State transition: {} -> {}", currentState, nextState);
                getCurrentStateHandler().exit(updatedContext);
                currentState = nextState;
                ticksInCurrentState = 0;
                getCurrentStateHandler().enter(updatedContext);
            } else {
                log.warn("Invalid state transition attempted: {} -> {}", currentState, nextState);
            }
        }
        
        ticksInCurrentState++;
    }
	
	public void forceTransition(ActionState newState) {
        if (currentState.canTransitionTo(newState)) {
            log.info("Forced state transition: {} -> {}", currentState, newState);
            getCurrentStateHandler().exit(context);
            currentState = newState;
            ticksInCurrentState = 0;
            getCurrentStateHandler().enter(context);
        } else {
            log.warn("Cannot force transition from {} to {}", currentState, newState);
        }
    }
    
    private StateHandler getCurrentStateHandler() {
        return switch (currentState) {
            case IDLE -> idleState;
            case PLANNING -> planningState;
            case NAVIGATING -> navigatingState;
            case MINING -> miningState;
            case REQUEST_TOOL -> requestToolState;
        };
    }
}
