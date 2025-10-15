package com.pedrorok.ami.entities.robot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
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
				new Idle<>().runForBetween(30, 60)
			)
		);
	}
}