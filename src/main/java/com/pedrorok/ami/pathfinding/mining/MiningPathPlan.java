package com.pedrorok.ami.pathfinding.mining;

import com.pedrorok.ami.entities.robot.tasks.mining.MiningPlan;
import com.pedrorok.ami.pathfinding.octree.SpatialOctree;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MiningPathPlan extends MiningPlan {
    private final SpatialOctree octree;
    private final MiningPathfinder pathfinder;
    
    public MiningPathPlan(List<BlockPos> blocks, List<BlockPos> obstacles, SpatialOctree octree, MiningPathfinder pathfinder) {
        super(blocks, obstacles);
        this.octree = octree;
        this.pathfinder = pathfinder;
        
        log.info("[MiningPathPlan] Created with {} blocks, {} obstacles, octree: {}, pathfinder: {}", 
            blocks.size(), obstacles.size(), octree != null, pathfinder != null);
    }
    
    public MiningPathPlan(String failureReason) {
        super(failureReason);
        this.octree = null;
        this.pathfinder = null;
        
        log.warn("[MiningPathPlan] Created with failure: {}", failureReason);
    }
    
    public void updateOctree(BlockPos pos, BlockState newState) {
        if (octree != null) {
            octree.update(pos, newState);
            log.debug("[MiningPathPlan] Updated octree at {}", pos);
        }
    }
    
    /**
     * Obtém o pathfinder associado a este plano.
     */
    public MiningPathfinder getPathfinder() {
        return pathfinder;
    }
    
    /**
     * Obtém a octree associada a este plano.
     */
    public SpatialOctree getOctree() {
        return octree;
    }
    
    /**
     * Verifica se um bloco é acessível usando pathfinding.
     */
    public boolean isBlockAccessible(BlockPos pos) {
        if (octree == null) {
            return true;
        }
        
        var node = octree.findNode(pos);
        return node != null && node.getState() != com.pedrorok.ami.pathfinding.octree.NodeState.SOLID;
    }
    
    /**
     * Obtém estatísticas da octree para debug.
     */
    public SpatialOctree.OctreeStats getOctreeStats() {
        return octree != null ? octree.getStats() : null;
    }
    
    /**
     * Valida se o plano ainda é viável considerando mudanças no mundo.
     */
    @Override
    public boolean validatePlan(net.minecraft.world.level.Level level) {
        if (!super.validatePlan(level)) {
            return false;
        }
        
        if (octree != null) {
            List<BlockPos> remainingBlocks = getRemainingBlocks();
            int navigableBlocks = 0;
            
            for (BlockPos block : remainingBlocks) {
                if (isBlockAccessible(block)) {
                    navigableBlocks++;
                }
            }
            
            if (remainingBlocks.size() > 0 && navigableBlocks < remainingBlocks.size() / 2) {
                log.warn("[MiningPathPlan] Plan may be unviable: only {} of {} blocks are navigable", 
                    navigableBlocks, remainingBlocks.size());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Obtém blocos restantes que ainda precisam ser minerados.
     */
    public List<BlockPos> getRemainingBlocks() {
        List<BlockPos> remaining = new ArrayList<>();
        
        for (int i = getCurrentBlockIndex(); i < getBlocksToMine().size(); i++) {
            remaining.add(getBlocksToMine().get(i));
        }
        
        return remaining;
    }
    
    /**
     * Obtém o próximo bloco acessível usando pathfinding.
     */
    public BlockPos getNextAccessibleBlock(com.pedrorok.ami.entities.robot.RobotEntity robot) {
        if (pathfinder == null) {
            return getCurrentBlock();
        }
        
        return pathfinder.findNextAccessibleBlock(robot, this);
    }
    
    /**
     * Verifica se há obstáculos no caminho para um bloco específico.
     */
    public boolean hasObstaclesToBlock(BlockPos target) {
        if (octree == null) {
            return false;
        }
        
        var node = octree.findNode(target);
        return node != null && node.getState() == com.pedrorok.ami.pathfinding.octree.NodeState.MIXED;
    }
    
    /**
     * Obtém informações de debug sobre o plano.
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append(String.format("MiningPathPlan[blocks=%d/%d, obstacles=%d", 
            getCompletedBlocks(), getTotalBlocks(), getObstaclesInPath().size()));
        
        if (octree != null) {
            var stats = octree.getStats();
            info.append(String.format(", octree=%s", stats));
        }
        
        info.append("]");
        return info.toString();
    }
}
