package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.ai.sensors.PlayerDetectorSensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSensorTypes {
	public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(Registries.SENSOR_TYPE, ProjectAmi.MOD_ID);
	
	public static final DeferredHolder<SensorType<?>, SensorType<PlayerDetectorSensor>> PLAYER_DETECTOR = SENSOR_TYPES.register("player_detector",
		() -> new SensorType<>(PlayerDetectorSensor::new)
	);
}