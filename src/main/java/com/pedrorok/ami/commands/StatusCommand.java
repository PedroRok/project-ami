package com.pedrorok.ami.commands;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.entity.player.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Slf4j
public class StatusCommand implements ParsedCommand {
    
    @Override
    public boolean execute(RobotEntity robot, Player player) {
        boolean hasTask = robot.getBrain().hasMemoryValue(ModMemoryModuleTypes.CURRENT_TASK.get());
        
        if (hasTask) {
            sendMessage(player, "<green>[A.M.I.]</green> <gray>Status: Working on a task</gray>");
        } else {
            sendMessage(player, "<blue>[A.M.I.]</blue> <gray>Status: Idle and ready for commands</gray>");
        }
        
        return true;
    }
    
    @Override
    public String getDescription() {
        return "Show robot status";
    }
    
    private void sendMessage(Player player, String message) {
        com.pedrorok.ami.ProjectAmi.adventure().audience(player).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
