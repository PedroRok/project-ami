package com.pedrorok.ami.system.dialog.actions;

import com.pedrorok.ami.system.dialog.DialogueAnimationHelper;

/**
 * Ação para executar animações em entidades durante o diálogo
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class AnimationAction implements DialogueAction {
    
    private final String animationName;
    
    public AnimationAction() {
        this.animationName = "";
    }
    
    public AnimationAction(String animationName) {
        this.animationName = animationName;
    }
    
    @Override
    public boolean execute(ActionContext context) {
        if (context.entity() != null && !animationName.isEmpty()) {
            return DialogueAnimationHelper.triggerAnimation(context.entity(), animationName);
        }
        return false;
    }
    
    @Override
    public String processText(String text, String command) {
        return text.replace("<" + command + ">", "");
    }
    
    @Override
    public String getCommandPattern() {
        return "anim_\\w+";
    }

    public static AnimationAction forAnimation(String animationName) {
        return new AnimationAction(animationName);
    }
}
