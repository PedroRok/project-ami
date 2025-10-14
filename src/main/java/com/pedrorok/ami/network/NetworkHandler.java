package com.pedrorok.ami.network;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.network.packets.OpenDialoguePacket;
import com.pedrorok.ami.network.packets.PlayAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
@EventBusSubscriber(modid = ProjectAmi.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                OpenDialoguePacket.TYPE,
                OpenDialoguePacket.STREAM_CODEC,
                OpenDialoguePacket::handle
        );
        registrar.playToClient(
                PlayAnimationPacket.TYPE,
                PlayAnimationPacket.STREAM_CODEC,
                PlayAnimationPacket::handle
        );
    }

    public static void sendToPlayer(OpenDialoguePacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}