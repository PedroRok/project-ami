package com.pedrorok.ami.system.dialog.actions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("CustomAction: {}", message);
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

