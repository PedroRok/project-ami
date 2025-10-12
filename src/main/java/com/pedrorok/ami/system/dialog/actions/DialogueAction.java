package com.pedrorok.ami.system.dialog.actions;

import com.pedrorok.ami.client.gui.DialogueScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Interface base para todas as ações de diálogo
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public interface DialogueAction {
    
    /**
     * Executa a ação no contexto do diálogo
     * @param context Contexto da execução da ação
     * @return true se a ação foi executada com sucesso
     */
    boolean execute(ActionContext context);
    
    /**
     * Processa o texto do diálogo
     * @param screen Tela de diálogo atual
     */
    default void process(DialogueScreen screen) {
        screen.currentSegmentIndex++;
        screen.currentSegmentTextIndex = 0;
    };

    /**
     * Retorna o padrão regex que esta ação reconhece
     * @return Pattern para reconhecer comandos desta ação
     */
    String getCommandPattern();
    
    /**
     * Contexto para execução de ações de diálogo
     */
    record ActionContext(Player player, LivingEntity entity, String command, int position) {}
}
