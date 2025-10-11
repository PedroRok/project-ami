package com.pedrorok.ami.registry;

import com.mojang.serialization.Codec;
import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.tasks.base.TaskData;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class ModMemoryModuleTypes {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, ProjectAmi.MOD_ID);
	
	public static final Supplier<MemoryModuleType<Unit>> REQUEST_TOOL_COOLDOWN = register("request_tool_cooldown", Unit.CODEC);
	public static final Supplier<MemoryModuleType<TaskData>> CURRENT_TASK = register("current_task", TaskData.DISPATCH_CODEC);
	
	private static <U> Supplier<MemoryModuleType<U>> register(String identifier, Codec<U> codec) {
		return MEMORY_MODULES.register(identifier, () -> new MemoryModuleType<>(Optional.of(codec)));
	}
	private static Supplier<MemoryModuleType<?>> register(String identifier) {
		return MEMORY_MODULES.register(identifier, () -> new MemoryModuleType<>(Optional.empty()));
	}
}