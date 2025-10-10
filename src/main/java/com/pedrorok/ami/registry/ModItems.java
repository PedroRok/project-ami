package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ProjectAmi.MOD_ID);
	
	// Robot Parts
	public static final DeferredHolder<Item, BlockItem> ROBOT_PART = ITEMS.register("robot_part",
		() -> new BlockItem(ModBlocks.ROBOT_PART.get(), new Item.Properties().rarity(Rarity.UNCOMMON))
	);
	
	// Reactor Core (for energy refueling)
	public static final DeferredHolder<Item, Item> REACTOR_CORE = ITEMS.register("reactor_core",
		() -> new Item(new Item.Properties().rarity(Rarity.RARE).stacksTo(16))
	);
}
