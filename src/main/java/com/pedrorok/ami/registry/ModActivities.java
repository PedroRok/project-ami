package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModActivities {
	public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(BuiltInRegistries.ACTIVITY, ProjectAmi.MOD_ID);
	
	public static final Supplier<Activity> MINING = ACTIVITIES.register("mining", () -> new Activity("mining"));
}
