package com.pedrorok.ami.blocks.robot_part;

import com.pedrorok.ami.blocks.robot_parts.PartType;
import com.pedrorok.ami.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RobotPartBlockEntity extends BlockEntity {
	private PartType partType = PartType.HEAD;
	
	public RobotPartBlockEntity(BlockPos pos, BlockState blockState) {
		super(ModBlockEntities.ROBOT_PART.get(), pos, blockState);
	}
	
	public PartType getPartType() {
		return partType;
	}
	
	public void setPartType(PartType partType) {
		this.partType = partType;
		this.setChanged();
	}
	
	/*@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putString("part_type", partType.getName());
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("part_type")) {
			String partTypeName = tag.getString("part_type");
			for (PartType type : PartType.values()) {
				if (type.getName().equals(partTypeName)) {
					this.partType = type;
					break;
				}
			}
		}
	}*/
}
