package com.pedrorok.ami.system.dialog.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registro dinâmico de ações de diálogo
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class DialogueActionRegistry {
    
    private static final Map<String, Supplier<DialogueAction>> actionFactories = new HashMap<>();
    private static final Map<String, DialogueAction> actionInstances = new HashMap<>();
    
    static {
        // Registra ações padrão
        registerAction("anim_", AnimationAction::new);
        registerAction("wait_", WaitAction::new);
        registerAction("br", BreakLineAction::new);
        registerAction("player", PlayerNameAction::new);
        
        // Exemplo de ação customizada - descomente para usar
        // registerAction("custom_", ExampleCustomAction::new);
    }
    
    /**
     * Registra uma nova ação de diálogo
     * @param prefix Prefixo do comando (ex: "anim_", "wait_")
     * @param factory Factory para criar instâncias da ação
     */
    public static void registerAction(String prefix, Supplier<DialogueAction> factory) {
        actionFactories.put(prefix, factory);
    }
    
    /**
     * Obtém uma instância da ação para um comando específico
     * @param command Comando completo (ex: "anim_happy", "wait_2")
     * @return Instância da ação ou null se não encontrada
     */
    public static DialogueAction getAction(String command) {
        // Primeiro, tenta encontrar uma ação registrada pelo prefixo
        for (Map.Entry<String, Supplier<DialogueAction>> entry : actionFactories.entrySet()) {
            String prefix = entry.getKey();
            if (command.startsWith(prefix)) {
                // Para comandos específicos, cria uma nova instância com parâmetros
                DialogueAction baseAction = entry.getValue().get();
                
                // Se for animação, cria com o nome específico
                if (prefix.equals("anim_") && baseAction instanceof AnimationAction) {
                    String animationName = command.substring(5); // Remove "anim_"
                    return AnimationAction.forAnimation(animationName);
                }
                
                // Se for wait, cria com o tempo específico
                if (prefix.equals("wait_") && baseAction instanceof WaitAction) {
                    try {
                        int seconds = Integer.parseInt(command.substring(5)); // Remove "wait_"
                        return WaitAction.forSeconds(seconds);
                    } catch (NumberFormatException e) {
                        return new WaitAction(); // Fallback para 1 segundo
                    }
                }
                
                // Para outras ações, retorna a instância base
                return baseAction;
            }
        }
        return null;
    }
    
    /**
     * Verifica se um comando é reconhecido pelo sistema
     * @param command Comando a ser verificado
     * @return true se o comando é reconhecido
     */
    public static boolean isCommandRecognized(String command) {
        return actionFactories.keySet().stream()
                .anyMatch(command::startsWith);
    }
    
    /**
     * Obtém todos os prefixos de comandos registrados
     * @return Set de prefixos registrados
     */
    public static String[] getRegisteredPrefixes() {
        return actionFactories.keySet().toArray(new String[0]);
    }
}