package com.pedrorok.ami.system.dialog;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.client.gui.DialogueScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class DialogueHandler {

    private static DialogueHandler INSTANCE;
    private final Map<String, DialogueNode> dialogues = new HashMap<>();
    private LivingEntity currentEntity;

    private DialogueHandler() {
        // TODO: Load dialogues from json or other data source
        registerDialogues();
    }

    public static DialogueHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DialogueHandler();
        }
        return INSTANCE;
    }

    private void registerDialogues() {
        DialogueNode greeting = new DialogueNode(
                "<anim_happy>Olá <player>!<wait_2><br>Meu nome é AMI<br><wait_2>Eu sou um robô amigável aqui para te ajudar.<br>Como posso te ajudar hoje?",
                "Fazer uma pergunta",
                "Ver itens disponíveis", 
                "Sair"
        );

        DialogueNode shopItems = new DialogueNode(
                "<anim_happy>Tenho várias coisas interessantes aqui!<br>O que você gostaria de fazer?",
                "Ver ferramentas de mineração",
                "Ver materiais de construção",
                "Voltar"
        );

        DialogueNode question = new DialogueNode(
                "<anim_happy>Claro! Pergunta o que quiser.<br><wait_1>Estou aqui há muitos anos e conheço este lugar muito bem.",
                "De onde você vem?",
                "O que você sabe sobre esta área?",
                "Voltar"
        );

        DialogueNode tools = new DialogueNode(
                "<anim_happy>Ferramentas de mineração!<br>Tenho picaretas e outras ferramentas úteis.",
                "Comprar picareta de ferro",
                "Comprar picareta de diamante",
                "Voltar"
        );

        DialogueNode materials = new DialogueNode(
                "<anim_wave>Materiais de construção!<br>Blocos, tijolos e muito mais.",
                "Ver blocos de construção",
                "Ver materiais decorativos",
                "Voltar"
        );

        // Conecta os nós do diálogo
        greeting.setOptionCallback(1, question);
        greeting.setOptionCallback(2, shopItems);
        greeting.setOptionCallback(3, null);

        shopItems.setOptionCallback(1, tools);
        shopItems.setOptionCallback(2, materials);
        shopItems.setOptionCallback(3, greeting);

        question.setOptionCallback(3, greeting);
        tools.setOptionCallback(3, shopItems);
        materials.setOptionCallback(3, shopItems);

        dialogues.put("greeting", greeting);
    }

    public void openDialogue(String dialogueId, LivingEntity entity) {
        this.currentEntity = entity;
        DialogueNode node = dialogues.get(dialogueId);


        if (node != null) {
            showDialogueNode(node);
        } else {
            ProjectAmi.LOGGER.info("DialogueHandler: Dialogue '{}' not found!", dialogueId);
        }
    }

    public void openDialogue(String dialogueId) {
        openDialogue(dialogueId, null);
    }

    private void showDialogueNode(DialogueNode node) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player != null) {
            java.util.List<DialogueNode.TextSegment> segments = node.processTextSequentially(mc.player, currentEntity);

            DialogueScreen screen = new DialogueScreen(
                    segments,
                    node.option1,
                    node.option2,
                    node.option3,
                    (selectedOption) -> {
                        DialogueNode nextNode = node.getNextNode(selectedOption);

                        if (nextNode != null) {
                            mc.execute(() -> showDialogueNode(nextNode));
                        }
                    },
                    currentEntity
            );

            mc.setScreen(screen);
        }
    }
}