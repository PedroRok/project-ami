package com.pedrorok.ami.entities.robot.behaviors.mining;

import com.mojang.datafixers.util.Pair;
import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.base.TaskType;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.object.MemoryTest;

import java.util.List;

public class NavigateToMiningStart extends MoveToWalkTarget<RobotEntity> {
	public static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1)
		.hasMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
	
	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, RobotEntity robot) {
		boolean result = robot.getBrain()
			.getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
			.filter(task -> task.type() == TaskType.MINING)
			.map(task -> {
				if (task instanceof MiningTaskData miningTask) {
					// Só executar se estiver na fase NAVIGATING
					if (miningTask.getPhase() != MiningTaskData.MiningPhase.NAVIGATING) {
						ProjectAmi.LOGGER.debug("[NavigateToStart] Não está na fase NAVIGATING (current: {})", miningTask.getPhase());
						return false;
					}
					
					BlockPos startPos = miningTask.getStartPos();
					
					if (startPos == null) {
						ProjectAmi.LOGGER.warn("[NavigateToStart] startPos é null, abortando navegação.");
						return false;
					}
					
					// Se já está perto do startPos, mudar para fase MINING
					double distance = robot.distanceToSqr(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5);
					if (distance <= 25.0) { // 5 blocks squared
						ProjectAmi.LOGGER.info("[NavigateToStart] Já está perto do startPos ({}). Mudando para fase MINING.", startPos);
						miningTask.setPhase(MiningTaskData.MiningPhase.MINING);
						return false; // Não precisa navegar, já chegou
					}
					
					ProjectAmi.LOGGER.info("[NavigateToStart] Navegando para startPos: {} (distancia: {})", startPos, Math.sqrt(distance));
					return true;
				}
				return false;
			})
			.orElse(false);
		
		return result;
	}
	
	@Override
	protected void start(ServerLevel level, RobotEntity robot, long gameTime) {
		ProjectAmi.LOGGER.info("=== [NavigateToMiningStart] START ===");
		robot.getBrain()
			.getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
			.ifPresent(task -> {
				if (task instanceof MiningTaskData miningTask) {
					BlockPos startPos = miningTask.getStartPos();
					if (startPos != null) {
						robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(startPos, 1.0F, 0));
						ProjectAmi.LOGGER.info("[NavigateToMiningStart] WALK_TARGET definido para: {}", startPos);
					}
				}
			});
	}
	
	@Override
	protected void tick(ServerLevel level, RobotEntity robot, long gameTime) {
		robot.getBrain()
			.getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
			.ifPresent(task -> {
				if (task instanceof MiningTaskData miningTask) {
					BlockPos startPos = miningTask.getStartPos();
					if (startPos != null) {
						double distance = robot.distanceToSqr(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5);
						if (distance <= 25.0) { // 5 blocks squared
							ProjectAmi.LOGGER.info("[NavigateToMiningStart] Robô chegou ao startPos ({}). Mudando para fase MINING.", startPos);
							miningTask.setPhase(MiningTaskData.MiningPhase.MINING);
							this.doStop(level, robot, gameTime); // Parar este behavior
						}
					}
				}
			});
	}
	
	@Override
	protected boolean shouldKeepRunning(RobotEntity entity) {
		return entity.getBrain()
			.getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
			.filter(task -> task.type() == TaskType.MINING)
			.map(task -> {
				if (task instanceof MiningTaskData miningTask) {
					return miningTask.getPhase() == MiningTaskData.MiningPhase.NAVIGATING;
				}
				return false;
			})
			.orElse(false);
	}
}
