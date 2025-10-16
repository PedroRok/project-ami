package com.pedrorok.ami.pathfinding.octree;

import com.pedrorok.ami.ProjectAmi;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class SpatialOctree {
    private OctreeNode root;
    @Getter private final Level level;
    private final int maxDepth;
    private final Map<BlockPos, OctreeNode> nodeCache;
    private final ConcurrentLinkedQueue<OctreeNode> nodePool;
    private final Map<BlockPos, Boolean> dirtyNodes;
    
    public SpatialOctree(Level level) {
        this.level = level;
        this.maxDepth = OctreeConfig.MAX_DEPTH;
        this.nodeCache = new ConcurrentHashMap<>();
        this.nodePool = new ConcurrentLinkedQueue<>();
        this.dirtyNodes = new ConcurrentHashMap<>();
    }
    
    public void build(OctreeRegion bounds) {
        log.info("Building octree for region: {}", bounds);
        
        long startTime = System.currentTimeMillis();
        root = buildRecursive(bounds, 0);
        long buildTime = System.currentTimeMillis() - startTime;
        
        log.info("Octree built in {}ms", buildTime);
    }
    
    private OctreeNode buildRecursive(OctreeRegion region, int depth) {
        OctreeNode node = getPooledNode(region);
        
        node.calculateDensity();
        
        if (depth >= maxDepth || !node.shouldSubdivide()) {
            return node;
        }
        
        OctreeRegion[] octants = region.subdivide();
        node.setChildren(new OctreeNode[8]);
        
        for (int i = 0; i < 8; i++) {
            node.getChildren()[i] = buildRecursive(octants[i], depth + 1);
        }
        
        return node;
    }
    
    private OctreeNode getPooledNode(OctreeRegion region) {
        OctreeNode node = nodePool.poll();
        if (node == null) {
            node = new OctreeNode(region, level);
        } else {
            node.reset(region, level);
        }
        return node;
    }
    
    public OctreeNode findNode(BlockPos pos) {
        OctreeNode cached = nodeCache.get(pos);
        if (cached != null && !isDirty(pos)) {
            return cached;
        }
        
        if (root == null) {
            return null;
        }
        
        OctreeNode found = root.findChild(pos);
        if (found != null) {
            nodeCache.put(pos, found);
        }
        
        return found;
    }
    
    public void update(BlockPos pos, BlockState newState) {
        if (root != null) {
            markDirty(pos);
            root.update(pos, newState);
        }
    }
    
    private void markDirty(BlockPos pos) {
        dirtyNodes.put(pos, true);
    }
    
    private boolean isDirty(BlockPos pos) {
        return dirtyNodes.getOrDefault(pos, false);
    }
    
    public void clearDirtyFlags() {
        dirtyNodes.clear();
    }
    
    public List<OctreeNode> getNeighbors(OctreeNode node) {
        List<OctreeNode> neighbors = new ArrayList<>();

        if (node == null) {
            return neighbors;
        }

        if (node.isLeaf()) {
            neighbors.addAll(findLeafNeighbors(node));
        } else {
            for (OctreeNode child : node.getChildren()) {
                neighbors.addAll(getNeighbors(child));
            }
        }

        return neighbors;
    }
    
    private List<OctreeNode> findLeafNeighbors(OctreeNode leafNode) {
        List<OctreeNode> neighbors = new ArrayList<>();
        OctreeRegion region = leafNode.getRegion();
        
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
    
    public boolean areNodesReachable(OctreeNode from, OctreeNode to) {
        if (from == null || to == null) {
            return false;
        }
        
        if (from.getState() == NodeState.SOLID || to.getState() == NodeState.SOLID) {
            return false;
        }
        
        List<OctreeNode> neighbors = getNeighbors(from);
        return neighbors.contains(to);
    }
    
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
	            case MIXED, UNKNOWN -> stats.mixedNodes++;
	        }
        } else {
            stats.internalNodes++;
            for (OctreeNode child : node.getChildren()) {
                calculateStats(child, stats, depth + 1);
            }
        }
    }
	
	@Data
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
