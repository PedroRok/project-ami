package com.pedrorok.ami.entities.robot.tasks.base;

import com.mojang.serialization.Codec;
import com.pedrorok.ami.entities.robot.RobotEntity;
import net.minecraft.util.StringRepresentable;

public interface TaskData {
	Codec<TaskData> DISPATCH_CODEC = StringRepresentable.fromEnum(TaskType::values).dispatch(TaskData::type, TaskType::codec);
	
	TaskType type();
	
	boolean canStart(RobotEntity robot);
	void start(RobotEntity robot);
	TaskResult tick(RobotEntity robot);
	void stop(RobotEntity robot);
}

