package com.pedrorok.ami.entities.robot;

import com.pedrorok.ami.entities.robot.behaviors.mining.CheckMiningConditions;
import com.pedrorok.ami.entities.robot.behaviors.mining.ExecuteMiningPlan;
import com.pedrorok.ami.entities.robot.behaviors.mining.NavigateToMiningStart;
import com.pedrorok.ami.entities.robot.behaviors.mining.PlanMiningTask;
import com.pedrorok.ami.entities.robot.behaviors.mining.RequestTool;
import com.pedrorok.ami.registry.ModActivities;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.DiggerItem;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;

import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked" })
public interface RobotAi extends SmartBrainOwner<RobotEntity> {
	@Override
	default List<Activity> getActivityPriorities() {
		return ObjectArrayList.of(ModActivities.MINING.get(), Activity.IDLE);
	}
	
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
				// new SetRandomFlyingTarget<>(),
				new Idle<>().runForBetween(30, 60)
			)
		);
	}
	
	@Override
	default Map<Activity, BrainActivityGroup<? extends RobotEntity>> getAdditionalTasks() {
		return Util.make(new Object2ObjectArrayMap<>(), map -> {
			map.put(
				ModActivities.MINING.get(), new BrainActivityGroup<RobotEntity>(ModActivities.MINING.get())
					.priority(10)
					.onlyStartWithMemoryStatus(ModMemoryModuleTypes.CURRENT_TASK.get(), MemoryStatus.VALUE_PRESENT)
					.behaviours(
						// Behaviors que rodam EM PARALELO (checados toda hora)
						new CheckMiningConditions(),    // Sempre checa se ainda pode minerar
						new RequestTool()               // Sempre checa se precisa de ferramenta
							.startCondition(e -> !(e.getMainHandItem().getItem() instanceof DiggerItem))
							.cooldownFor(e -> 600),
						
						// Behaviors SEQUENCIAIS - apenas UM executa por vez
						new FirstApplicableBehaviour<>(
							new PlanMiningTask(),           // 1º: Se phase == PLANNING, executa APENAS ESTE
							new NavigateToMiningStart(),    // 2º: Se phase == NAVIGATING, executa APENAS ESTE  
							new ExecuteMiningPlan()         // 3º: Se phase == MINING, executa APENAS ESTE
						)
					)
			);
		});
	}
}