package com.pedrorok.ami.entities.robot;

import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.network.NetworkHandler;
import com.pedrorok.ami.network.packets.OpenDialoguePacket;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import com.pedrorok.ami.system.dialog.DialogueAnimationHelper;
import com.pedrorok.ami.system.dialog.DialogueHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RobotEntity extends PathfinderMob implements RobotAi, InventoryCarrier, GeoEntity, DialogueAnimationHelper.DialogueAnimatable {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	
	@Getter private final RobotEnergy energy;
	private final SimpleContainer inventory = new SimpleContainer(9);

	private String currentDialogueAnimation = null;

	public RobotEntity(EntityType<? extends RobotEntity> entityType, Level level) {
		super(entityType, level);
		this.energy = new RobotEnergy(this);
		this.moveControl = new FlyingMoveControl(this, 20, true);
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 10.0D)
			.add(Attributes.FLYING_SPEED, 0.1F)
			.add(Attributes.MOVEMENT_SPEED, 0.1F)
			.add(Attributes.ATTACK_DAMAGE, 2.0D);
	}
	
	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
		this.setOwner(player);
		this.getBrain().setMemory(ModMemoryModuleTypes.CURRENT_TASK.get(), new MiningTaskData());


		if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
			if (player instanceof ServerPlayer serverPlayer) {
				NetworkHandler.sendToPlayer(
						new OpenDialoguePacket("greeting", this.getId()),
						serverPlayer
				);
			}
			return InteractionResult.SUCCESS;
		}

		return super.mobInteract(player, hand);
	}
	
	//region Brain stuff
	@Override
	protected Brain.@NotNull Provider<RobotEntity> brainProvider() {
		return new SmartBrainProvider<>(this);
	}
	
	@Override
	public void customServerAiStep() {
		this.level().getProfiler().push("robotBrain");
		this.tickBrain(this);
		this.level().getProfiler().popPush("robotEnergy");
		this.energy.tick();
		this.level().getProfiler().pop();
		super.customServerAiStep();
	}
	
	@Override
	protected void sendDebugPackets() {
		DebugPackets.sendEntityBrain(this);
		super.sendDebugPackets();
	}
	//endregion
	
	//region Navigation
	@Override
	protected @NotNull PathNavigation createNavigation(Level level) {
		FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
		flyingpathnavigation.setCanOpenDoors(false);
		flyingpathnavigation.setCanFloat(true);
		flyingpathnavigation.setCanPassDoors(true);
		return flyingpathnavigation;
	}
	
	@Override
	public void travel(Vec3 travelVector) {
		if (this.isControlledByLocalInstance()) {
			if (this.isInWater()) {
				this.moveRelative(0.02F, travelVector);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
			} else if (this.isInLava()) {
				this.moveRelative(0.02F, travelVector);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
			} else {
				this.moveRelative(this.getSpeed(), travelVector);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
			}
		}
		
		this.calculateEntityAnimation(false);
	}
	
	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }
	//endregion
	
	//region Inventory
	@Override
	protected void dropEquipment() {
		this.inventory.removeAllItems().forEach(this::spawnAtLocation);
		ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
		if (!stack.isEmpty() && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
			this.spawnAtLocation(stack);
			this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
	}
	
	@Override
	public @NotNull SimpleContainer getInventory() {
		return this.inventory;
	}
	//endregion
	
	//region Owner
	public boolean hasOwner() {
		return this.getBrain().hasMemoryValue(MemoryModuleType.LIKED_PLAYER);
	}
	
	public boolean isOwnedBy(Player player) {
		return this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER)
			       .map(uuid -> uuid.equals(player.getUUID())).orElse(false);
	}
	
	@Nullable
	public Player getOwner() {
		return this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER)
			.map(uuid -> this.level().getPlayerByUUID(uuid)).orElse(null);
	}
	
	public void setOwner(@Nullable Player owner) {
		this.setOwner(owner == null ? null : owner.getUUID());
	}
	
	public void setOwner(@Nullable UUID owner) {
		if (owner == null) {
			this.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
			return;
		}
		this.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, owner);
	}
	//endregion
	
	//region GeoEntity
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));

		controllers.add(new AnimationController<>(this, "dialogue_controller", 0, this::dialogueAnimController));
	}

	private <E extends GeoAnimatable> PlayState dialogueAnimController(AnimationState<E> state) {
		if (currentDialogueAnimation != null) {
			state.getController().setAnimation(
					RawAnimation.begin().thenPlay(currentDialogueAnimation)
			);

			if (state.getController().hasAnimationFinished()) {
				currentDialogueAnimation = null;
				state.getController().forceAnimationReset();
			}

			return PlayState.CONTINUE;
		}

		return PlayState.STOP;
	}

	private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {

		if (currentDialogueAnimation != null) {
			return PlayState.STOP;
		}

		if (event.getController().getAnimationState().equals(AnimationController.State.STOPPED)) {
			RawAnimation rawAnimation = RawAnimation.begin().thenLoop("animation.idle");
			event.getController().setAnimation(rawAnimation);
		}
		return PlayState.CONTINUE;
	}

	@Override
	public void playDialogueAnimation(String animationName) {
		this.currentDialogueAnimation = "animation." + animationName;
	}
	//endregion
	
	//region SaveData
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.put("energy", this.energy.saveToNBT());
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.energy.loadFromNBT(tag.getCompound("energy"));
	}
	//endregion
}
