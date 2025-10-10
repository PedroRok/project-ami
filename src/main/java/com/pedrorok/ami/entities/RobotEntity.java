package com.pedrorok.ami.entities;

import com.mojang.serialization.Dynamic;
import com.pedrorok.ami.entities.ai.RobotAi;
import com.pedrorok.ami.entities.components.RobotEnergy;
import com.pedrorok.ami.registry.ModSensorTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RobotEntity extends Mob implements GeoEntity {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private UUID ownerUUID;
	private String robotName = "A.M.I.";
	private RobotEnergy energy;
	
	public RobotEntity(EntityType<? extends RobotEntity> entityType, Level level) {
		super(entityType, level);
		this.energy = new RobotEnergy(this);
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 10.0D)
			.add(Attributes.MOVEMENT_SPEED, 0.2D)
			.add(Attributes.ATTACK_DAMAGE, 2.0D);
	}
	
	@Override
	protected Brain.Provider<RobotEntity> brainProvider() {
		return Brain.provider(List.of(), List.of(
			ModSensorTypes.PLAYER_DETECTOR.get()
		));
	}
	
	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return RobotAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}
	
	@Override
	public void customServerAiStep() {
		if (level() instanceof ServerLevel serverLevel) {
			RobotAi.updateActivity(this);
			energy.tick(); // Consume energy per tick
		}
		super.customServerAiStep();
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}
	
	// Owner management
	public void setOwner(UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
	}
	
	public Optional<UUID> getOwnerUUID() {
		return Optional.ofNullable(ownerUUID);
	}
	
	public boolean isOwnedBy(Player player) {
		return ownerUUID != null && ownerUUID.equals(player.getUUID());
	}
	
	public String getRobotName() {
		return robotName;
	}
	
	public void setRobotName(String name) {
		this.robotName = name;
	}
	
	// Energy management
	public RobotEnergy getEnergy() {
		return energy;
	}
	
	public boolean hasEnergy(int amount) {
		return energy.hasEnergy(amount);
	}
	
	public boolean consumeEnergy(int amount) {
		return energy.consumeEnergy(amount);
	}
	
	public int addEnergy(int amount) {
		return energy.addEnergy(amount);
	}
	
	public boolean isOutOfEnergy() {
		return energy.isOutOfEnergy();
	}
	
	public boolean isLowEnergy() {
		return energy.isLowEnergy();
	}
	
	@Override
	protected void defineSynchedData(net.minecraft.world.entity.SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (ownerUUID != null) {
			tag.putUUID("owner", ownerUUID);
		}
		tag.putString("robot_name", robotName);
		energy.saveToNBT(tag);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID("owner")) {
			ownerUUID = tag.getUUID("owner");
		}
		if (tag.contains("robot_name")) {
			robotName = tag.getString("robot_name");
		}
		energy.loadFromNBT(tag);
	}
}
