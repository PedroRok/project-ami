package com.pedrorok.ami.system;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatHistory {
    private final UUID robotUUID;
    private final List<RobotChatMessage> messages;
    private static final int MAX_MESSAGES = 100;
    
    public ChatHistory(UUID robotUUID) {
        this.robotUUID = robotUUID;
        this.messages = new ArrayList<>();
    }
    
    public void addMessage(RobotChatMessage message) {
        messages.add(message);
        
        // Keep only the last MAX_MESSAGES messages
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }
    
    public void addMessage(String sender, String text) {
        addMessage(new RobotChatMessage(sender, text));
    }
    
    public List<RobotChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }
    
    public UUID getRobotUUID() {
        return robotUUID;
    }
    
    public void clear() {
        messages.clear();
    }
    
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("robot_uuid", robotUUID);
        
        ListTag messagesTag = new ListTag();
        for (RobotChatMessage message : messages) {
            messagesTag.add(message.toNBT());
        }
        tag.put("messages", messagesTag);
        
        return tag;
    }
    
    public static ChatHistory fromNBT(CompoundTag tag) {
        UUID robotUUID = tag.getUUID("robot_uuid");
        ChatHistory history = new ChatHistory(robotUUID);
        
        ListTag messagesTag = tag.getList("messages", Tag.TAG_COMPOUND);
        for (int i = 0; i < messagesTag.size(); i++) {
            CompoundTag messageTag = messagesTag.getCompound(i);
            history.addMessage(RobotChatMessage.fromNBT(messageTag));
        }
        
        return history;
    }
}
