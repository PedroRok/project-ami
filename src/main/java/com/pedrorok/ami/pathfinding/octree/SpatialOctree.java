package com.pedrorok.ami.pathfinding.octree;

import com.pedrorok.ami.ProjectAmi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Estrutura principal da octree espacial.
 * Gerencia a subdivisão hierárquica do espaço 3D.
 */
public class SpatialOctree {
    private OctreeNode root;
    private final Level level;
    private final int maxDepth;
    
    public SpatialOctree(Level level) {
        this.level = level;
        this.maxDepth = OctreeConfig.MAX_DEPTH;
    }
    
    /**
     * Constrói a octree para a região especificada.
     */
    public void build(OctreeRegion bounds) {
        ProjectAmi.LOGGER.info("[SpatialOctree] Building octree for region: {}", bounds);
        
        long startTime = System.currentTimeMillis();
        root = buildRecursive(bounds, 0);
        long buildTime = System.currentTimeMillis() - startTime;
        
        ProjectAmi.LOGGER.info("[SpatialOctree] Octree built in {}ms", buildTime);
    }
    
    private OctreeNode buildRecursive(OctreeRegion region, int depth) {
        OctreeNode node = new OctreeNode(region, level);
        
        // Determinar estado inicial
        node.calculateDensity();
        
        // Critérios de parada
        if (depth >= maxDepth || !node.shouldSubdivide()) {
            return node; // Leaf node
        }
        
        // Subdividir recursivamente
        OctreeRegion[] octants = region.subdivide();
        node.setChildren(new OctreeNode[8]);
        
        for (int i = 0; i < 8; i++) {
            node.getChildren()[i] = buildRecursive(octants[i], depth + 1);
        }
        
        return node;
    }
    
    /**
     * Encontra o nó que contém a posição especificada.
     */
    public OctreeNode findNode(BlockPos pos) {
        if (root == null) {
            return null;
        }
        
        return root.findChild(pos);
    }
    
    /**
     * Atualiza a octree após uma mudança no mundo.
     */
    public void update(BlockPos pos, BlockState newState) {
        if (root != null) {
            root.update(pos, newState);
        }
    }
    
    /**
     * Obtém todos os nós vizinhos de um nó específico.
     * Vizinhos são nós adjacentes que podem ser navegados.
     */
    public List<OctreeNode> getNeighbors(OctreeNode node) {
        List<OctreeNode> neighbors = new ArrayList<>();
        
        if (node == null || node.isLeaf()) {
            return neighbors;
        }
        
        // Para leaf nodes, encontrar nós vizinhos na mesma profundidade
        if (node.isLeaf()) {
            neighbors.addAll(findLeafNeighbors(node));
        } else {
            // Para internal nodes, usar filhos
            for (OctreeNode child : node.getChildren()) {
                neighbors.addAll(getNeighbors(child));
            }
        }
        
        return neighbors;
    }
    
    private List<OctreeNode> findLeafNeighbors(OctreeNode leafNode) {
        List<OctreeNode> neighbors = new ArrayList<>();
        OctreeRegion region = leafNode.getRegion();
        
        // Verificar vizinhos em todas as 6 direções
        Direction[] directions = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
            Direction.UP, Direction.DOWN
        };
        
        for (Direction dir : directions) {
            BlockPos neighborCenter = region.getCenter().relative(dir, region.getSize() / 2);
            OctreeNode neighbor = findNode(neighborCenter);
            
            if (neighbor != null && neighbor != leafNode && 
                neighbor.getState() != NodeState.SOLID) {
                neighbors.add(neighbor);
            }
        }
        
        return neighbors;
    }
    
    /**
     * Obtém todos os nós leaf da octree.
     */
    public List<OctreeNode> getAllLeafNodes() {
        List<OctreeNode> leafNodes = new ArrayList<>();
        
        if (root != null) {
            collectLeafNodes(root, leafNodes);
        }
        
        return leafNodes;
    }
    
    private void collectLeafNodes(OctreeNode node, List<OctreeNode> leafNodes) {
        if (node.isLeaf()) {
            leafNodes.add(node);
        } else {
            for (OctreeNode child : node.getChildren()) {
                collectLeafNodes(child, leafNodes);
            }
        }
    }
    
    /**
     * Verifica se dois nós são navegáveis entre si.
     */
    public boolean areNodesReachable(OctreeNode from, OctreeNode to) {
        if (from == null || to == null) {
            return false;
        }
        
        // Nós SOLID não são navegáveis
        if (from.getState() == NodeState.SOLID || to.getState() == NodeState.SOLID) {
            return false;
        }
        
        // Verificar se são vizinhos diretos
        List<OctreeNode> neighbors = getNeighbors(from);
        return neighbors.contains(to);
    }
    
    /**
     * Calcula estatísticas da octree para debug.
     */
    public OctreeStats getStats() {
        OctreeStats stats = new OctreeStats();
        
        if (root != null) {
            calculateStats(root, stats, 0);
        }
        
        return stats;
    }
    
    private void calculateStats(OctreeNode node, OctreeStats stats, int depth) {
        stats.totalNodes++;
        stats.maxDepth = Math.max(stats.maxDepth, depth);
        
        if (node.isLeaf()) {
            stats.leafNodes++;
            stats.totalVolume += node.getRegion().getVolume();
            
        switch (node.getState()) {
            case EMPTY -> stats.emptyNodes++;
            case SOLID -> stats.solidNodes++;
            case MIXED -> stats.mixedNodes++;
            case UNKNOWN -> stats.mixedNodes++; // Treat unknown as mixed for stats
        }
        } else {
            stats.internalNodes++;
            for (OctreeNode child : node.getChildren()) {
                calculateStats(child, stats, depth + 1);
            }
        }
    }
    
    /**
     * Obtém o nível do mundo associado a esta octree.
     */
    public Level getLevel() {
        return level;
    }
    
    public static class OctreeStats {
        public int totalNodes = 0;
        public int leafNodes = 0;
        public int internalNodes = 0;
        public int emptyNodes = 0;
        public int solidNodes = 0;
        public int mixedNodes = 0;
        public int maxDepth = 0;
        public int totalVolume = 0;
        
        @Override
        public String toString() {
            return String.format("OctreeStats[nodes=%d, leaves=%d, depth=%d, volume=%d]", 
                totalNodes, leafNodes, maxDepth, totalVolume);
        }
    }
}
