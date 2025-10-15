package com.pedrorok.ami.system.dialog;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.client.gui.DialogueScreen;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
                "<anim_happy><mood_happy_3>Hello <player>!<wait_2><br>My name is AMI<br><wait_2>I’m a friendly robot here to help you.<br>How can I assist you today?",
                "Can you do something for me?",
                "What can you do?",
                "Exit"
        );

        DialogueNode shopItems = new DialogueNode(
                "<anim_happy>I’ve got lots of interesting things here!<br>What would you like to do?",
                "See mining tools",
                "See building materials",
                "Back"
        );

        DialogueNode question = new DialogueNode(
                "<anim_happy>Of course! Ask whatever you’d like.<br><wait_1>I’ve been here for many years and know this place very well.",
                "Where are you from?",
                "What do you know about this area?",
                "Back"
        );

        DialogueNode tools = new DialogueNode(
                "<anim_happy>Mining tools!<br>I have pickaxes and other useful tools.",
                "Buy iron pickaxe",
                "Buy diamond pickaxe",
                "Back"
        );

        DialogueNode materials = new DialogueNode(
                "<anim_wave>Building materials!<br>Blocks, bricks, and much more.",
                "See building blocks",
                "See decorative materials",
                "Back"
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
            log.info("DialogueHandler: Dialogue '{}' not found!", dialogueId);
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