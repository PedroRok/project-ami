package com.pedrorok.ami.entities.robot.fsm;

public interface StateHandler {
    void enter(ActionContext context);
    ActionState tick(ActionContext context);
    void exit(ActionContext context);
}
