package com.pedrorok.ami.commands;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.commands.parser.CommandParser;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.entity.player.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Slf4j
public class RobotCommandHandler {
    private static final CommandParser parser = new CommandParser();
    
    public static boolean processCommand(RobotEntity robot, Player player, String command) {
        if (!robot.isOwnedBy(player)) {
            sendMessage(player, "<red>[A.M.I.]</red> <gray>I don't recognize you as my owner.</gray>");
            return false;
        }
        
        if (robot.getBrain().hasMemoryValue(ModMemoryModuleTypes.CURRENT_TASK.get())) {
            sendMessage(player, "<yellow>[A.M.I.]</yellow> <gray>I already have a task in progress!</gray>");
            return false;
        }
        
        return parser.parse(command)
            .map(cmd -> cmd.execute(robot, player))
            .orElseGet(() -> {
                sendUnknownCommandMessage(player, command);
                return false;
            });
    }
    
    private static void sendUnknownCommandMessage(Player player, String command) {
        sendMessage(player, "<red>[A.M.I.]</red> <gray>Unknown command: " + command + "</gray>");
        sendMessage(player, "<yellow>[A.M.I.]</yellow> <gray>Available commands:</gray>");
        sendMessage(player, "<gray>  - mine straight <distance></gray>");
        sendMessage(player, "<gray>  - mine tunnel2x1 <distance></gray>");
        sendMessage(player, "<gray>  - mine tunnel3x3 <distance></gray>");
        sendMessage(player, "<gray>  - mine staircase <distance></gray>");
        sendMessage(player, "<gray>  - mine branch <distance></gray>");
        sendMessage(player, "<gray>  - stop</gray>");
        sendMessage(player, "<gray>  - status</gray>");
    }
    
    private static void sendMessage(Player player, String message) {
        ProjectAmi.adventure().audience(player).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
