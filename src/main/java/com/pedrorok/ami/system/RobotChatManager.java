package com.pedrorok.ami.system;

import com.pedrorok.ami.entities.RobotEntity;
import com.pedrorok.ami.registry.ModActivities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RobotChatManager {
    private static final Map<UUID, ChatHistory> chatHistories = new HashMap<>();
    
    /**
     * Processes a chat command from a player to a robot
     */
    public static void processCommand(Player player, RobotEntity robot, String command) {
        if (!robot.isOwnedBy(player)) {
            return; // Only owner can command the robot
        }
        
        String lowerCommand = command.toLowerCase().trim();
        
        // Parse and execute command
        if (lowerCommand.contains("mine") || lowerCommand.contains("minerar")) {
            executeMiningCommand(robot, command);
        } else if (lowerCommand.contains("follow") || lowerCommand.contains("seguir")) {
            executeFollowCommand(robot);
        } else if (lowerCommand.contains("wait") || lowerCommand.contains("esperar")) {
            executeWaitCommand(robot, command);
        } else if (lowerCommand.contains("stop") || lowerCommand.contains("parar")) {
            executeStopCommand(robot);
        } else {
            // Unknown command
            addChatMessage(robot, "A.M.I.", "Não entendi esse comando. Tente: mine, follow, wait, ou stop.");
        }
    }
    
    private static void executeMiningCommand(RobotEntity robot, String command) {
        // Check if robot has a pickaxe
        if (!(robot.getMainHandItem().getItem() instanceof net.minecraft.world.item.PickaxeItem)) {
            addChatMessage(robot, "A.M.I.", "Preciso de uma picareta para minerar! Você pode me dar uma?");
            // Switch to requesting tool activity
            robot.getBrain().setActiveActivityIfPossible(ModActivities.REQUESTING_TOOL.get());
            return;
        }
        
        // Check energy
        if (robot.isOutOfEnergy()) {
            addChatMessage(robot, "A.M.I.", "Estou sem energia! Preciso de um Reactor Core para continuar.");
            return;
        }
        
        // Start mining
        addChatMessage(robot, "A.M.I.", "Começando a minerar! Vou cavar alguns blocos para você.");
        robot.getBrain().setActiveActivityIfPossible(ModActivities.MINING.get());
    }
    
    private static void executeFollowCommand(RobotEntity robot) {
        addChatMessage(robot, "A.M.I.", "Perfeito! Vou te seguir onde você for.");
        robot.getBrain().setActiveActivityIfPossible(ModActivities.FOLLOW_PLAYER.get());
    }
    
    private static void executeWaitCommand(RobotEntity robot, String command) {
        // Parse duration from command (simple implementation)
        int duration = 100; // Default 5 seconds
        if (command.contains("5")) duration = 100;
        else if (command.contains("10")) duration = 200;
        else if (command.contains("30")) duration = 600;
        
        addChatMessage(robot, "A.M.I.", "Entendido! Vou esperar aqui por " + (duration / 20) + " segundos.");
        robot.getBrain().setActiveActivityIfPossible(ModActivities.WAITING.get());
    }
    
    private static void executeStopCommand(RobotEntity robot) {
        addChatMessage(robot, "A.M.I.", "Parando todas as atividades. Estou aqui se precisar de algo!");
        robot.getBrain().setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.IDLE);
    }
    
    /**
     * Adds a message to the robot's chat history
     */
    public static void addChatMessage(RobotEntity robot, String sender, String message) {
        UUID robotUUID = robot.getUUID();
        ChatHistory history = chatHistories.computeIfAbsent(robotUUID, k -> new ChatHistory(robotUUID));
        history.addMessage(sender, message);
    }
    
    /**
     * Gets the chat history for a robot
     */
    public static ChatHistory getChatHistory(RobotEntity robot) {
        return chatHistories.computeIfAbsent(robot.getUUID(), k -> new ChatHistory(robot.getUUID()));
    }
    
    /**
     * Clears chat history for a robot
     */
    public static void clearChatHistory(RobotEntity robot) {
        chatHistories.remove(robot.getUUID());
    }
}
