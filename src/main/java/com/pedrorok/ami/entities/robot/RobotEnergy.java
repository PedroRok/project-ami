package com.pedrorok.ami.entities.robot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import lombok.Getter;
import lombok.Setter;

public class RobotEnergy {
	private final LivingEntity entity;
	@Getter private int currentEnergy;
	@Getter private int maxEnergy;
	@Getter @Setter private int energyPerTick = 1;
	
	public RobotEnergy(LivingEntity entity) {
		this.entity = entity;
		this.maxEnergy = 1000;
		this.currentEnergy = maxEnergy;
	}
	
	public RobotEnergy(LivingEntity entity, int maxEnergy) {
		this.entity = entity;
		this.maxEnergy = maxEnergy;
		this.currentEnergy = maxEnergy;
	}
	
	public float getEnergyPercentage() {
		return (float) currentEnergy / maxEnergy;
	}
	
	public boolean hasEnergy(int amount) {
		return currentEnergy >= amount;
	}
	
	public void consumeEnergy(int amount) {
		currentEnergy = Math.max(0, currentEnergy - amount);
	}
	
	public int addEnergy(int amount) {
		int oldEnergy = currentEnergy;
		currentEnergy = Math.min(maxEnergy, currentEnergy + amount);
		return currentEnergy - oldEnergy;
	}
	
	public void setEnergy(int amount) {
		currentEnergy = Math.max(0, Math.min(maxEnergy, amount));
	}
	
	public void setMaxEnergy(int maxEnergy) {
		this.maxEnergy = maxEnergy;
		currentEnergy = Math.min(currentEnergy, maxEnergy);
	}
	
	public void tick() {
		if (entity.isAlive() && !entity.isSleeping()) {
			consumeEnergy(energyPerTick);
		}
	}
	
	public boolean isOutOfEnergy() {
		return currentEnergy <= 0;
	}
	
	public boolean isLowEnergy() {
		return getEnergyPercentage() < 0.2f;
	}
	
	public CompoundTag saveToNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("current_energy", currentEnergy);
		tag.putInt("max_energy", maxEnergy);
		tag.putInt("energy_per_tick", energyPerTick);
		return tag;
	}
	
	public void loadFromNBT(CompoundTag tag) {
		currentEnergy = tag.getInt("current_energy");
		maxEnergy = tag.getInt("max_energy");
		energyPerTick = tag.getInt("energy_per_tick");
	}
}
