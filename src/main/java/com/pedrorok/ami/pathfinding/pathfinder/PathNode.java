package com.pedrorok.ami.pathfinding.pathfinder;

import com.pedrorok.ami.pathfinding.octree.OctreeNode;
import net.minecraft.core.BlockPos;

import lombok.Data;

@Data
public class PathNode implements Comparable<PathNode> {
	public final BlockPos position;
	public final OctreeNode octreeNode;
	public double gCost;
	public double hCost;
	public PathNode parent;
	
	public PathNode(BlockPos position, OctreeNode octreeNode) {
		this.position = position;
		this.octreeNode = octreeNode;
	}
	
	public double getFCost() {
		return gCost + hCost;
	}
	
	@Override
	public int compareTo(PathNode other) {
		return Double.compare(this.getFCost(), other.getFCost());
	}
}