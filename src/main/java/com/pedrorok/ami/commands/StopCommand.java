package com.pedrorok.ami.commands;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.entity.player.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Slf4j
public class StopCommand implements ParsedCommand {
    
    @Override
    public boolean execute(RobotEntity robot, Player player) {
        if (robot.getBrain().hasMemoryValue(ModMemoryModuleTypes.CURRENT_TASK.get())) {
            robot.getBrain().eraseMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
            sendMessage(player, "<yellow>[A.M.I.]</yellow> <gray>Task stopped!</gray>");
            return true;
        } else {
            sendMessage(player, "<yellow>[A.M.I.]</yellow> <gray>No active task to stop.</gray>");
            return false;
        }
    }
    
    @Override
    public String getDescription() {
        return "Stop current task";
    }
    
    private void sendMessage(Player player, String message) {
        com.pedrorok.ami.ProjectAmi.adventure().audience(player).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
