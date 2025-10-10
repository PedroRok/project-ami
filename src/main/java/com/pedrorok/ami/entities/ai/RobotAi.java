package com.pedrorok.ami.entities.ai;

import com.google.common.collect.ImmutableList;
import com.pedrorok.ami.entities.RobotEntity;
import com.pedrorok.ami.entities.ai.behaviors.FollowPlayerBehavior;
import com.pedrorok.ami.entities.ai.behaviors.MiningBehavior;
import com.pedrorok.ami.entities.ai.behaviors.RequestToolBehavior;
import com.pedrorok.ami.entities.ai.behaviors.RobotIdleBehavior;
import com.pedrorok.ami.entities.ai.behaviors.RobotWanderBehavior;
import com.pedrorok.ami.entities.ai.behaviors.WaitBehavior;
import com.pedrorok.ami.registry.ModActivities;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;

public class RobotAi {
	public static Brain<RobotEntity> makeBrain(Brain<RobotEntity> brain) {
		initCoreActivity(brain);
		initIdleActivity(brain);
		initFollowPlayerActivity(brain);
		initMiningActivity(brain);
		initWaitingActivity(brain);
		initRequestingToolActivity(brain);
		brain.setCoreActivities(Set.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}
	
	private static void initCoreActivity(Brain<RobotEntity> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
	}
	
	private static void initIdleActivity(Brain<RobotEntity> brain) {
		brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
			new FollowPlayerBehavior(), 
			new RobotWanderBehavior(), 
			new RobotIdleBehavior()
		));
	}
	
	private static void initFollowPlayerActivity(Brain<RobotEntity> brain) {
		brain.addActivity(ModActivities.FOLLOW_PLAYER.get(), 5, ImmutableList.of(
			new FollowPlayerBehavior()
		));
	}
	
	private static void initMiningActivity(Brain<RobotEntity> brain) {
		brain.addActivity(ModActivities.MINING.get(), 3, ImmutableList.of(
			new MiningBehavior()
		));
	}
	
	private static void initWaitingActivity(Brain<RobotEntity> brain) {
		brain.addActivity(ModActivities.WAITING.get(), 1, ImmutableList.of(
			new WaitBehavior()
		));
	}
	
	private static void initRequestingToolActivity(Brain<RobotEntity> brain) {
		brain.addActivity(ModActivities.REQUESTING_TOOL.get(), 2, ImmutableList.of(
			new RequestToolBehavior()
		));
	}
	
	public static void updateActivity(RobotEntity robot) {
		robot.getBrain().updateActivityFromSchedule(robot.level().getDayTime(), robot.level().getGameTime());
	}
}