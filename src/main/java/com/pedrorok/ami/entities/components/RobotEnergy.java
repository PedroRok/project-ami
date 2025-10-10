package com.pedrorok.ami.entities.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public class RobotEnergy {
    private final LivingEntity entity;
    private int currentEnergy;
    private int maxEnergy;
    private int energyPerTick = 1; // Energy consumed per tick when active
    
    public RobotEnergy(LivingEntity entity) {
        this.entity = entity;
        this.maxEnergy = 1000; // Default max energy
        this.currentEnergy = maxEnergy; // Start with full energy
    }
    
    public RobotEnergy(LivingEntity entity, int maxEnergy) {
        this.entity = entity;
        this.maxEnergy = maxEnergy;
        this.currentEnergy = maxEnergy;
    }
    
    /**
     * Gets the current energy level
     * @return Current energy
     */
    public int getCurrentEnergy() {
        return currentEnergy;
    }
    
    /**
     * Gets the maximum energy capacity
     * @return Maximum energy
     */
    public int getMaxEnergy() {
        return maxEnergy;
    }
    
    /**
     * Gets the energy percentage (0.0 to 1.0)
     * @return Energy percentage
     */
    public float getEnergyPercentage() {
        return (float) currentEnergy / maxEnergy;
    }
    
    /**
     * Checks if the entity has enough energy
     * @param amount Required energy amount
     * @return true if has enough energy
     */
    public boolean hasEnergy(int amount) {
        return currentEnergy >= amount;
    }
    
    /**
     * Consumes energy
     * @param amount Amount to consume
     * @return true if energy was consumed successfully
     */
    public boolean consumeEnergy(int amount) {
        if (currentEnergy >= amount) {
            currentEnergy -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Adds energy
     * @param amount Amount to add
     * @return Amount actually added (capped at max)
     */
    public int addEnergy(int amount) {
        int oldEnergy = currentEnergy;
        currentEnergy = Math.min(maxEnergy, currentEnergy + amount);
        return currentEnergy - oldEnergy;
    }
    
    /**
     * Sets the energy level
     * @param amount Energy amount (clamped to 0-max)
     */
    public void setEnergy(int amount) {
        currentEnergy = Math.max(0, Math.min(maxEnergy, amount));
    }
    
    /**
     * Sets the maximum energy capacity
     * @param maxEnergy New maximum energy
     */
    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
        // Clamp current energy to new max
        currentEnergy = Math.min(currentEnergy, maxEnergy);
    }
    
    /**
     * Consumes energy per tick when the entity is active
     */
    public void tick() {
        if (entity.isAlive() && !entity.isSleeping()) {
            consumeEnergy(energyPerTick);
        }
    }
    
    /**
     * Gets the energy consumption per tick
     * @return Energy per tick
     */
    public int getEnergyPerTick() {
        return energyPerTick;
    }
    
    /**
     * Sets the energy consumption per tick
     * @param energyPerTick New energy per tick
     */
    public void setEnergyPerTick(int energyPerTick) {
        this.energyPerTick = energyPerTick;
    }
    
    /**
     * Checks if the entity is out of energy
     * @return true if out of energy
     */
    public boolean isOutOfEnergy() {
        return currentEnergy <= 0;
    }
    
    /**
     * Checks if the entity has low energy (below 20%)
     * @return true if low energy
     */
    public boolean isLowEnergy() {
        return getEnergyPercentage() < 0.2f;
    }
    
    /**
     * Saves the energy data to NBT
     * @param tag NBT tag to save to
     */
    public void saveToNBT(CompoundTag tag) {
        tag.putInt("current_energy", currentEnergy);
        tag.putInt("max_energy", maxEnergy);
        tag.putInt("energy_per_tick", energyPerTick);
    }
    
    /**
     * Loads the energy data from NBT
     * @param tag NBT tag to load from
     */
    public void loadFromNBT(CompoundTag tag) {
        if (tag.contains("current_energy")) {
            currentEnergy = tag.getInt("current_energy");
        }
        if (tag.contains("max_energy")) {
            maxEnergy = tag.getInt("max_energy");
        }
        if (tag.contains("energy_per_tick")) {
            energyPerTick = tag.getInt("energy_per_tick");
        }
    }
}
