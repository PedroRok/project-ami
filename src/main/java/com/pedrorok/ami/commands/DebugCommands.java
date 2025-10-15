package com.pedrorok.ami.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.registry.ModActivities;
import com.pedrorok.ami.registry.ModEntities;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class DebugCommands {
	public static final Stream<String> STRINGS = Arrays.stream(Direction.values()).map(Direction::toString);
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("ami")
			.requires(source -> source.hasPermission(2)) // OP level 2 required
			.then(Commands.literal("spawn")
				.executes(DebugCommands::spawnRobot))
			.then(Commands.literal("mine")
				.then(Commands.argument("targets", EntityArgument.entities())
					.then(Commands.argument("direction", StringArgumentType.string())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(STRINGS, builder))
						.then(Commands.argument("blocks", IntegerArgumentType.integer(1, 100))
							.then(Commands.argument("pattern", StringArgumentType.string())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(MiningTaskData.MiningPattern.NAMES, builder))
								.executes(DebugCommands::setMiningTask))))))
			.then(Commands.literal("stop")
				.then(Commands.argument("targets", EntityArgument.entities())
					.executes(DebugCommands::stopTask)))
			.then(Commands.literal("debug")
				.then(Commands.argument("targets", EntityArgument.entities())
					.executes(DebugCommands::debugRobot)))
			.then(Commands.literal("energy")
				.then(Commands.argument("targets", EntityArgument.entities())
					.then(Commands.argument("amount", IntegerArgumentType.integer(0, 10000))
						.executes(DebugCommands::setEnergy))))
		);
	}
	
	private static int spawnRobot(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		
		RobotEntity robot = new RobotEntity(ModEntities.ROBOT.get(), level);
		robot.moveTo(source.getPosition());
		level.addFreshEntity(robot);
		
		// Set owner to command source if it's a player
		if (source.getEntity() instanceof Player player) {
			robot.setOwner(player);
		}
		
		source.sendSuccess(() -> Component.literal("Robot AMI spawned at " + source.getPosition()), true);
		return 1;
	}
	
	private static int setMiningTask(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		String directionStr = context.getArgument("direction", String.class);
		int blocks = IntegerArgumentType.getInteger(context, "blocks");
		String patternStr = context.getArgument("pattern", String.class);
		
		Direction direction;
		try {
			direction = Direction.byName(directionStr.toLowerCase());
			if (direction == null) {
				throw new IllegalArgumentException("Invalid direction");
			}
		} catch (Exception e) {
			source.sendFailure(Component.literal("Invalid direction: " + directionStr + ". Use: north, south, east, west, up, down"));
			return 0;
		}
		
		MiningTaskData.MiningPattern pattern;
		try {
			pattern = MiningTaskData.MiningPattern.valueOf(patternStr.toUpperCase());
		} catch (Exception e) {
			source.sendFailure(Component.literal("Invalid pattern: " + patternStr + ". Use: STRAIGHT, TUNNEL_2x1, TUNNEL_3x3, STAIRCASE, BRANCH"));
			return 0;
		}
		
		int count = 0;
		for (Entity entity : targets) {
			if (entity instanceof RobotEntity robot) {
				BlockPos startPos = context.getSource().getPlayerOrException().blockPosition();
				MiningTaskData taskData = new MiningTaskData(direction, blocks, pattern, startPos);
				
				robot.getBrain().setMemory(ModMemoryModuleTypes.CURRENT_TASK.get(), taskData);
				robot.getBrain().setActiveActivityIfPossible(ModActivities.MINING.get());
				count++;
			}
		}
		
		final int finalCount = count;
		source.sendSuccess(() -> Component.literal("Mining task set for " + finalCount + " robot(s): " + 
			blocks + " blocks " + direction.getName() + " using " + pattern.getSerializedName() + " pattern"), true);
		return count;
	}
	
	private static int stopTask(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		
		int count = 0;
		for (Entity entity : targets) {
			if (entity instanceof RobotEntity robot) {
				robot.getBrain().eraseMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
				robot.getBrain().setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.IDLE);
				count++;
			}
		}
		
		final int finalCount = count;
		source.sendSuccess(() -> Component.literal("Task stopped for " + finalCount + " robot(s)"), true);
		return count;
	}
	
	private static int debugRobot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		
		for (Entity entity : targets) {
			if (entity instanceof RobotEntity robot) {
				source.sendSuccess(() -> Component.literal("=== ROBOT DEBUG ==="), false);
				source.sendSuccess(() -> Component.literal("Energy: " + robot.getEnergy().getCurrentEnergy() + "/" + robot.getEnergy().getMaxEnergy()), false);
				source.sendSuccess(() -> Component.literal("Activity: " + robot.getBrain().getActiveNonCoreActivity().orElse(null)), false);
				source.sendSuccess(() -> Component.literal("Position: " + robot.blockPosition()), false);
				source.sendSuccess(() -> Component.literal("Owner: " + robot.getOwner()), false);
				
				robot.getBrain().getMemory(ModMemoryModuleTypes.CURRENT_TASK.get()).ifPresent(task -> {
					if (task instanceof MiningTaskData miningTask) {
						if (miningTask.getCurrentTarget() != null) {
							source.sendSuccess(() -> Component.literal("Current Target: " + miningTask.getCurrentTarget()), false);
						}
					}
				});
			}
		}
		
		return targets.size();
	}
	
	private static int setEnergy(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		int energy = IntegerArgumentType.getInteger(context, "amount");
		
		int count = 0;
		for (Entity entity : targets) {
			if (entity instanceof RobotEntity robot) {
				robot.getEnergy().setEnergy(energy);
				count++;
			}
		}
		
		final int finalEnergy = energy;
		final int finalCount = count;
		source.sendSuccess(() -> Component.literal("Energy set to " + finalEnergy + " for " + finalCount + " robot(s)"), true);
		return count;
	}
}
