package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModActivities {
	public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(Registries.ACTIVITY, ProjectAmi.MOD_ID);
	
	public static final DeferredHolder<Activity, Activity> FOLLOW_PLAYER = ACTIVITIES.register("follow_player", () -> new Activity("follow_player"));
	public static final DeferredHolder<Activity, Activity> MINING = ACTIVITIES.register("mining", () -> new Activity("mining"));
	public static final DeferredHolder<Activity, Activity> WAITING = ACTIVITIES.register("waiting", () -> new Activity("waiting"));
	public static final DeferredHolder<Activity, Activity> REQUESTING_TOOL = ACTIVITIES.register("requesting_tool", () -> new Activity("requesting_tool"));
}