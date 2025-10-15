package com.pedrorok.ami.entities.robot.fsm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActionState {
    IDLE("idle"),
    PLANNING("planning"),
    NAVIGATING("navigating"),
    MINING("mining"),
    REQUEST_TOOL("request_tool");
    
    private final String name;
    
    public boolean canTransitionTo(ActionState target) {
        return switch (this) {
            case IDLE -> target == PLANNING || target == REQUEST_TOOL;
            case PLANNING -> target == NAVIGATING || target == IDLE || target == REQUEST_TOOL;
            case NAVIGATING -> target == MINING || target == IDLE || target == REQUEST_TOOL;
            case MINING -> target == IDLE || target == REQUEST_TOOL;
            case REQUEST_TOOL -> target != REQUEST_TOOL;
        };
    }
}
