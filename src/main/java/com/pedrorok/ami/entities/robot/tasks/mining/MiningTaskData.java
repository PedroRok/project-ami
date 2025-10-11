package com.pedrorok.ami.entities.robot.tasks.mining;

import com.mojang.serialization.MapCodec;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.base.TaskData;
import com.pedrorok.ami.entities.robot.tasks.base.TaskResult;
import com.pedrorok.ami.entities.robot.tasks.base.TaskType;

public class MiningTaskData implements TaskData {
	public static final MapCodec<MiningTaskData> CODEC = MapCodec.unit(MiningTaskData::new);
	
	@Override
	public boolean canStart(RobotEntity robot) {
		return false;
	}
	
	@Override
	public void start(RobotEntity robot) {
	}
	
	@Override
	public TaskResult tick(RobotEntity robot) {
		return TaskResult.RUNNING;
	}
	
	@Override
	public void stop(RobotEntity robot) {
	}
	
	@Override
	public TaskType type() {
		return TaskType.MINING;
	}
}
