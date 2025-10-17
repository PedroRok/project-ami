package com.pedrorok.ami.system.dialog.nodes;

import com.pedrorok.ami.system.dialog.DialogueHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author Rok, Pedro Lucas nmm. Created on 16/10/2025
 * @project project-ami
 */
public class SimpleNodeOption extends NodeOption{
    private final String nextNode;
    public SimpleNodeOption(String text, String nextNode) {
        super(text);
        this.nextNode = nextNode;
    }

    @Override
    public void whenClicked(Minecraft mc, LivingEntity entity) {
        DialogueHandler.getInstance().openDialogue(nextNode, entity);
    }
}
