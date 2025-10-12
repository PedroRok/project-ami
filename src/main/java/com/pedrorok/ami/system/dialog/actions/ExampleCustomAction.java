package com.pedrorok.ami.system.dialog.actions;

import com.pedrorok.ami.ProjectAmi;

/**
 * Exemplo de ação customizada - mostra como adicionar novas ações facilmente
 * Esta ação simplesmente loga uma mensagem quando executada
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class ExampleCustomAction implements DialogueAction {
    
    private final String message;
    
    public ExampleCustomAction() {
        this.message = "Ação customizada executada!";
    }
    
    public ExampleCustomAction(String message) {
        this.message = message;
    }
    
    @Override
    public boolean execute(ActionContext context) {
        ProjectAmi.LOGGER.info("CustomAction: {}", message);
        return true;
    }

    
    @Override
    public String getCommandPattern() {
        return "custom_\\w+";
    }
    
    /**
     * Cria uma nova instância da ação com mensagem específica
     */
    public static ExampleCustomAction withMessage(String message) {
        return new ExampleCustomAction(message);
    }
}
