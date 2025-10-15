package com.pedrorok.ami.network.packets;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;


/**
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public record PlayMoodPacket(String moodName, int timeInSec, int entityId) implements CustomPacketPayload {

    public static final Type<PlayMoodPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "play_mood")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayMoodPacket> STREAM_CODEC =
            StreamCodec.of(PlayMoodPacket::encode, PlayMoodPacket::decode);


    public static void encode(FriendlyByteBuf buf, PlayMoodPacket packet) {
        buf.writeUtf(packet.moodName);
        buf.writeInt(packet.timeInSec);
        buf.writeInt(packet.entityId);
    }

    public static PlayMoodPacket decode(FriendlyByteBuf buf) {
        String animationName = buf.readUtf();
        int timeInSec = buf.readInt();
        int entityId = buf.readInt();
        return new PlayMoodPacket(animationName, timeInSec, entityId);
    }

    public static void handle(PlayMoodPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(packet.entityId());
                if (!(entity instanceof RobotEntity robot)) return;
                robot.playMood(packet.moodName, packet.timeInSec);
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
