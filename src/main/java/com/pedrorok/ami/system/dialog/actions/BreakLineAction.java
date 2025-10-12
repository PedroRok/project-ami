package com.pedrorok.ami.system.dialog.actions;

import com.pedrorok.ami.client.gui.DialogueScreen;

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
    public void process(DialogueScreen screen) {
        screen.currentText += "\n";
        screen.currentSegmentIndex++;
        screen.currentSegmentTextIndex = 0;
    }
    
    @Override
    public String getCommandPattern() {
        return "br";
    }
}
