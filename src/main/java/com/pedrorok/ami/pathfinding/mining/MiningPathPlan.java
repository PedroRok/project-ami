package com.pedrorok.ami.pathfinding.mining;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningPlan;
import com.pedrorok.ami.pathfinding.octree.SpatialOctree;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Plano de mineração estendido que integra octree pathfinding.
 * Herda funcionalidade básica de MiningPlan e adiciona capacidades de octree.
 */
public class MiningPathPlan extends MiningPlan {
    private final SpatialOctree octree;
    private final MiningPathfinder pathfinder;
    
    public MiningPathPlan(List<BlockPos> blocks, List<BlockPos> obstacles, SpatialOctree octree, MiningPathfinder pathfinder) {
        super(blocks, obstacles);
        this.octree = octree;
        this.pathfinder = pathfinder;
        
        ProjectAmi.LOGGER.info("[MiningPathPlan] Created with {} blocks, {} obstacles, octree: {}, pathfinder: {}", 
            blocks.size(), obstacles.size(), octree != null, pathfinder != null);
    }
    
    /**
     * Construtor para casos de erro (quando não há blocos para minerar).
     */
    public MiningPathPlan(String failureReason) {
        super(failureReason);
        this.octree = null;
        this.pathfinder = null;
        
        ProjectAmi.LOGGER.warn("[MiningPathPlan] Created with failure: {}", failureReason);
    }
    
    /**
     * Atualiza a octree quando um bloco é modificado no mundo.
     */
    public void updateOctree(BlockPos pos, BlockState newState) {
        if (octree != null) {
            octree.update(pos, newState);
            ProjectAmi.LOGGER.debug("[MiningPathPlan] Updated octree at {}", pos);
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
            return true; // Fallback se octree não estiver disponível
        }
        
        // Verificar se o nó da octree é navegável
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
        
        // Validação adicional usando octree
        if (octree != null) {
            // Verificar se ainda há blocos navegáveis
            List<BlockPos> remainingBlocks = getRemainingBlocks();
            int navigableBlocks = 0;
            
            for (BlockPos block : remainingBlocks) {
                if (isBlockAccessible(block)) {
                    navigableBlocks++;
                }
            }
            
            // Se menos de 50% dos blocos restantes são navegáveis, considerar inviável
            if (remainingBlocks.size() > 0 && navigableBlocks < remainingBlocks.size() / 2) {
                ProjectAmi.LOGGER.warn("[MiningPathPlan] Plan may be unviable: only {} of {} blocks are navigable", 
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
            // Fallback: usar getCurrentBlock() da classe base que já tem progressão correta
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
