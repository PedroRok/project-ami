package com.pedrorok.ami.events;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.commands.DebugCommands;
import com.pedrorok.ami.commands.RobotCommandHandler;
import com.pedrorok.ami.entities.robot.RobotEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.List;

@EventBusSubscriber(modid = ProjectAmi.MOD_ID)
public class ModEvents {
	
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		DebugCommands.register(event.getDispatcher());
	}
	
	@SubscribeEvent
	public static void onServerChat(ServerChatEvent event) {
		ServerPlayer player = event.getPlayer();
		List<RobotEntity> robots = player.serverLevel().getEntitiesOfClass(RobotEntity.class, player.getBoundingBox().inflate(15));
		for (RobotEntity robot : robots) {
			RobotCommandHandler.processCommand(robot, player, event.getRawText());
		}
	}
}
