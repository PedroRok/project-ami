package com.pedrorok.ami.system;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.time.Instant;

public class RobotChatMessage {
    private final String sender;
    private final String text;
    private final long timestamp;
    
    public RobotChatMessage(String sender, String text) {
        this.sender = sender;
        this.text = text;
        this.timestamp = Instant.now().toEpochMilli();
    }
    
    public RobotChatMessage(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getText() {
        return text;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Component getFormattedMessage() {
        return Component.literal(String.format("[%s] %s: %s", 
            formatTime(timestamp), sender, text));
    }
    
    private String formatTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return instant.toString().substring(11, 19); // HH:MM:SS format
    }
    
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("sender", sender);
        tag.putString("text", text);
        tag.putLong("timestamp", timestamp);
        return tag;
    }
    
    public static RobotChatMessage fromNBT(CompoundTag tag) {
        return new RobotChatMessage(
            tag.getString("sender"),
            tag.getString("text"),
            tag.getLong("timestamp")
        );
    }
}
