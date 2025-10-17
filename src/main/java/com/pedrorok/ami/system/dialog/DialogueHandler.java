package com.pedrorok.ami.system.dialog;

import com.pedrorok.ami.client.gui.DialogueScreen;
import com.pedrorok.ami.system.dialog.nodes.CloseNodeOption;
import com.pedrorok.ami.system.dialog.nodes.DialogueNode;
import com.pedrorok.ami.system.dialog.nodes.NodeOption;
import com.pedrorok.ami.system.dialog.nodes.SimpleNodeOption;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
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

    public void registerDialogues() {
        DialogueNode greeting = new DialogueNode(
                "<anim_happy><mood_happy_3>Hello <player>!<wait_2><br>My name is AMI<br><wait_2>I’m a friendly robot here to help you.<wait_1><br>How can I assist you today?",
                new SimpleNodeOption("Can you do something for me?", "action-selection"),
                new SimpleNodeOption("What can you do?", "info1"),
                new CloseNodeOption("Nothing for now, thanks!")
        );

        DialogueNode actionSelection = new DialogueNode(
                "<anim_neutral><mood_neutral_1>Sure!<wait_1><br>What do you need help with?",
                new SimpleNodeOption("I need help with mining.", "mining-info"),
                new CloseNodeOption("Actually, never mind.")
        );

        DialogueNode miningInfo = new DialogueNode(
                "<anim_happy><mood_happy_2>Great!<wait_1><br>I can assist you with mining tasks.<wait_1><br>Just tell me what to mine and where to start.",
                new CloseNodeOption("Thanks, AMI!")
        );

        DialogueNode info1 = new DialogueNode(
                "<anim_neutral><mood_neutral_1>I can help you with various tasks like mining, building, and gathering resources.<wait_1><br>Just let me know what you need!",
                new SimpleNodeOption("Can you do something for me?", "action-selection"),
                new CloseNodeOption("Thanks for the info!")
        );

        // Register dialogues
        dialogues.put("mining-info", miningInfo);
        dialogues.put("info1", info1);
        dialogues.put("action-selection", actionSelection);
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
            List<DialogueNode.TextSegment> segments = node.processTextSequentially(mc.player, currentEntity);

            DialogueScreen screen = new DialogueScreen(
                    segments,
                    node.getOptions(),
                    currentEntity
            );

            mc.setScreen(screen);
        }
    }
}