package com.pedrorok.ami.events;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.registry.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/10/2025
 * @project project-ami
 */
@EventBusSubscriber(modid = ProjectAmi.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Registra o RenderType para transparência
            ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.ROBOT_PART.get(),
                RenderType.translucent()
            );
        });
    }
}