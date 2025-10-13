package com.pedrorok.ami.entities.robot.behaviors.mining;

import com.mojang.datafixers.util.Pair;
import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.base.TaskType;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;

import java.util.List;

public class CheckMiningConditions extends ExtendedBehaviour<RobotEntity> {
	public static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1)
		.hasMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
	
	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, RobotEntity robot) {
		return robot.getBrain()
			.getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
			.filter(task -> task.type() == TaskType.MINING)
			.isPresent();
	}
	
	@Override
	protected void start(ServerLevel level, RobotEntity robot, long gameTime) {
		// Check energy
		// if (robot.getEnergy().getCurrentEnergy() < 50) {
		// 	sendMessageToOwner(robot, "<red>[A.M.I.]</red> <gray>Low energy! Need fuel to continue mining.</gray>");
		// 	robot.getBrain().eraseMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
		// 	return;
		// }
		
		// Check tool
		if (!(robot.getMainHandItem().getItem() instanceof net.minecraft.world.item.DiggerItem)) {
			sendMessageToOwner(robot, "<red>[A.M.I.]</red> <gray>Tool broken! Need a new pickaxe to continue mining.</gray>");
			robot.getBrain().eraseMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
			return;
		}
		
		// Check owner distance
		Player owner = robot.getOwner();
		if (owner != null && robot.distanceTo(owner) > 64) {
			sendMessageToOwner(robot, "<yellow>[A.M.I.]</yellow> <gray>You're too far away! Pausing mining task.</gray>");
			// Don't erase memory, just pause - owner can come back
		}
	}
	
	private void sendMessageToOwner(RobotEntity robot, String message) {
		Player owner = robot.getOwner();
		if (owner != null) {
			ProjectAmi.adventure().audience(owner).sendMessage(MiniMessage.miniMessage().deserialize(message));
		}
	}
}
