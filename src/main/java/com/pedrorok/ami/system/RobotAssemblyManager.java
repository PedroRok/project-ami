package com.pedrorok.ami.system;

import com.pedrorok.ami.blocks.robot_parts.PartType;
import com.pedrorok.ami.entities.RobotEntity;
import com.pedrorok.ami.registry.ModEntities;
import com.pedrorok.ami.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class RobotAssemblyManager {
	
	private static final Map<PartType, Integer> REQUIRED_PARTS = Map.of(
		PartType.HEAD, 1,
		PartType.ARM, 2,
		PartType.BODY, 1,
		PartType.REACTOR, 1
	);
	
	/**
	 * Verifica se o jogador tem todas as partes necessárias para montar o robô
	 */
	public static boolean canAssembleRobot(Player player) {
		Map<PartType, Integer> playerParts = countPlayerParts(player);
		
		for (Map.Entry<PartType, Integer> required : REQUIRED_PARTS.entrySet()) {
			int playerCount = playerParts.getOrDefault(required.getKey(), 0);
			if (playerCount < required.getValue()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Monta o robô A.M.I. no mundo e consome as partes do inventário
	 */
	public static boolean assembleRobot(Player player, Level level, BlockPos pos) {
		if (!canAssembleRobot(player)) {
			return false;
		}
		
		// Consume parts from inventory
		if (!consumePartsFromInventory(player)) {
			return false;
		}
		
		// Spawn the robot
		RobotEntity robot = ModEntities.ROBOT.get().create(level);
		if (robot != null) {
			robot.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
			robot.setOwner(player.getUUID());
			level.addFreshEntity(robot);
			
			// TODO: Open chat GUI with introduction message
			return true;
		}
		
		return false;
	}
	
	/**
	 * Conta quantas partes de cada tipo o jogador tem no inventário
	 */
	private static Map<PartType, Integer> countPlayerParts(Player player) {
		Map<PartType, Integer> counts = new HashMap<>();
		
		for (ItemStack stack : player.getInventory().items) {
			if (stack.getItem() == ModItems.ROBOT_PART.get()) {
				// TODO: Get part type from NBT or item variant
				// For now, assume we have a way to determine part type
				// This would need to be implemented with item variants or NBT
			}
		}
		
		return counts;
	}
	
	/**
	 * Remove as partes necessárias do inventário do jogador
	 */
	private static boolean consumePartsFromInventory(Player player) {
		// TODO: Implement actual consumption logic
		// This would need to find and remove the correct items
		return true;
	}
	
	/**
	 * Retorna as partes que ainda faltam para montar o robô
	 */
	public static Map<PartType, Integer> getMissingParts(Player player) {
		Map<PartType, Integer> playerParts = countPlayerParts(player);
		Map<PartType, Integer> missing = new HashMap<>();
		
		for (Map.Entry<PartType, Integer> required : REQUIRED_PARTS.entrySet()) {
			int playerCount = playerParts.getOrDefault(required.getKey(), 0);
			int needed = required.getValue() - playerCount;
			if (needed > 0) {
				missing.put(required.getKey(), needed);
			}
		}
		
		return missing;
	}
}
