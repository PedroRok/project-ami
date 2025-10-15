package com.pedrorok.ami.pathfinding.pathfinder;

import net.minecraft.core.BlockPos;

import lombok.Data;
import java.util.Collections;
import java.util.List;

@Data
public class PathResult {
	private final boolean success;
	private final List<BlockPos> path;
	private final String failureReason;
	private final List<BlockPos> obstacles;
	
	private PathResult(boolean success, List<BlockPos> path, String failureReason, List<BlockPos> obstacles) {
		this.success = success;
		this.path = path != null ? path : Collections.emptyList();
		this.failureReason = failureReason;
		this.obstacles = obstacles != null ? obstacles : Collections.emptyList();
	}
	
	public static PathResult success(List<BlockPos> path) {
		return new PathResult(true, path, null, null);
	}
	
	public static PathResult failure(String reason) {
		return new PathResult(false, null, reason, null);
	}
	
	public static PathResult failure(String reason, List<BlockPos> obstacles) {
		return new PathResult(false, null, reason, obstacles);
	}
	
	public int getPathLength() {
		return path.size();
	}
}