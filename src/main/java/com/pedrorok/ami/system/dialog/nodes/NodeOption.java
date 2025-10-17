package com.pedrorok.ami.system.dialog.nodes;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author Rok, Pedro Lucas nmm. Created on 16/10/2025
 * @project project-ami
 */
public abstract class NodeOption {

    public final String text;

    public NodeOption(String text) {
        this.text = text;
    }

    public abstract void whenClicked(Minecraft mc, LivingEntity entity);
}
