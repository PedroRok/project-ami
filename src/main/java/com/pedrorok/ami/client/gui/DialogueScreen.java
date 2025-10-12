package com.pedrorok.ami.client.gui;

import com.pedrorok.ami.system.dialog.DialogueNode;
import com.pedrorok.ami.system.dialog.actions.BreakLineAction;
import com.pedrorok.ami.system.dialog.actions.DialogueAction;
import com.pedrorok.ami.system.dialog.actions.WaitAction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class DialogueScreen extends Screen {

    private final String fullText;
    private String currentText = "";
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
    private boolean hasActionsExecuting = false;
    private int actionIndicatorTicks = 0;
    
    // Sistema sequencial de texto com ações
    private java.util.List<DialogueNode.TextSegment> textSegments = new java.util.ArrayList<>();
    private int currentSegmentIndex = 0;
    private int currentSegmentTextIndex = 0;
    private boolean waitingForAction = false;
    private WaitAction currentWaitAction = null;

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
        int buttonY = this.height / 2 + 60;

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
                new DialogueAction.ActionContext(
                    net.minecraft.client.Minecraft.getInstance().player, entity, "", 0);
            
            if (currentSegment.action.execute(context)) {
                if (currentSegment.action instanceof WaitAction waitAction) {
                    // É uma ação de espera - pausa a digitação
                    currentWaitAction = waitAction;
                    waitingForAction = true;
                    hasActionsExecuting = true;
                } else if (currentSegment.action instanceof BreakLineAction) {
                    // É uma quebra de linha - adiciona \n ao texto e continua
                    currentText += "\n";
                    currentSegmentIndex++;
                    currentSegmentTextIndex = 0;
                } else {
                    // Outras ações (como animações) - executa e continua
                    currentSegmentIndex++;
                    currentSegmentTextIndex = 0;
                }
            }
        } else if (currentSegment.hasText()) {
            // Este segmento tem texto - digita ele
            String segmentText = currentSegment.text;
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

        int boxWidth = 300;
        int boxHeight = 100;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = (this.height - boxHeight) / 2 - 40;

        // Renderiza a entidade do lado esquerdo se existir
        if (entity != null) {
            int entityX = boxX - 60; // Posição X (à esquerda da caixa)
            int entityY = boxY + boxHeight - 50; // Posição Y (base da caixa)
            int entitySize = 50; // Tamanho da entidade

            // Fundo para a entidade (opcional)
            int bgWidth = 90;
            guiGraphics.fill(entityX - bgWidth/2, boxY, entityX + bgWidth/2, boxY + boxHeight, 0x88000000);
            guiGraphics.fill(entityX - bgWidth/2 + 2, boxY + 2, entityX + bgWidth/2 - 2, boxY + boxHeight - 2, 0x55000000);

            // Debug visual
            guiGraphics.drawString(this.font, "Entity: " + entity.getName().getString(), entityX - 40, boxY - 15, 0xFFFFFF, false);

            // Renderiza a entidade olhando para o mouse
            try {
                renderEntity(guiGraphics, entityX, entityY, entitySize, mouseX, mouseY);
            } catch (Exception e) {
                System.out.println("Erro ao renderizar entidade: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Debug: mostra que não há entidade
            guiGraphics.drawString(this.font, "No Entity", boxX - 80, boxY + 10, 0xFF0000, false);
        }

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
            String actionText = "Executando ações...";
            int actionX = boxX + 10;
            int actionY = boxY + boxHeight - 15;
            
            // Efeito de piscar
            boolean visible = (actionIndicatorTicks / 10) % 2 == 0;
            if (visible) {
                guiGraphics.drawString(this.font, actionText, actionX, actionY, 0x00FF00, false);
            }
        }
    }

    /**
     * Renderiza a entidade na tela (similar ao inventário do jogador)
     */
    private void renderEntity(GuiGraphics guiGraphics, int x, int y, int size, float mouseX, float mouseY) {
        // Calcula a rotação baseada na posição do mouse
        float xRotation = (float) Math.atan((double)((y - size / 2) - mouseY) / 40.0);
        float yRotation = (float) Math.atan((double)(x - mouseX) / 40.0);

        // Rotação da entidade
        Quaternionf pose = (new Quaternionf()).rotateZ((float) Math.PI);
        Quaternionf cameraOrientation = (new Quaternionf()).rotateX(xRotation * 20.0F * ((float) Math.PI / 180F));
        pose.mul(cameraOrientation);

        float yBodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;

        // Aplica as rotações
        entity.yBodyRot = 180.0F + yRotation * 20.0F;
        entity.setYRot(180.0F + yRotation * 40.0F);
        entity.setXRot(-xRotation * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        // Renderiza a entidade
        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F, 0.0F);
        InventoryScreen.renderEntityInInventory(guiGraphics, x, y, size, vector3f, pose, cameraOrientation, entity);

        // Restaura as rotações originais
        entity.yBodyRot = yBodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
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
            textIndex = fullText.length();
            currentText = fullText;
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