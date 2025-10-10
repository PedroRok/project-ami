package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.RobotEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = ProjectAmi.MOD_ID)
public class ModEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ProjectAmi.MOD_ID);
	
	public static final DeferredHolder<EntityType<?>, EntityType<RobotEntity>> ROBOT = ENTITIES.register("robot",
		() -> EntityType.Builder.of(RobotEntity::new, net.minecraft.world.entity.MobCategory.CREATURE).sized(0.6F, 1.8F).build("robot"));
	
	@SubscribeEvent
	public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
		event.put(ROBOT.get(), RobotEntity.createAttributes().build());
	}
}
