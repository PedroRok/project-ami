package com.pedrorok.ami.network.packets;

import com.pedrorok.ami.ProjectAmi;
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
public record OpenDialoguePacket(String dialogueId, int entityId) implements CustomPacketPayload {

    public static final Type<OpenDialoguePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "entity_action_point_reach")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDialoguePacket> STREAM_CODEC =
            StreamCodec.of(OpenDialoguePacket::encode, OpenDialoguePacket::decode);


    public static void encode(FriendlyByteBuf buf, OpenDialoguePacket packet) {
        buf.writeUtf(packet.dialogueId);
        buf.writeInt(packet.entityId);
    }

    public static OpenDialoguePacket decode(FriendlyByteBuf buf) {
        String dialogId = buf.readUtf();
        int entityId = buf.readInt();
        return new OpenDialoguePacket(dialogId, entityId);
    }

    public static void handle(OpenDialoguePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(packet.entityId());
                LivingEntity livingEntity = entity instanceof LivingEntity ? (LivingEntity) entity : null;
                DialogueHandler.getInstance().openDialogue(packet.dialogueId(), livingEntity);
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
