package com.pedrorok.ami.entities.robot;

import com.pedrorok.ami.entities.robot.behaviors.mining.RequestTool;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.DiggerItem;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({ "unchecked" })
public interface RobotAi extends SmartBrainOwner<RobotEntity> {
	@Override
	default List<? extends ExtendedSensor<? extends RobotEntity>> getSensors() {
		return ObjectArrayList.of(
			new NearbyPlayersSensor<>(),
			new NearbyLivingEntitySensor<>()
		);
	}
	
	@Override
	default BrainActivityGroup<? extends RobotEntity> getCoreTasks() {
		return BrainActivityGroup.coreTasks(
			new LookAtTarget<>().runForBetween(45, 90),
			new MoveToWalkTarget<>()
		);
	}
	
	@Override
	default BrainActivityGroup<? extends RobotEntity> getIdleTasks() {
		return BrainActivityGroup.idleTasks(
			new SetPlayerLookTarget<>().runForBetween(30, 60),
			new SetRandomLookTarget<>(),
			new OneRandomBehaviour<>(
				new SetRandomFlyingTarget<>(),
				new Idle<>().runForBetween(30, 60)
			)
		);
	}
	
	@Override
	default Map<Activity, BrainActivityGroup<? extends RobotEntity>> getAdditionalTasks() {
		return Util.make(new Object2ObjectArrayMap<>(), map -> {
			map.put(Activity.WORK, new BrainActivityGroup<RobotEntity>(Activity.WORK).priority(10)
			.onlyStartWithMemoryStatus(ModMemoryModuleTypes.CURRENT_TASK.get(), MemoryStatus.VALUE_PRESENT)
			.behaviours(
				new FirstApplicableBehaviour<>(
					new RequestTool()
						.cooldownFor(e -> 600)
						.startCondition(e -> !(e.getMainHandItem().getItem() instanceof DiggerItem))
				)
			));
		});
	}
}