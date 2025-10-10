package com.pedrorok.ami.client.model;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.RobotEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/10/2025
 * @project project-ami
 */
public class RobotEntityModel extends GeoModel<RobotEntity> {
    @Override
    public ResourceLocation getModelResource(RobotEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "geo/entity/ami/ami.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RobotEntity animatable) {
        // For now, use the main body texture
        // TODO: Implement combined texture or per-bone texture system
        return ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "textures/entity/ami/ami_texture.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RobotEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "animations/entity/ami/ami.animation.json");
    }
}