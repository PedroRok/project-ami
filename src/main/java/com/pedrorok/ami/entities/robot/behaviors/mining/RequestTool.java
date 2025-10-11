package com.pedrorok.ami.entities.robot.behaviors.mining;

import com.mojang.datafixers.util.Pair;
import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotAi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;

import java.util.List;

public class RequestTool extends ExtendedBehaviour<RobotEntity> {
	public static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleType.LIKED_PLAYER);
	
	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, RobotEntity owner) {
		ItemStack stack = owner.getMainHandItem();
		if (!(stack.getItem() instanceof PickaxeItem)) {
			return false;
		}
		return super.checkExtraStartConditions(level, owner);
	}
	
	@Override
	protected void start(ServerLevel level, RobotEntity entity, long gameTime) {
		Player owner = entity.getOwner();
		if (owner == null) return;
		
		ProjectAmi.adventure().audience(owner)
			.sendMessage(MiniMessage.miniMessage().deserialize("<green>[A.M.I.]</green> <gray>Could you please provide me with a pickaxe? I need it to mine blocks.</gray>"));
	}
}
