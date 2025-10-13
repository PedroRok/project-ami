package com.pedrorok.ami.registry;

import com.mojang.serialization.Codec;
import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.tasks.base.TaskData;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningPlan;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class ModMemoryModuleTypes {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, ProjectAmi.MOD_ID);
	
	public static final Supplier<MemoryModuleType<Unit>> REQUEST_TOOL_COOLDOWN = register("request_tool_cooldown", Unit.CODEC);
	public static final Supplier<MemoryModuleType<TaskData>> CURRENT_TASK = register("current_task", TaskData.DISPATCH_CODEC);
	public static final Supplier<MemoryModuleType<MiningPlan>> MINING_PLAN = register("mining_plan", null);
	
	private static <U> Supplier<MemoryModuleType<U>> register(String identifier, Codec<U> codec) {
		if (codec == null) {
			return MEMORY_MODULES.register(identifier, () -> new MemoryModuleType<>(Optional.empty()));
		}
		return MEMORY_MODULES.register(identifier, () -> new MemoryModuleType<>(Optional.of(codec)));
	}
	private static Supplier<MemoryModuleType<?>> register(String identifier) {
		return MEMORY_MODULES.register(identifier, () -> new MemoryModuleType<>(Optional.empty()));
	}
}