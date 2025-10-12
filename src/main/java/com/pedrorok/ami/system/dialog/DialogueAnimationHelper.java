package com.pedrorok.ami.system.dialog;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class DialogueAnimationHelper {

    /**
     * Dispara uma animação na entidade GeckoLib
     *
     * @param entity A entidade que deve executar a animação
     * @param animationName Nome da animação (ex: "happy", "sad", "wave")
     * @return true se a animação foi disparada com sucesso
     */
    public static boolean triggerAnimation(LivingEntity entity, String animationName) {
        if (!(entity instanceof GeoAnimatable)) {
            return false;
        }

        try {
            if (entity instanceof DialogueAnimatable dialogueEntity) {
                dialogueEntity.playDialogueAnimation(animationName);
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Interface que suas entidades devem implementar para suportar animações de diálogo
     */
    public interface DialogueAnimatable {
        /**
         * Dispara uma animação de diálogo
         * @param animationName Nome da animação a ser executada
         */
        void playDialogueAnimation(String animationName);
    }
}