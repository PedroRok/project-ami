package com.pedrorok.ami.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.client.model.RobotEntityModel;
import com.pedrorok.ami.entities.RobotEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Comparator;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/10/2025
 * @project project-ami
 */
public class RobotEntityRenderer extends GeoEntityRenderer<RobotEntity> {

    private final ResourceLocation FACE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "textures/entity/ami/eye_default.png");

    public RobotEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RobotEntityModel());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RobotEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "textures/entity/ami/ami_texture.png");
    }

    @Override
    public void renderRecursively(PoseStack poseStack, RobotEntity animatable, GeoBone bone, RenderType renderType,
            MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {

        if (bone.getName().equals("face")) {
            RenderType faceRenderType = RenderType.entityTranslucent(FACE_TEXTURE);
            VertexConsumer faceConsumer = bufferSource.getBuffer(faceRenderType);

            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, faceConsumer, isReRender, partialTick, packedLight, packedOverlay, colour);
            return;
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public @Nullable RenderType getRenderType(RobotEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }


    @Override
    protected void applyRotations(RobotEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);

        GeoModel<RobotEntity> model = getGeoModel();
        var faceBone = model.getBone("face").orElse(null);
        var headBone = model.getBone("head").orElse(null);
        if (headBone != null) {
            float yaw = entity.getYRot();
            float pitch = entity.getYRot();
            headBone.setPosX((float) -Math.toRadians(yaw - entity.getYRot()));
            headBone.setPosY((float) -Math.toRadians(pitch));
        }

        Vec3 eyePos = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 target = findClosestPlayerPos(entity.level(), eyePos); // substituir pela posição do objeto de interesse mais próximo

        if (target == null) return;

        Vec3 dir = target.subtract(eyePos).normalize();

        float yaw = (float) (Math.atan2(dir.z, dir.x) * (180f / Math.PI)) - 90f;
        float pitch = (float) -(Math.asin(dir.y) * (180f / Math.PI));

        float diffYaw = yaw - entity.getYRot();
        if (diffYaw > 85 || diffYaw < -85) return;

        if (faceBone != null) {
            faceBone.setPosX((float) -Math.toRadians(yaw - entity.getYRot()));
            faceBone.setPosY((float) -Math.toRadians(pitch));
        }
    }



    private Vec3 findClosestPlayerPos(Level level, Vec3 from) {
        Player closest = level.players().stream()
                .filter(p -> !p.isSpectator())
                .min(Comparator.comparingDouble(p -> p.position().distanceToSqr(from)))
                .orElse(null);

        return closest != null ? closest.position().add(0, closest.getEyeHeight() +0.2f, 0) : null;
    }
}