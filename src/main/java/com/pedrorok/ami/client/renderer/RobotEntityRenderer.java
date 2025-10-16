package com.pedrorok.ami.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.client.model.RobotEntityModel;
import com.pedrorok.ami.entities.robot.RobotEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/10/2025
 * @project project-ami
 */
public class RobotEntityRenderer extends GeoEntityRenderer<RobotEntity> {
    private final ResourceLocation MODEL_TEXTURE = ProjectAmi.resource("textures/entity/ami/ami_texture.png");

    private final ResourceLocation BATTERY_FULL = ProjectAmi.resource("textures/block/battery_full.png");
    private final ResourceLocation BATTERY_EMPTY = ProjectAmi.resource("textures/item/battery_empty.png");


    private final ResourceLocation DEFAULT_EYE = ProjectAmi.resource("textures/entity/ami/eye_default.png");
    private final ResourceLocation BLINK_EYE = ProjectAmi.resource("textures/entity/ami/eye_blink.png");

    private static final Map<String, ResourceLocation> MOODS = new HashMap<>() {{
        put("happy", ProjectAmi.resource("textures/entity/ami/eye_happy.png"));
        put("sad", ProjectAmi.resource("textures/entity/ami/eye_sad.png"));
    }};

    public static void registerMoods(String mood, ResourceLocation texture) {
        MOODS.put(mood, texture);
    }

    private int blinkTick = 0;

    public RobotEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RobotEntityModel());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RobotEntity animatable) {
        return MODEL_TEXTURE;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, RobotEntity animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour
    ) {
        if (bone.getName().equals("face")) {
            boolean blink = false;
            if (animatable.tickCount < 30) return;
            if (animatable.tickCount % 60 == 0) {
                blinkTick = 5;
            }
            if (blinkTick > 0) {
                blinkTick--;
                blink = true;
            }

            boolean hasMood = animatable.getCurrentMood() != null;
            ResourceLocation displayEye = hasMood ? MOODS.get(animatable.getCurrentMood()) : null;
            ResourceLocation eyeTexture = displayEye != null ? displayEye : DEFAULT_EYE;

            eyeTexture = blink || animatable.tickCount < 50 ? BLINK_EYE : eyeTexture;
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,  bufferSource.getBuffer(RenderType.entityTranslucentEmissive(eyeTexture)), isReRender, partialTick, packedLight, packedOverlay, colour);


            if (hasMood && animatable.getLastMoodTick()< animatable.tickCount) {
                animatable.playMood(null, 0);
                blinkTick = 10;
            }

            return;
        }

        if (bone.getName().equals("battery")) {
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,  bufferSource.getBuffer(RenderType.entityTranslucentEmissive(animatable.getEnergy().isOutOfEnergy() ? BATTERY_EMPTY : BATTERY_FULL)), isReRender, partialTick, packedLight, packedOverlay, colour);
            return;
        }

        if (bone.getName().equals("right-hand")) {
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer,
                    isReRender, partialTick, packedLight, packedOverlay, colour);
            renderItemInHand(poseStack, animatable, bufferSource, packedLight, packedOverlay);
            return;
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    private void renderItemInHand(PoseStack poseStack, RobotEntity entity,
                                  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack heldItem = entity.getItemInMainHand();
        if (heldItem.isEmpty()) return;

        poseStack.pushPose();

        poseStack.translate(0.28, 0.19, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.scale(0.5f, 0.5f, 0.5f);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                heldItem,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();
    }


    @Override
    public @Nullable RenderType getRenderType(RobotEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}