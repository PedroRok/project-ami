package com.pedrorok.ami.system.dialog.nodes;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

/**
 *
 * @project project-ami
 * @author Rok, Pedro Lucas nmm. Created on 16/10/2025
 */
public class CloseNodeOption extends NodeOption {
    public CloseNodeOption(String text) {
        super(text);
    }

    @Override
    public void whenClicked(Minecraft mc, LivingEntity entity) {
        mc.player.closeContainer();
    }
}
