package com.pedrorok.ami.items;

import com.pedrorok.ami.blocks.RobotPartsBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/10/2025
 * @project project-ami
 */
public class AmiPartItem extends Item {

    public final String part;

    public AmiPartItem(String part) {
        super(new Item.Properties());
        this.part = part;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (!(blockState.getBlock() instanceof RobotPartsBlock robotBlock)) return InteractionResult.PASS;
        return robotBlock.tryPlacePart(context, blockState, part);
    }
}
