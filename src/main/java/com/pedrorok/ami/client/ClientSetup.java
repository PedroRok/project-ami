package com.pedrorok.ami.client;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.client.renderer.RobotEntityRenderer;
import com.pedrorok.ami.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/10/2025
 * @project project-ami
 */
@EventBusSubscriber(modid = ProjectAmi.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onClientSetup(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ROBOT.get(), RobotEntityRenderer::new);
    }
}
