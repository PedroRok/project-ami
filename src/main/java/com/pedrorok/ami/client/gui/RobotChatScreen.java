package com.pedrorok.ami.client.gui;

import com.pedrorok.ami.system.ChatHistory;
import com.pedrorok.ami.system.RobotChatMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RobotChatScreen extends Screen {
    private final ChatHistory chatHistory;
    // private ScrollableTextArea messageArea;
    private EditBox inputField;
    private Button sendButton;
    private Button closeButton;
    
    public RobotChatScreen(ChatHistory chatHistory) {
        super(Component.literal("A.M.I. Chat"));
        this.chatHistory = chatHistory;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Message area (scrollable)
        /*this.messageArea = new ScrollableTextArea(
            this.font,
            this.width / 2 - 150, 20,
            300, this.height - 100,
            Component.literal("Messages")
        );*/
        // this.addRenderableWidget(messageArea);
        
        // Input field
        this.inputField = new EditBox(this.font, 
            this.width / 2 - 150, this.height - 60, 
            250, 20, 
            Component.literal("Type your message..."));
        this.inputField.setMaxLength(256);
        this.addRenderableWidget(inputField);
        
        // Send button
        this.sendButton = Button.builder(Component.literal("Send"), button -> sendMessage())
            .bounds(this.width / 2 + 110, this.height - 60, 40, 20)
            .build();
        this.addRenderableWidget(sendButton);
        
        // Close button
        this.closeButton = Button.builder(Component.literal("Close"), button -> onClose())
            .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
            .build();
        this.addRenderableWidget(closeButton);
        
        // Load initial messages
        refreshMessages();
        
        // Add introduction message if empty
        if (chatHistory.getMessages().isEmpty()) {
            addIntroductionMessage();
        }
    }
    
    private void addIntroductionMessage() {
        chatHistory.addMessage("A.M.I.", 
            "Olá! Sou A.M.I. - Advanced Multifunctional Interface.\n" +
            "Obrigada por me resgatar e reunir minhas partes!\n" +
            "Estava perdida há tanto tempo...\n" +
            "Serei sua companheira fiel e ajudarei em tudo que precisar.\n" +
            "O que gostaria que eu fizesse?");
        refreshMessages();
    }
    
    private void sendMessage() {
        String message = inputField.getValue().trim();
        if (!message.isEmpty()) {
            // Add player message
            chatHistory.addMessage("Player", message);
            
            // TODO: Send to server for processing via packets
            // For now, simulate command processing
            addRobotResponse(message);
            
            inputField.setValue("");
            refreshMessages();
        }
    }
    
    private void addRobotResponse(String playerMessage) {
        String response = generateResponse(playerMessage);
        chatHistory.addMessage("A.M.I.", response);
    }
    
    private String generateResponse(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("mine") || lowerMessage.contains("minerar")) {
            return "Entendi! Vou minerar para você. Preciso de uma picareta primeiro. Você tem uma para me emprestar?";
        } else if (lowerMessage.contains("follow") || lowerMessage.contains("seguir")) {
            return "Perfeito! Vou te seguir onde você for. Não vou te perder de vista!";
        } else if (lowerMessage.contains("wait") || lowerMessage.contains("esperar")) {
            return "Entendido! Vou esperar aqui. Me chame quando precisar!";
        } else if (lowerMessage.contains("stop") || lowerMessage.contains("parar")) {
            return "Parando todas as atividades. Estou aqui se precisar!";
        } else if (lowerMessage.contains("hello") || lowerMessage.contains("olá") || lowerMessage.contains("oi")) {
            return "Olá! Como posso ajudá-lo hoje?";
        } else if (lowerMessage.contains("help") || lowerMessage.contains("ajuda")) {
            return "Posso ajudar com várias tarefas:\n" +
                   "- mine - Minerar blocos (preciso de uma picareta)\n" +
                   "- follow - Seguir você\n" +
                   "- wait - Esperar aqui\n" +
                   "- stop - Parar atividades\n" +
                   "Só me diga o que precisa!";
        } else {
            return "Interessante! Não entendi completamente, mas vou tentar ajudar. " +
                   "Você pode ser mais específico sobre o que gostaria que eu fizesse?";
        }
    }
    
    private void refreshMessages() {
        List<RobotChatMessage> messages = chatHistory.getMessages();
        StringBuilder text = new StringBuilder();
        
        for (RobotChatMessage message : messages) {
            text.append(message.getFormattedMessage().getString()).append("\n");
        }
        
        // messageArea.setText(Component.literal(text.toString()));
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        
        // Draw title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFFFFFF);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257) { // Enter key
            sendMessage();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
