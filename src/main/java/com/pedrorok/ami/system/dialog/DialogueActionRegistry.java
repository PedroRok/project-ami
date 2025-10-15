package com.pedrorok.ami.system.dialog;

import com.pedrorok.ami.system.dialog.actions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registro dinâmico de ações de diálogo
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class DialogueActionRegistry {
    
    private static final Map<String, Function<String, DialogueAction>> actionFactories = new HashMap<>();
    private static final Map<String, DialogueAction> actionInstances = new HashMap<>();
    
    static {
        registerAction("anim_", (cmd) -> {
            String animationName = cmd.substring(5);
            return AnimationAction.forAnimation(animationName);
        });
        registerAction("wait_", (cmd) -> {
            try {
                int seconds = Integer.parseInt(cmd.substring(5)); // Remove "wait_"
                return WaitAction.forSeconds(seconds);
            } catch (NumberFormatException e) {
                return new WaitAction();
            }
        });
        registerAction("br", BreakLineAction::new);
        registerAction("player", PlayerNameAction::new);
        registerAction("mood_", (cmd) -> {
            String[] split = cmd.split("_");
            if (split.length == 3) {
                String moodName = split[1];
                try {
                    int timePlaying = Integer.parseInt(split[2]);
                    return MoodAction.forMood(moodName, timePlaying);
                } catch (NumberFormatException e) {
                    return new MoodAction();
                }
            }
            return new MoodAction();
        });
        
        // Exemplo de ação customizada - descomente para usar
        // registerAction("custom_", ExampleCustomAction::new);
    }
    
    /**
     * Registra uma nova ação de diálogo
     * @param prefix Prefixo do comando (ex: "anim_", "wait_")
     * @param factory Factory para criar instâncias da ação
     */
    public static void registerAction(String prefix, Function<String, DialogueAction> factory) {
        actionFactories.put(prefix, factory);
    }
    
    /**
     * Obtém uma instância da ação para um comando específico
     * @param command Comando completo (ex: "anim_happy", "wait_2")
     * @return Instância da ação ou null se não encontrada
     */
    public static DialogueAction getAction(String command) {
        for (Map.Entry<String, Function<String, DialogueAction>> entry : actionFactories.entrySet()) {
            String prefix = entry.getKey();
            if (command.startsWith(prefix)) {
                return entry.getValue().apply(command);
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