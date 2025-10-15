package com.pedrorok.ami.pathfinding.octree;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import lombok.Setter;

public class OctreeNode {
    @Getter private OctreeRegion region;
    @Getter @Setter private NodeState state;
    @Getter @Setter private OctreeNode[] children;
    @Getter @Setter private int blockDensity;
    @Getter private Level level;
    
    public OctreeNode(OctreeRegion region, Level level) {
        this.region = region;
        this.level = level;
        this.state = NodeState.UNKNOWN;
        this.children = null;
        this.blockDensity = 0;
    }
    
    public void reset(OctreeRegion newRegion, Level newLevel) {
        this.region = newRegion;
        this.level = newLevel;
        this.state = NodeState.UNKNOWN;
        this.children = null;
        this.blockDensity = 0;
    }
	
	public boolean isLeaf() {
        return children == null;
    }
	
	public boolean shouldSubdivide() {
		if (state != NodeState.MIXED) return false;
		if (region.getSize() <= OctreeConfig.MIN_NODE_SIZE) return false;
		
		calculateDensity();
		
		return blockDensity > OctreeConfig.DENSITY_THRESHOLD_LOW && blockDensity < OctreeConfig.DENSITY_THRESHOLD_HIGH;
	}
    
    public void calculateDensity() {
        int solidBlocks = 0;
        int sampleCount = 0;
        
        int step = Math.max(1, OctreeConfig.SAMPLE_RATE);
        
        for (int x = region.min().getX(); x <= region.max().getX(); x += step) {
            for (int y = region.min().getY(); y <= region.max().getY(); y += step) {
                for (int z = region.min().getZ(); z <= region.max().getZ(); z += step) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    
                    if (!state.isAir()) {
                        solidBlocks++;
                    }
                    sampleCount++;
                }
            }
        }
        
        if (sampleCount > 0) {
            blockDensity = (solidBlocks * 100) / sampleCount;
        } else {
            blockDensity = 0;
        }
        
        if (blockDensity <= OctreeConfig.DENSITY_THRESHOLD_LOW) {
            this.state = NodeState.EMPTY;
        } else if (blockDensity >= OctreeConfig.DENSITY_THRESHOLD_HIGH) {
            this.state = NodeState.SOLID;
        } else {
            this.state = NodeState.MIXED;
        }
    }
    
    public OctreeNode findChild(BlockPos pos) {
        if (isLeaf()) {
            return region.contains(pos) ? this : null;
        }
        
        for (OctreeNode child : children) {
            if (child.region.contains(pos)) {
                return child.findChild(pos);
            }
        }
        
        return null;
    }
    
    public void update(BlockPos pos, BlockState newState) {
        if (!region.contains(pos)) {
            return;
        }
        
        if (isLeaf()) {
            calculateDensity();
        } else {
            for (OctreeNode child : children) {
                child.update(pos, newState);
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("OctreeNode[region=%s, state=%s, density=%d%%, leaf=%s]", 
            region, state, blockDensity, isLeaf());
    }
}
