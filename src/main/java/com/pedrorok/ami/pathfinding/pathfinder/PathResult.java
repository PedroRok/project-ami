package com.pedrorok.ami.pathfinding.pathfinder;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de uma operação de pathfinding.
 * Contém o caminho calculado, obstáculos encontrados e metadados.
 */
public class PathResult {
    private final List<BlockPos> path;
    private final boolean success;
    private final List<BlockPos> obstacles;
    private final double estimatedCost;
    private final String failureReason;
    
    private PathResult(List<BlockPos> path, boolean success, List<BlockPos> obstacles, 
                      double estimatedCost, String failureReason) {
        this.path = path != null ? new ArrayList<>(path) : Collections.emptyList();
        this.success = success;
        this.obstacles = obstacles != null ? new ArrayList<>(obstacles) : Collections.emptyList();
        this.estimatedCost = estimatedCost;
        this.failureReason = failureReason;
    }
    
    public static PathResult success(List<BlockPos> path) {
        return new PathResult(path, true, Collections.emptyList(), 
            calculatePathCost(path), null);
    }
    
    public static PathResult success(List<BlockPos> path, List<BlockPos> obstacles) {
        return new PathResult(path, true, obstacles, 
            calculatePathCost(path), null);
    }
    
    public static PathResult failure(String reason) {
        return new PathResult(Collections.emptyList(), false, 
            Collections.emptyList(), Double.MAX_VALUE, reason);
    }
    
    public static PathResult failure(String reason, List<BlockPos> obstacles) {
        return new PathResult(Collections.emptyList(), false, 
            obstacles, Double.MAX_VALUE, reason);
    }
    
    public List<BlockPos> getPath() {
        return Collections.unmodifiableList(path);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public List<BlockPos> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }
    
    public double getEstimatedCost() {
        return estimatedCost;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public int getPathLength() {
        return path.size();
    }
    
    public boolean isEmpty() {
        return path.isEmpty();
    }
    
    private static double calculatePathCost(List<BlockPos> path) {
        if (path.size() < 2) {
            return 0.0;
        }
        
        double totalCost = 0.0;
        for (int i = 1; i < path.size(); i++) {
            totalCost += path.get(i).distSqr(path.get(i - 1));
        }
        
        return Math.sqrt(totalCost);
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("PathResult[success=true, length=%d, cost=%.2f]", 
                path.size(), estimatedCost);
        } else {
            return String.format("PathResult[success=false, reason=%s]", failureReason);
        }
    }
}
