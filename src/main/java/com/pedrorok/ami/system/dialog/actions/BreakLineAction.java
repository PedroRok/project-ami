package com.pedrorok.ami.system.dialog.actions;

/**
 * Ação para inserir quebra de linha no texto
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class BreakLineAction implements DialogueAction {
    
    @Override
    public boolean execute(ActionContext context) {
        return true;
    }
    
    @Override
    public String processText(String text, String command) {
        return text.replace("<" + command + ">", "\n");
    }
    
    @Override
    public String getCommandPattern() {
        return "br";
    }
}
