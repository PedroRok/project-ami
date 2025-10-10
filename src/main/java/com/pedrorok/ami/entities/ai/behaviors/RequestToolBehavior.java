package com.pedrorok.ami.entities.ai.behaviors;

import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;

import java.util.Map;
import java.util.Optional;

public class RequestToolBehavior extends Behavior<LivingEntity> {
    private int requestCooldown = 0;
    private static final int REQUEST_COOLDOWN_TICKS = 200; // 10 seconds
    
    public RequestToolBehavior() {
        super(Map.of(
	        MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }
    
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
        // Check if entity needs a tool (pickaxe)
        ItemStack heldItem = entity.getMainHandItem();
        if (heldItem.getItem() instanceof PickaxeItem) {
            return false; // Already has a pickaxe
        }
        
        // Check cooldown
        if (requestCooldown > 0) {
            return false;
        }
        
        return true;
    }
    
    @Override
    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        requestCooldown = REQUEST_COOLDOWN_TICKS;
        
        // TODO: Send chat message to player requesting tool
        // This would integrate with the chat system
        Player nearestPlayer = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
        if (nearestPlayer != null) {
            // Send message: "I need a pickaxe to mine! Can you give me one?"
        }
    }
    
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        if (requestCooldown > 0) {
            requestCooldown--;
        }
        
        // Check if player gave us a tool
        ItemStack heldItem = entity.getMainHandItem();
        if (heldItem.getItem() instanceof PickaxeItem) {
            // Tool received, stop requesting
            return;
        }
        
        // Look at the player
        Player nearestPlayer = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
        if (nearestPlayer != null) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, nearestPlayer.getEyePosition());
        }
    }
    
    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        // Continue until we get a tool or cooldown expires
        return !(entity.getMainHandItem().getItem() instanceof PickaxeItem);
    }
    
    @Override
    protected void stop(ServerLevel level, LivingEntity entity, long gameTime) {
        requestCooldown = 0;
    }
}
