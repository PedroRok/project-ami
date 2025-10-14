package com.pedrorok.ami.network.packets;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.system.dialog.DialogueHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;


/**
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public record PlayAnimationPacket(String animationName, int entityId) implements CustomPacketPayload {

    public static final Type<PlayAnimationPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "play_animation")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayAnimationPacket> STREAM_CODEC =
            StreamCodec.of(PlayAnimationPacket::encode, PlayAnimationPacket::decode);


    public static void encode(FriendlyByteBuf buf, PlayAnimationPacket packet) {
        buf.writeUtf(packet.animationName);
        buf.writeInt(packet.entityId);
    }

    public static PlayAnimationPacket decode(FriendlyByteBuf buf) {
        String animationName = buf.readUtf();
        int entityId = buf.readInt();
        return new PlayAnimationPacket(animationName, entityId);
    }

    public static void handle(PlayAnimationPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(packet.entityId());
                if (!(entity instanceof RobotEntity robot)) return;
                robot.playDialogueAnimation(packet.animationName);
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
