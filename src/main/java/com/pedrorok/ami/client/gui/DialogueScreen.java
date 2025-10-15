package com.pedrorok.ami.client.gui;

import com.mojang.datafixers.kinds.IdF;
import com.pedrorok.ami.entities.robot.IHaveEnergy;
import com.pedrorok.ami.entities.robot.RobotEnergy;
import com.pedrorok.ami.registry.ModSounds;
import com.pedrorok.ami.system.dialog.DialogueNode;
import com.pedrorok.ami.system.dialog.actions.DialogueAction;
import com.pedrorok.ami.system.dialog.actions.WaitAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class DialogueScreen extends Screen {

    private final String fullText;
    public String currentText = "";
    private int textIndex = 0;
    private int tickCounter = 0;
    private final int charsPerTick = 1;

    private boolean textComplete = false;
    private Button option1Button;
    private Button option2Button;
    private Button option3Button;

    private final String option1Text;
    private final String option2Text;
    private final String option3Text;

    private final DialogueCallback callback;
    private final LivingEntity entity;
    private boolean isTransitioning = false;
    public boolean hasActionsExecuting = false;
    private int actionIndicatorTicks = 0;

    // Sistema sequencial de texto com ações
    private List<DialogueNode.TextSegment> textSegments = new ArrayList<>();
    public int currentSegmentIndex = 0;
    public int currentSegmentTextIndex = 0;
    public boolean waitingForAction = false;
    public WaitAction currentWaitAction = null;

    public DialogueScreen(String dialogueText, String opt1, String opt2, String opt3, DialogueCallback callback, LivingEntity entity) {
        super(Component.literal("Dialogue"));
        this.fullText = dialogueText;
        this.option1Text = opt1;
        this.option2Text = opt2;
        this.option3Text = opt3;
        this.callback = callback;
        this.entity = entity;
    }

    public DialogueScreen(String dialogueText, String opt1, String opt2, String opt3, DialogueCallback callback) {
        this(dialogueText, opt1, opt2, opt3, callback, null);
    }

    /**
     * Construtor para usar com sistema sequencial de texto
     */
    public DialogueScreen(java.util.List<com.pedrorok.ami.system.dialog.DialogueNode.TextSegment> segments, String opt1, String opt2, String opt3, DialogueCallback callback, LivingEntity entity) {
        super(Component.literal("Dialogue"));
        this.textSegments = segments;
        this.fullText = ""; // Será construído dinamicamente
        this.option1Text = opt1;
        this.option2Text = opt2;
        this.option3Text = opt3;
        this.callback = callback;
        this.entity = entity;
    }

    @Override
    protected void init() {
        super.init();
        isTransitioning = false;

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int buttonY = this.height / 2 + 45;

        this.option1Button = Button.builder(Component.literal(option1Text), button -> {
            if (!isTransitioning && callback != null) {
                isTransitioning = true;
                callback.onOptionSelected(1);
                return;
            }
            this.onClose();
        }).bounds(centerX - buttonWidth / 2, buttonY, buttonWidth, buttonHeight).build();

        this.option2Button = Button.builder(Component.literal(option2Text), button -> {
            if (!isTransitioning && callback != null) {
                isTransitioning = true;
                callback.onOptionSelected(2);
                return;
            }
            this.onClose();
        }).bounds(centerX - buttonWidth / 2, buttonY + 25, buttonWidth, buttonHeight).build();

        this.option3Button = Button.builder(Component.literal(option3Text), button -> {
            if (!isTransitioning && callback != null) {
                isTransitioning = true;
                callback.onOptionSelected(3);
                return;
            }
            this.onClose();
        }).bounds(centerX - buttonWidth / 2, buttonY + 50, buttonWidth, buttonHeight).build();

        // Botões começam invisíveis
        this.option1Button.visible = false;
        this.option2Button.visible = false;
        this.option3Button.visible = false;

        this.addRenderableWidget(option1Button);
        this.addRenderableWidget(option2Button);
        this.addRenderableWidget(option3Button);
    }

    @Override
    public void tick() {
        super.tick();

        // Atualiza indicador de ações
        actionIndicatorTicks++;

        if (!textComplete) {
            // Se estamos esperando por uma ação, verifica se ela terminou
            if (waitingForAction && currentWaitAction != null) {
                if (!currentWaitAction.updateTicks()) {
                    // Ação terminou, continua com o próximo segmento
                    waitingForAction = false;
                    currentWaitAction = null;
                    hasActionsExecuting = false;
                    currentSegmentIndex++;
                    currentSegmentTextIndex = 0;
                } else {
                    hasActionsExecuting = true;
                }
            } else if (!waitingForAction) {
                // Continua digitando o texto normalmente
                tickCounter++;

                if (tickCounter >= charsPerTick) {
                    tickCounter = 0;
                    updateTextSequentially();
                }
            }
        }
    }

    /**
     * Atualiza o texto de forma sequencial, respeitando as ações
     */
    private void updateTextSequentially() {
        if (textSegments.isEmpty()) {
            // Modo antigo - texto simples
            if (textIndex < fullText.length()) {
                textIndex++;
                currentText = fullText.substring(0, textIndex);
            } else {
                textComplete = true;
                showButtons();
            }
            return;
        }

        // Modo sequencial - processa segmento por segmento
        if (currentSegmentIndex >= textSegments.size()) {
            textComplete = true;
            showButtons();
            return;
        }

        DialogueNode.TextSegment currentSegment = textSegments.get(currentSegmentIndex);

        if (currentSegment.hasAction()) {
            // Este segmento tem uma ação - executa
            DialogueAction.ActionContext context =
                    new DialogueAction.ActionContext(Minecraft.getInstance().player, entity, "", 0);

            if (currentSegment.action().execute(context)) {
                currentSegment.action().process(this);
            }
        } else if (currentSegment.hasText()) {
            if (entity.tickCount % 2 == 0)
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CHAT_SFX.get(), 1f));
            // Este segmento tem texto - digita ele
            String segmentText = currentSegment.text();
            if (currentSegmentTextIndex < segmentText.length()) {
                currentSegmentTextIndex++;
                currentText += segmentText.substring(currentSegmentTextIndex - 1, currentSegmentTextIndex);
            } else {
                // Segmento de texto terminou, vai para o próximo
                currentSegmentIndex++;
                currentSegmentTextIndex = 0;
            }
        } else {
            // Segmento vazio, pula para o próximo
            currentSegmentIndex++;
            currentSegmentTextIndex = 0;
        }
    }

    /**
     * Mostra os botões de opção
     */
    private void showButtons() {
        option1Button.visible = true;
        option2Button.visible = true;
        option3Button.visible = true;
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int boxWidth = 250;
        int boxHeight = 100;
        int boxX = (this.width - boxWidth) / 2 + 53;
        int boxY = (this.height - boxHeight) / 2 - 40;

        // Renderiza a entidade do lado esquerdo se existir


        // Fundo da caixa de diálogo
        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xCC000000);
        guiGraphics.fill(boxX + 2, boxY + 2, boxX + boxWidth - 2, boxY + boxHeight - 2, 0xFF1a1a1a);

        // Desenha o texto animado (com quebra de linha)
        int textX = boxX + 10;
        int textY = boxY + 10;
        int maxWidth = boxWidth - 20;

        // Desenha o texto com quebra de linha
        drawWrappedText(guiGraphics, currentText, textX, textY, maxWidth, 0xFFFFFF);

        // Indicador de "pressione para continuar" quando o texto não terminou
        if (!textComplete) {
            String skipText = "Clique para pular...";
            int skipX = boxX + boxWidth - font.width(skipText) - 10;
            int skipY = boxY + boxHeight - 15;
            guiGraphics.drawString(this.font, skipText, skipX, skipY, 0x888888, false);
        }

        // Indicador visual para ações sendo executadas
        if (hasActionsExecuting) {
            String actionText = "...";
            int actionX = boxX + 10;
            int actionY = boxY + boxHeight - 15;

            // Efeito de piscar
            boolean visible = (actionIndicatorTicks / 10) % 2 == 0;
            if (visible) {
                guiGraphics.drawString(this.font, actionText, actionX, actionY, 0x00FF00, false);
            }
        }

        DialogueScreenEntityModule.renderEntity(entity, guiGraphics, boxX, boxY, boxHeight, mouseX, mouseY, this.font);
    }

    private void drawWrappedText(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        // Primeiro, divide o texto por quebras de linha explícitas (\n)
        String[] lines = text.split("\n");
        int currentY = y;

        for (String line : lines) {
            // Para cada linha, faz o wrap de palavras se necessário
            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;

                if (font.width(testLine) > maxWidth) {
                    if (currentLine.length() > 0) {
                        guiGraphics.drawString(this.font, currentLine.toString(), x, currentY, color, false);
                        currentY += 12; // Aumentei um pouco o espaçamento
                        currentLine = new StringBuilder(word);
                    } else {
                        // Palavra muito longa, quebra ela
                        String longWord = word;
                        while (font.width(longWord) > maxWidth) {
                            String part = "";
                            for (int i = 0; i < longWord.length(); i++) {
                                String testPart = part + longWord.charAt(i);
                                if (font.width(testPart) > maxWidth) {
                                    break;
                                }
                                part = testPart;
                            }
                            guiGraphics.drawString(this.font, part, x, currentY, color, false);
                            currentY += 12;
                            longWord = longWord.substring(part.length());
                        }
                        if (!longWord.isEmpty()) {
                            currentLine = new StringBuilder(longWord);
                        }
                    }
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }

            if (currentLine.length() > 0) {
                guiGraphics.drawString(this.font, currentLine.toString(), x, currentY, color, false);
                currentY += 12;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!textComplete && button == 0) {
            currentText = "";
            for (DialogueNode.TextSegment textSegment : textSegments) {
                if (textSegment.hasAction() && textSegment.action().processWhenSkipped()) {
                    DialogueAction.ActionContext context =
                            new DialogueAction.ActionContext(Minecraft.getInstance().player, entity, "", 0);
                    if (textSegment.action().execute(context)) {
                        textSegment.action().process(this);
                    }
                }
                currentText += textSegment.text();
            }
            textIndex = currentText.length();

            textComplete = true;
            option1Button.visible = true;
            option2Button.visible = true;
            option3Button.visible = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    @FunctionalInterface
    public interface DialogueCallback {
        void onOptionSelected(int option);
    }
}