package com.pedrorok.ami.entities.robot.tasks.base;

import com.mojang.serialization.MapCodec;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

public enum TaskType implements StringRepresentable {
	MINING("mining", () -> MiningTaskData.CODEC),
	/*COMBAT("combat", CombatTaskData.CODEC),
	BUILD("build", BuildTaskData.CODEC),
	TRANSPORT("transport", TransportTaskData.CODEC),
	COLLECT("collect", CollectTaskData.CODEC)*/;
	
	private final String serializedName;
	private final Supplier<MapCodec<? extends TaskData>> codec;
	
	TaskType(String serializedName, Supplier<MapCodec<? extends TaskData>> codec) {
		this.serializedName = serializedName;
		this.codec = codec;
	}
	
	public MapCodec<? extends TaskData> codec() {
		return this.codec.get();
	}
	
	@Override
	public String getSerializedName() {
		return serializedName;
	}
}
