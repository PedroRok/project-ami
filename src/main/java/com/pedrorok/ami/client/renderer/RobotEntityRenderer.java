package com.pedrorok.ami.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.client.model.RobotEntityModel;
import com.pedrorok.ami.entities.robot.RobotEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/10/2025
 * @project project-ami
 */
public class RobotEntityRenderer extends GeoEntityRenderer<RobotEntity> {
	private final ResourceLocation MODEL_TEXTURE = ProjectAmi.resource("textures/entity/ami/ami_texture.png");
	private final ResourceLocation FACE_TEXTURE = ProjectAmi.resource("textures/entity/ami/eye_default.png");
	
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
			VertexConsumer faceConsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(FACE_TEXTURE));
			super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, faceConsumer, isReRender, partialTick, packedLight, packedOverlay, colour);
			return;
		}
		super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
	}
	
	@Override
	public @Nullable RenderType getRenderType(RobotEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(texture);
	}
}