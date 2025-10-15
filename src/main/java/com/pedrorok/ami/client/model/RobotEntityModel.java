package com.pedrorok.ami.client.model;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import java.util.Comparator;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/10/2025
 * @project project-ami
 */
public class RobotEntityModel extends GeoModel<RobotEntity> {
	private final ResourceLocation MODEL = ProjectAmi.resource("geo/entity/ami/ami.geo.json");
	private final ResourceLocation TEXTURE = ProjectAmi.resource("textures/entity/ami/ami_texture.png");
	private final ResourceLocation ANIMATION = ProjectAmi.resource("animations/entity/ami/ami.animation.json");
	
	@Override
    public ResourceLocation getModelResource(RobotEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RobotEntity animatable) {
        // For now, use the main body texture
        // TODO: Implement combined texture or per-bone texture system
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RobotEntity animatable) {
        return ANIMATION;
    }
	
	@Override
	public void setCustomAnimations(RobotEntity animatable, long instanceId, AnimationState<RobotEntity> animationState) {
		EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
		if (data == null) return;

		if (animatable.tickCount < 60) return;
		
		final AnimationProcessor<RobotEntity> processor = getAnimationProcessor();
		float headPitch = data.headPitch() * Mth.DEG_TO_RAD;
		float headYaw = (data.netHeadYaw() * Mth.DEG_TO_RAD) / 2F;
		
		GeoBone head = processor.getBone("head");
		if (head != null) {
			head.setRotX(headPitch);
			head.setRotY(headYaw);
		}

		GeoBone leftArm = processor.getBone("left-arm");
		GeoBone rightArm = processor.getBone("right-arm");
		GeoBone body = processor.getBone("chest");
		Vec3 deltaMovement = animatable.getDeltaMovement();
		if (leftArm != null && rightArm != null && body != null && animatable.getDeltaMovement().length() > 0.01) {
			Vec3 direction = Vec3.directionFromRotation(animatable.getXRot(), animatable.getYRot() );

			// TODO: improve this
			deltaMovement = deltaMovement.subtract(direction.scale(deltaMovement.dot(direction))).scale(-1);
			rightArm.setRotX((float) deltaMovement.x);
			rightArm.setRotZ((float) deltaMovement.z);

			leftArm.setRotX((float) deltaMovement.x);
			leftArm.setRotZ((float) deltaMovement.z);


			body.setRotX((float) deltaMovement.x);
			body.setRotZ((float) deltaMovement.z);
		}
		
		GeoBone face = processor.getBone("face");
		if (face != null) {
			Player target = findClosestPlayerPos(animatable.level(), animatable.position());
			if (target == null) return;
			
			Vec3 toPlayer = target.getEyePosition().subtract(animatable.getEyePosition());
			toPlayer = toPlayer.normalize();
			
			float targetYaw = Mth.wrapDegrees((float) (Math.atan2(toPlayer.z, toPlayer.x) * Mth.RAD_TO_DEG) - 90f);
			float targetPitch = (float) (Math.asin(toPlayer.y) * Mth.RAD_TO_DEG);
			
			float diffYaw = Mth.wrapDegrees(animatable.yBodyRot) - targetYaw;
			if (Math.abs(diffYaw) < 85) {
				float faceYawOffset = headYaw - (float) Math.toRadians(diffYaw);
				face.setPosX(-faceYawOffset);
			}
			
			float diffPitch = targetPitch - animatable.getXRot();
			if (Math.abs(diffPitch) < 85) {
				float facePitchOffset = headPitch - (float) Math.toRadians(diffPitch);
				face.setPosY(-facePitchOffset);
			}
		}
	}
	
	private Player findClosestPlayerPos(Level level, Vec3 from) {
		return level.players().stream().filter(p -> !p.isSpectator()).min(Comparator.comparingDouble(p -> p.position().distanceToSqr(from))).orElse(null);
	}
}