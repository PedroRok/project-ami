package com.pedrorok.ami.system.dialog;

import com.pedrorok.ami.system.dialog.actions.DialogueAction;
import com.pedrorok.ami.system.dialog.actions.DialogueActionRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class DialogueNode {
    protected String text;
    protected String processedText;
    protected List<DialogueAction> actions;
    protected String option1;
    protected String option2;
    protected String option3;

    private DialogueNode nextNode1;
    private DialogueNode nextNode2;
    private DialogueNode nextNode3;
    
    /**
     * Processa o texto de forma sequencial, criando uma lista de segmentos com ações
     */
    public java.util.List<TextSegment> processTextSequentially(Player player, @Nullable LivingEntity robot) {
        java.util.List<TextSegment> segments = new java.util.ArrayList<>();
        String workingText = text;

        // Primeiro, substitui o nome do jogador
        workingText = workingText.replace("<player>", player.getName().getString());

        // Constrói o padrão regex dinamicamente baseado nas ações registradas
        String[] prefixes = DialogueActionRegistry.getRegisteredPrefixes();
        StringBuilder patternBuilder = new StringBuilder("<(");
        for (int i = 0; i < prefixes.length; i++) {
            if (i > 0) patternBuilder.append("|");
            patternBuilder.append(DialogueActionRegistry.getAction(prefixes[i]).getCommandPattern());
        }
        patternBuilder.append(")>");
        
        Pattern commandPattern = Pattern.compile(patternBuilder.toString());
        Matcher matcher = commandPattern.matcher(workingText);

        int lastEnd = 0;

        while (matcher.find()) {
            String command = matcher.group(1);
            
            // Adiciona o texto antes do comando como um segmento
            String textBefore = workingText.substring(lastEnd, matcher.start());
            if (!textBefore.isEmpty()) {
                segments.add(new TextSegment(textBefore, null));
            }

            // Obtém a ação correspondente
            DialogueAction action = DialogueActionRegistry.getAction(command);
            if (action != null) {
                // Adiciona um segmento vazio com a ação (para pausar a digitação)
                segments.add(new TextSegment("", action));
            }

            lastEnd = matcher.end();
        }

        // Adiciona o resto do texto como um segmento final
        String remainingText = workingText.substring(lastEnd);
        if (!remainingText.isEmpty()) {
            segments.add(new TextSegment(remainingText, null));
        }
        
        return segments;
    }
    
    /**
     * Representa um segmento de texto com uma ação opcional
     */
    public static class TextSegment {
        public final String text;
        public final DialogueAction action;
        
        public TextSegment(String text, DialogueAction action) {
            this.text = text;
            this.action = action;
        }
        
        public boolean hasAction() {
            return action != null;
        }
        
        public boolean hasText() {
            return text != null && !text.isEmpty();
        }
    }
    
    /**
     * Executa todas as ações do diálogo
     */
    public void executeActions(Player player, @Nullable LivingEntity robot) {
        for (DialogueAction action : actions) {
            DialogueAction.ActionContext context = new DialogueAction.ActionContext(
                player, robot, "", 0
            );
            action.execute(context);
        }
    }

    public DialogueNode(String text, String option1, String option2, String option3) {
        this.text = text;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.processedText = text; // Texto processado inicia igual ao original
    }
    
    /**
     * Obtém o texto processado (com comandos substituídos)
     */
    public String getProcessedText() {
        return processedText != null ? processedText : text;
    }

    public void setOptionCallback(int optionNumber, DialogueNode nextNode) {
        switch (optionNumber) {
            case 1 -> this.nextNode1 = nextNode;
            case 2 -> this.nextNode2 = nextNode;
            case 3 -> this.nextNode3 = nextNode;
        }
    }

    public DialogueNode getNextNode(int optionNumber) {
        return switch (optionNumber) {
            case 1 -> nextNode1;
            case 2 -> nextNode2;
            case 3 -> nextNode3;
            default -> null;
        };
    }
}
