package com.pedrorok.ami.entities.robot.fsm.states;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.fsm.ActionContext;
import com.pedrorok.ami.entities.robot.fsm.ActionState;
import com.pedrorok.ami.entities.robot.fsm.StateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.Component;

@Slf4j
@RequiredArgsConstructor
public class RequestToolState implements StateHandler {
    private final RobotEntity robot;
    
    @Override
    public void enter(ActionContext context) {
        log.info("Entering REQUEST_TOOL state");
        String message = "<yellow>[A.M.I.]</yellow> <gray>I need a pickaxe to mine!</gray>";
        sendMessageToOwner(message);
    }
    
    @Override
    public ActionState tick(ActionContext context) {
        if (hasTool()) {
            log.info("Tool obtained, returning to previous state");
            return ActionState.IDLE;
        }
        
        return ActionState.REQUEST_TOOL;
    }
    
    @Override
    public void exit(ActionContext context) {
        log.debug("Exiting REQUEST_TOOL state");
    }
    
    private boolean hasTool() {
        return robot.getMainHandItem().getItem() instanceof net.minecraft.world.item.DiggerItem;
    }
    
    private void sendMessageToOwner(String message) {
        if (robot.getOwner() != null) {
	        ProjectAmi.adventure().audience(robot.getOwner()).sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }
}
