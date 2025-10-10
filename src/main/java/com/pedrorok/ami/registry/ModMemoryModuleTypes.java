package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMemoryModuleTypes {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, ProjectAmi.MOD_ID);
}