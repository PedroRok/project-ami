package com.pedrorok.ami.pathfinding.pathfinder;

import com.pedrorok.ami.pathfinding.octree.NodeState;
import com.pedrorok.ami.pathfinding.octree.OctreeConfig;
import com.pedrorok.ami.pathfinding.octree.OctreeNode;
import com.pedrorok.ami.pathfinding.octree.SpatialOctree;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OctreePathfinder {
    private final SpatialOctree octree;
    private final LRUCache<PathCacheKey, PathResult> pathCache;
    private final Map<BlockPos, OctreeNode> nodeCache;
    
    public OctreePathfinder(SpatialOctree octree) {
        this.octree = octree;
        this.pathCache = new LRUCache<>(100);
        this.nodeCache = new ConcurrentHashMap<>();
    }
    
    @Data
    private static class PathCacheKey {
        private final BlockPos start;
        private final BlockPos goal;
        private final int entityId;
        
        @Override
        public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof PathCacheKey key)) return false;
	        return entityId == key.entityId
	            && Objects.equals(start, key.start)
	            && Objects.equals(goal, key.goal);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(start, goal, entityId);
        }
    }
    
    public PathResult findPath(BlockPos start, BlockPos goal, Entity entity) {
        if (start.equals(goal)) {
            return PathResult.success(Collections.singletonList(start));
        }
        
        PathCacheKey cacheKey = new PathCacheKey(start, goal, entity != null ? entity.getId() : 0);
        PathResult cached = pathCache.get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for path from {} to {}", start, goal);
            return cached;
        }
        
        log.debug("Finding path from {} to {}", start, goal);
        
        PathResult result = findPathInternal(start, goal, entity);
        pathCache.put(cacheKey, result);
        
        return result;
    }
    
    private PathResult findPathInternal(BlockPos start, BlockPos goal, Entity entity) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, PathNode> openSetMap = new HashMap<>();
        
        if (!isPositionNavigable(start, entity)) {
            return PathResult.failure("Start position is not navigable");
        }
        
        PathNode startPathNode = new PathNode(start, getCachedNode(start));
        startPathNode.gCost = 0;
        startPathNode.hCost = heuristic(start, goal);
        openSet.add(startPathNode);
        openSetMap.put(start, startPathNode);
        
        int iterations = 0;
        int maxIterations = OctreeConfig.MAX_PATH_LENGTH * 2;
        
        while (!openSet.isEmpty() && iterations < maxIterations) {
            PathNode current = openSet.poll();
            openSetMap.remove(current.position);
            
            if (current.position.equals(goal)) {
                log.debug("Path found in {} iterations", iterations);
                return PathResult.success(reconstructPath(current));
            }
            
            closedSet.add(current.position);
            
            List<PathNode> neighbors = getJumpPointNeighbors(current, goal, entity);
            
            for (PathNode neighbor : neighbors) {
                if (closedSet.contains(neighbor.position)) continue;
                if (!isPositionNavigable(neighbor.position, entity)) continue;
                
                double tentativeG = current.gCost + calculateCost(current.position, neighbor.position, neighbor.octreeNode);
                
                PathNode existingNode = openSetMap.get(neighbor.position);
                if (existingNode != null && tentativeG >= existingNode.gCost) continue;
                
                PathNode neighborPathNode = existingNode != null ? existingNode : neighbor;
                neighborPathNode.gCost = tentativeG;
                neighborPathNode.hCost = heuristic(neighbor.position, goal);
                neighborPathNode.parent = current;
                
                if (existingNode == null) {
                    openSet.add(neighborPathNode);
                    openSetMap.put(neighbor.position, neighborPathNode);
                }
            }
            
            iterations++;
        }
		
	    String reason = iterations >= maxIterations ? "Maximum iterations reached" : "No path found";
	    log.warn("Pathfinding failed: {}", reason);
		
	    return PathResult.failure(reason);
    }
    
    private List<PathNode> getJumpPointNeighbors(PathNode current, BlockPos goal, Entity entity) {
        List<PathNode> neighbors = new ArrayList<>();
        
        if (current.octreeNode == null) {
            return neighbors;
        }
        
        List<OctreeNode> octreeNeighbors = octree.getNeighbors(current.octreeNode);
        
        for (OctreeNode neighbor : octreeNeighbors) {
            BlockPos neighborPos = neighbor.getRegion().getCenter();
            
            if (isJumpPoint(current.position, neighborPos, goal)) {
                neighbors.add(new PathNode(neighborPos, neighbor));
            }
        }
        
        return neighbors;
    }
    
    private boolean isJumpPoint(BlockPos from, BlockPos to, BlockPos goal) {
        int dx = Integer.signum(to.getX() - from.getX());
        int dy = Integer.signum(to.getY() - from.getY());
        int dz = Integer.signum(to.getZ() - from.getZ());
        
        if (dx == 0 && dy == 0 && dz == 0) return false;
        
        if (dx != 0 && dy != 0 && dz != 0) return true;
        if (dx != 0 && dy != 0) return true;
        if (dx != 0 && dz != 0) return true;
        if (dy != 0 && dz != 0) return true;
        
        return isForcedNeighbor(from, to, dx, dy, dz);
    }
    
    private boolean isForcedNeighbor(BlockPos from, BlockPos to, int dx, int dy, int dz) {
        OctreeNode fromNode = getCachedNode(from);
        OctreeNode toNode = getCachedNode(to);
        
        if (fromNode == null || toNode == null) return false;
        
        if (fromNode.getState() == NodeState.SOLID || toNode.getState() == NodeState.SOLID) {
            return true;
        }
        
        return false;
    }
    
    private OctreeNode getCachedNode(BlockPos pos) {
        return nodeCache.computeIfAbsent(pos, octree::findNode);
    }
    
    private double heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + 
               Math.abs(a.getY() - b.getY()) + 
               Math.abs(a.getZ() - b.getZ());
    }
    
    private double calculateCost(BlockPos from, BlockPos to, OctreeNode node) {
        double distance = Math.sqrt(from.distSqr(to));
        double cost = distance;

        if (node != null && node.getState() == NodeState.MIXED) {
            cost *= OctreeConfig.MIXED_NODE_COST_MULTIPLIER;
        }

        double heightDiff = Math.abs(to.getY() - from.getY());
        cost += heightDiff * OctreeConfig.HEIGHT_CHANGE_PENALTY;

        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());

        if (dx > 0 && dy > 0 && dz > 0) {
            cost *= 1.2;
        } else if ((dx > 0 && dy > 0) || (dx > 0 && dz > 0) || (dy > 0 && dz > 0)) {
            cost *= 1.1;
        }

        return cost;
    }
    
    private List<BlockPos> reconstructPath(PathNode goalNode) {
        List<BlockPos> path = new ArrayList<>();
        PathNode current = goalNode;
        
        while (current != null) {
            path.add(current.position);
            current = current.parent;
        }
        
        Collections.reverse(path);
        return path;
    }
    
    public boolean hasDirectPath(BlockPos start, BlockPos goal) {
        OctreeNode startNode = getCachedNode(start);
        OctreeNode goalNode = getCachedNode(goal);
        
        if (startNode == null || goalNode == null) {
            return false;
        }
        
        if (startNode == goalNode) {
            return startNode.getState() != NodeState.SOLID;
        }
        
        return octree.areNodesReachable(startNode, goalNode);
    }
    
    public BlockPos findNearestNavigablePosition(BlockPos target) {
        OctreeNode targetNode = getCachedNode(target);
        
        if (targetNode == null) {
            return target;
        }
        
        if (targetNode.getState() != NodeState.SOLID) {
            return targetNode.getRegion().getCenter();
        }
        
        List<OctreeNode> neighbors = octree.getNeighbors(targetNode);
        
        for (OctreeNode neighbor : neighbors) {
            if (neighbor.getState() != NodeState.SOLID) {
                return neighbor.getRegion().getCenter();
            }
        }
        
        return target;
    }
    
    public PathfindingStats getLastStats() {
        return new PathfindingStats();
    }
    
    public boolean isPositionNavigable(BlockPos pos, Entity entity) {
        OctreeNode node = getCachedNode(pos);
        if (node == null || node.getState() == NodeState.SOLID) {
            return false;
        }
        
        Level level = octree.getLevel();
        
        AABB entityBounds = entity.getBoundingBox();
        double width = entityBounds.getXsize();
        double height = entityBounds.getYsize();
        double depth = entityBounds.getZsize();
        
        AABB targetBounds = new AABB(
            pos.getX() + (1.0 - width) / 2.0,
            pos.getY(),
            pos.getZ() + (1.0 - depth) / 2.0,
            pos.getX() + (1.0 + width) / 2.0,
            pos.getY() + height,
            pos.getZ() + (1.0 + depth) / 2.0
        );
        
        return level.noCollision(null, targetBounds);
    }
    
    public void clearCache() {
        pathCache.clear();
        nodeCache.clear();
    }
    
    public void invalidateCache(BlockPos pos) {
        pathCache.entrySet().removeIf(entry -> 
            entry.getKey().getStart().equals(pos) || entry.getKey().getGoal().equals(pos));
        nodeCache.remove(pos);
    }
	
	private static class LRUCache<K, V> extends LinkedHashMap<K, V> {
		private final int maxSize;
		
		public LRUCache(int maxSize) {
			super(16, 0.75f, true);
			this.maxSize = maxSize;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > maxSize;
		}
	}
    
    @Data
    public static class PathfindingStats {
	    public int nodesExplored = 0;
	    public int iterations = 0;
	    public long executionTimeMs = 0;
	    
	    @Override
	    public String toString() {
		    return String.format("PathfindingStats[iterations=%d, time=%dms]", iterations, executionTimeMs);
	    }
    }
}
