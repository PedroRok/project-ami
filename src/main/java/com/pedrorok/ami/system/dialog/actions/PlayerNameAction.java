package com.pedrorok.ami.system.dialog.actions;

/**
 * Ação para substituir placeholders pelo nome do jogador
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class PlayerNameAction implements DialogueAction {

    public PlayerNameAction(String ignore) {}

    @Override
    public boolean execute(ActionContext context) {
        return true;
    }
    
    @Override
    public String getCommandPattern() {
        return "player";
    }
}
