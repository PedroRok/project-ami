package com.pedrorok.ami.system.dialog.nodes;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.system.dialog.DialogueActionRegistry;
import com.pedrorok.ami.system.dialog.actions.DialogueAction;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

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
    @Getter
    protected List<NodeOption> options;

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
    public record TextSegment(String text, DialogueAction action) {

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

    public DialogueNode(String text, NodeOption... options) {
        this.text = text;
        this.options = List.of(options);
        if (this.options.isEmpty()) {
            ProjectAmi.LOGGER.warn("DialogueNode created without options! Text: {}", text);
        }
        if (this.options.size() > 3) {
            ProjectAmi.LOGGER.warn("DialogueNode created with more than 3 options! Only the first 3 will be used. Text: {}", text);
        }
        this.processedText = text; // Texto processado inicia igual ao original
    }

    /**
     * Obtém o texto processado (com comandos substituídos)
     */
    public String getProcessedText() {
        return processedText != null ? processedText : text;
    }
}
