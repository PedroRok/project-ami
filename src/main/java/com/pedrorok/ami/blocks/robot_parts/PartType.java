package com.pedrorok.ami.blocks.robot_parts;

import net.minecraft.util.StringRepresentable;

public enum PartType implements StringRepresentable {
    HEAD("head"),
    ARM("arm"),
    BODY("body"),
    REACTOR("reactor");
    
    private final String name;
    
    PartType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
