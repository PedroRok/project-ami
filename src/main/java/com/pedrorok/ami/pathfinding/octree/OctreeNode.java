package com.pedrorok.ami.pathfinding.octree;

import com.pedrorok.ami.ProjectAmi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Nó individual da octree.
 * Representa uma região 3D e seu estado de ocupação.
 */
public class OctreeNode {
    private final OctreeRegion region;
    private NodeState state;
    private OctreeNode[] children; // null se leaf node
    private int blockDensity; // 0-100%
    private final Level level;
    
    public OctreeNode(OctreeRegion region, Level level) {
        this.region = region;
        this.level = level;
        this.state = NodeState.UNKNOWN;
        this.children = null;
        this.blockDensity = 0;
    }
    
    public OctreeRegion getRegion() {
        return region;
    }
    
    public NodeState getState() {
        return state;
    }
    
    public void setState(NodeState state) {
        this.state = state;
    }
    
    public OctreeNode[] getChildren() {
        return children;
    }
    
    public void setChildren(OctreeNode[] children) {
        this.children = children;
    }
    
    public boolean isLeaf() {
        return children == null;
    }
    
    public int getBlockDensity() {
        return blockDensity;
    }
    
    public void setBlockDensity(int blockDensity) {
        this.blockDensity = blockDensity;
    }
    
    /**
     * Determina se este nó deve ser subdividido.
     * Critérios: estado MIXED, tamanho suficiente, densidade adequada.
     */
    public boolean shouldSubdivide() {
        if (state != NodeState.MIXED) {
            return false;
        }
        
        if (region.getSize() <= OctreeConfig.MIN_NODE_SIZE) {
            return false;
        }
        
        calculateDensity();
        
        return blockDensity > OctreeConfig.DENSITY_THRESHOLD_LOW && 
               blockDensity < OctreeConfig.DENSITY_THRESHOLD_HIGH;
    }
    
    /**
     * Calcula a densidade de blocos sólidos na região.
     * Usa sampling para performance em regiões grandes.
     */
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
        
        // Determinar estado baseado na densidade
        if (blockDensity <= OctreeConfig.DENSITY_THRESHOLD_LOW) {
            this.state = NodeState.EMPTY;
        } else if (blockDensity >= OctreeConfig.DENSITY_THRESHOLD_HIGH) {
            this.state = NodeState.SOLID;
        } else {
            this.state = NodeState.MIXED;
        }
        
        ProjectAmi.LOGGER.debug("[OctreeNode] Region {} density: {}% -> {}", 
            region, blockDensity, this.state);
    }
    
    /**
     * Encontra o nó filho que contém a posição especificada.
     */
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
    
    /**
     * Atualiza o estado do nó após uma mudança no mundo.
     */
    public void update(BlockPos pos, BlockState newState) {
        if (!region.contains(pos)) {
            return;
        }
        
        if (isLeaf()) {
            // Recalcular densidade para leaf nodes
            calculateDensity();
        } else {
            // Propagar atualização para filhos
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
