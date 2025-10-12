package com.pedrorok.ami.system.dialog.actions;

/**
 * Ação para substituir placeholders pelo nome do jogador
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class PlayerNameAction implements DialogueAction {
    
    @Override
    public boolean execute(ActionContext context) {
        // Ação de substituição não precisa de execução, apenas processamento de texto
        return true;
    }
    
    @Override
    public String processText(String text, String command) {
        // Esta ação precisa do contexto do jogador, mas não podemos acessá-lo aqui
        // O processamento será feito no DialogueNode
        return text;
    }
    
    @Override
    public String getCommandPattern() {
        return "player";
    }
}
