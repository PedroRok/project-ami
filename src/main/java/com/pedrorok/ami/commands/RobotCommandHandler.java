package com.pedrorok.ami.commands;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.registry.ModActivities;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RobotCommandHandler {
	// Command patterns
	private static final Pattern MINE_STRAIGHT = Pattern.compile("(?:mine|minere|cave|escave)\\s+(?:straight|reto|frente|ahead)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MINE_DOWN = Pattern.compile("(?:mine|minere|cave|escave)\\s+(?:down|baixo|below)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MINE_UP = Pattern.compile("(?:mine|minere|cave|escave)\\s+(?:up|acima|above)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MINE_TUNNEL_2x1 = Pattern.compile("(?:mine|minere|cave|escave)\\s+(?:tunnel2x1|tunel2x1)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MINE_TUNNEL_3x3 = Pattern.compile("(?:mine|minere|cave|escave)\\s+(?:tunnel3x3|tunel3x3)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MINE_STAIRCASE = Pattern.compile("(?:mine|minere|cave|escave)\\s+(?:staircase|escada)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MINE_BRANCH = Pattern.compile("(?:mine|minere|cave|escave)\\s+(?:branch|galho)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
	
	public static boolean processCommand(RobotEntity robot, Player player, String command) {
		String normalizedCommand = command.trim().toLowerCase();
		
		// Check ownership
		if (!robot.isOwnedBy(player)) {
			sendMessage(player, "<red>[A.M.I.]</red> <gray>I don't recognize you as my owner.</gray>");
			return false;
		}
		
		// Check if robot already has a task
		if (robot.getBrain().hasMemoryValue(ModMemoryModuleTypes.CURRENT_TASK.get())) {
			sendMessage(player, "<yellow>[A.M.I.]</yellow> <gray>I already have a task in progress!</gray>");
			return false;
		}
		
		// Parse mining commands
		Matcher straightMatcher = MINE_STRAIGHT.matcher(normalizedCommand);
		if (straightMatcher.matches()) {
			int blocks = Integer.parseInt(straightMatcher.group(1));
			return processMiningCommand(robot, player, Direction.NORTH, blocks, MiningTaskData.MiningPattern.STRAIGHT);
		}
		
		Matcher downMatcher = MINE_DOWN.matcher(normalizedCommand);
		if (downMatcher.matches()) {
			int blocks = Integer.parseInt(downMatcher.group(1));
			return processMiningCommand(robot, player, Direction.DOWN, blocks, MiningTaskData.MiningPattern.STRAIGHT);
		}
		
		Matcher upMatcher = MINE_UP.matcher(normalizedCommand);
		if (upMatcher.matches()) {
			int blocks = Integer.parseInt(upMatcher.group(1));
			return processMiningCommand(robot, player, Direction.UP, blocks, MiningTaskData.MiningPattern.STRAIGHT);
		}
		
		Matcher tunnel2x1Matcher = MINE_TUNNEL_2x1.matcher(normalizedCommand);
		if (tunnel2x1Matcher.matches()) {
			int blocks = Integer.parseInt(tunnel2x1Matcher.group(1));
			return processMiningCommand(robot, player, Direction.NORTH, blocks, MiningTaskData.MiningPattern.TUNNEL_2X1);
		}
		
		Matcher tunnel3x3Matcher = MINE_TUNNEL_3x3.matcher(normalizedCommand);
		if (tunnel3x3Matcher.matches()) {
			int blocks = Integer.parseInt(tunnel3x3Matcher.group(1));
			return processMiningCommand(robot, player, Direction.NORTH, blocks, MiningTaskData.MiningPattern.TUNNEL_3X3);
		}
		
		Matcher staircaseMatcher = MINE_STAIRCASE.matcher(normalizedCommand);
		if (staircaseMatcher.matches()) {
			int blocks = Integer.parseInt(staircaseMatcher.group(1));
			return processMiningCommand(robot, player, Direction.NORTH, blocks, MiningTaskData.MiningPattern.STAIRCASE);
		}
		
		Matcher branchMatcher = MINE_BRANCH.matcher(normalizedCommand);
		if (branchMatcher.matches()) {
			int blocks = Integer.parseInt(branchMatcher.group(1));
			return processMiningCommand(robot, player, Direction.NORTH, blocks, MiningTaskData.MiningPattern.BRANCH);
		}
		
		// Unknown command
		sendMessage(player, "<red>[A.M.I.]</red> <gray>Unknown command: " + command + "</gray>");
		sendMessage(player, "<yellow>[A.M.I.]</yellow> <gray>Available commands:</gray>");
		sendMessage(player, "<gray>  - mine straight <number></gray>");
		sendMessage(player, "<gray>  - mine down <number></gray>");
		sendMessage(player, "<gray>  - mine up <number></gray>");
		sendMessage(player, "<gray>  - mine tunnel2x1 <number></gray>");
		sendMessage(player, "<gray>  - mine tunnel3x3 <number></gray>");
		sendMessage(player, "<gray>  - mine staircase <number></gray>");
		sendMessage(player, "<gray>  - mine branch <number></gray>");
		
		return false;
	}
	
	private static boolean processMiningCommand(RobotEntity robot, Player player, Direction direction, int blocks, MiningTaskData.MiningPattern pattern) {
		ProjectAmi.LOGGER.info("=== PROCESSANDO COMANDO DE MINERAÇÃO ===");
		ProjectAmi.LOGGER.info("Jogador: {}", player.getName().getString());
		ProjectAmi.LOGGER.info("Direção: {}, Blocos: {}, Pattern: {}", direction, blocks, pattern);
		
		// Validate block count
		if (blocks <= 0 || blocks > 100) {
			ProjectAmi.LOGGER.warn("Block count inválido: {}", blocks);
			sendMessage(player, "<red>[A.M.I.]</red> <gray>Invalid block count! Use between 1 and 100.</gray>");
			return false;
		}
		// Check energy
		// if (robot.getEnergy().getCurrentEnergy() < 50) {
		// 	sendMessage(player, "<red>[A.M.I.]</red> <gray>Not enough energy! Need fuel to mine.</gray>");
		// 	return false;
		// }
		// Check tool
		if (!(robot.getMainHandItem().getItem() instanceof net.minecraft.world.item.DiggerItem)) {
			ProjectAmi.LOGGER.warn("Robô sem ferramenta de mineração");
			sendMessage(player, "<red>[A.M.I.]</red> <gray>I need a pickaxe to mine!</gray>");
			return false;
		}
		
		// 🆕 SCAN INTELIGENTE
		BlockPos startPos = findFirstSolidBlock(player, direction, 10);
		
		if (startPos == null) {
			ProjectAmi.LOGGER.error("FALHA: Nenhum bloco sólido encontrado!");
			sendMessage(player, "<red>[A.M.I.]</red> <gray>No solid blocks found in that direction!</gray>");
			return false;
		}
		
		ProjectAmi.LOGGER.info("StartPos definido: {}", startPos);
		
		// Create mining task with correct startPos
		MiningTaskData taskData = new MiningTaskData(direction, blocks, pattern, startPos);
		
		// 🆕 AJUSTAR TOTAL BLOCKS PARA PADRÕES DE TÚNEL
		if (pattern == MiningTaskData.MiningPattern.TUNNEL_2X1) {
			// Túnel 2x1: cada "bloco" na verdade são 2 blocos (linha principal + superior)
			taskData.setTotalBlocks(blocks * 2);
		} else if (pattern == MiningTaskData.MiningPattern.TUNNEL_3X3) {
			// Túnel 3x3: cada "bloco" na verdade são 9 blocos (padrão 3x3)
			taskData.setTotalBlocks(blocks * 9);
		}
		
		robot.getBrain().setMemory(ModMemoryModuleTypes.CURRENT_TASK.get(), taskData);
		robot.getBrain().setActiveActivityIfPossible(ModActivities.MINING.get());
		
		ProjectAmi.LOGGER.info("=== TASK CRIADA COM SUCESSO ===");
		
		// Send confirmation
		String patternName = pattern.getSerializedName();
		String directionName = direction.getName();
		sendMessage(player, "<green>[A.M.I.]</green> <gray>Mining task started! " + blocks + " blocks " + directionName + " using " + patternName + " pattern.</gray>");
		return true;
	}
	
	private static void sendMessage(Player player, String message) {
		ProjectAmi.adventure().audience(player).sendMessage(MiniMessage.miniMessage().deserialize(message));
	}
	
	private static BlockPos findFirstSolidBlock(Player player, Direction direction, int maxDistance) {
		BlockPos scanPos = player.blockPosition();
		ProjectAmi.LOGGER.info("=== INICIANDO SCAN ===");
		ProjectAmi.LOGGER.info("Posição inicial (player): {}", scanPos);
		ProjectAmi.LOGGER.info("Direção: {}", direction);
		ProjectAmi.LOGGER.info("Max distance: {}", maxDistance);
		
		for (int i = 0; i < maxDistance; i++) {
			boolean isAir = player.level().getBlockState(scanPos).isAir();
			ProjectAmi.LOGGER.info("Scan #{}: Pos={}, isAir={}, Block={}", 
				i, scanPos, isAir, player.level().getBlockState(scanPos).getBlock());
			
			if (!isAir) {
				// Se encontrou bloco sólido, retornar a posição ANTERIOR (onde o robô pode ficar)
				BlockPos beforeSolid = scanPos.relative(direction.getOpposite());
				ProjectAmi.LOGGER.info("=== BLOCO SÓLIDO ENCONTRADO: {} ===", scanPos);
				ProjectAmi.LOGGER.info("=== STARTPOS SERÁ: {} (posição ANTES do bloco sólido) ===", beforeSolid);
				return beforeSolid; // Retornar posição ANTES do bloco sólido
			}
			scanPos = scanPos.relative(direction);
		}
		
		ProjectAmi.LOGGER.warn("=== NENHUM BLOCO SÓLIDO ENCONTRADO ===");
		return null;
	}
}
