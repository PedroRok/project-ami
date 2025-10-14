package com.pedrorok.ami.pathfinding.pathfinder;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.pathfinding.octree.NodeState;
import com.pedrorok.ami.pathfinding.octree.OctreeConfig;
import com.pedrorok.ami.pathfinding.octree.OctreeNode;
import com.pedrorok.ami.pathfinding.octree.SpatialOctree;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import java.util.*;

/**
 * Implementação do algoritmo A* para pathfinding sobre octree.
 * Otimiza a busca usando a estrutura hierárquica da octree.
 */
public class OctreePathfinder {
    private final SpatialOctree octree;
    
    public OctreePathfinder(SpatialOctree octree) {
        this.octree = octree;
    }
    
    /**
     * Encontra o caminho mais eficiente entre dois pontos usando A*.
     */
    public PathResult findPath(BlockPos start, BlockPos goal, Entity entity) {
        if (start.equals(goal)) {
            return PathResult.success(Collections.singletonList(start));
        }
        
        ProjectAmi.LOGGER.debug("[OctreePathfinder] Finding path from {} to {}", start, goal);
        
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, PathNode> openSetMap = new HashMap<>();
        
        // Nó inicial
        if (!isPositionNavigable(start, entity)) {
            return PathResult.failure("Start position is not navigable");
        }
        
        PathNode startPathNode = new PathNode(start, octree.findNode(start));
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
                ProjectAmi.LOGGER.debug("[OctreePathfinder] Path found in {} iterations", iterations);
                return PathResult.success(reconstructPath(current));
            }
            
            closedSet.add(current.position);
            
            // Expandir vizinhos usando octree
            List<OctreeNode> neighbors = octree.getNeighbors(current.octreeNode);
            
            for (OctreeNode neighbor : neighbors) {
                BlockPos neighborPos = neighbor.getRegion().getCenter();
                
                if (closedSet.contains(neighborPos)) {
                    continue;
                }
                
                // Verificar se a posição é navegável considerando o bounding box real da entidade
                if (!isPositionNavigable(neighborPos, entity)) {
                    continue;
                }
                
                // Calcular custo do movimento
                double tentativeG = current.gCost + 
                    calculateCost(current.position, neighborPos, neighbor);
                
                // Verificar se já existe um caminho melhor para este nó
                PathNode existingNode = openSetMap.get(neighborPos);
                if (existingNode != null && tentativeG >= existingNode.gCost) {
                    continue;
                }
                
                // Criar ou atualizar nó
                PathNode neighborPathNode = existingNode != null ? existingNode : 
                    new PathNode(neighborPos, neighbor);
                
                neighborPathNode.gCost = tentativeG;
                neighborPathNode.hCost = heuristic(neighborPos, goal);
                neighborPathNode.parent = current;
                
                if (existingNode == null) {
                    openSet.add(neighborPathNode);
                    openSetMap.put(neighborPos, neighborPathNode);
                }
            }
            
            iterations++;
        }
        
        String reason = iterations >= maxIterations ? 
            "Maximum iterations reached" : "No path found";
        
        ProjectAmi.LOGGER.warn("[OctreePathfinder] Pathfinding failed: {}", reason);
        return PathResult.failure(reason);
    }
    
    /**
     * Heurística para estimar distância até o objetivo.
     * Usa distância Manhattan para ser consistente com movimento em grade.
     */
    private double heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + 
               Math.abs(a.getY() - b.getY()) + 
               Math.abs(a.getZ() - b.getZ());
    }
    
    /**
     * Calcula o custo de movimento entre dois pontos.
     * Considera o estado do nó da octree e penalidades específicas.
     */
    private double calculateCost(BlockPos from, BlockPos to, OctreeNode node) {
        double baseCost = from.distSqr(to);
        
        // Penalizar nós MIXED (requerem navegação cuidadosa)
        if (node.getState() == NodeState.MIXED) {
            baseCost *= OctreeConfig.MIXED_NODE_COST_MULTIPLIER;
        }
        
        // Penalizar mudanças de altura (importante para mining)
        double heightDiff = Math.abs(to.getY() - from.getY());
        baseCost += heightDiff * OctreeConfig.HEIGHT_CHANGE_PENALTY;
        
        // Penalizar movimentos diagonais (mais custosos)
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());
        
        if (dx > 0 && dy > 0 && dz > 0) {
            baseCost *= 1.2; // Movimento diagonal 3D
        } else if ((dx > 0 && dy > 0) || (dx > 0 && dz > 0) || (dy > 0 && dz > 0)) {
            baseCost *= 1.1; // Movimento diagonal 2D
        }
        
        return Math.sqrt(baseCost);
    }
    
    /**
     * Reconstrói o caminho a partir do nó final.
     */
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
    
    /**
     * Verifica se existe um caminho direto entre dois pontos.
     * Útil para validação rápida antes de executar A* completo.
     */
    public boolean hasDirectPath(BlockPos start, BlockPos goal) {
        OctreeNode startNode = octree.findNode(start);
        OctreeNode goalNode = octree.findNode(goal);
        
        if (startNode == null || goalNode == null) {
            return false;
        }
        
        // Verificar se são o mesmo nó ou vizinhos diretos
        if (startNode == goalNode) {
            return startNode.getState() != NodeState.SOLID;
        }
        
        return octree.areNodesReachable(startNode, goalNode);
    }
    
    /**
     * Encontra o nó navegável mais próximo a uma posição.
     * Útil quando a posição de destino não é navegável.
     */
    public BlockPos findNearestNavigablePosition(BlockPos target) {
        OctreeNode targetNode = octree.findNode(target);
        
        if (targetNode == null) {
            return target;
        }
        
        // Se o nó é navegável, retornar centro
        if (targetNode.getState() != NodeState.SOLID) {
            return targetNode.getRegion().getCenter();
        }
        
        // Buscar nós vizinhos navegáveis
        List<OctreeNode> neighbors = octree.getNeighbors(targetNode);
        
        for (OctreeNode neighbor : neighbors) {
            if (neighbor.getState() != NodeState.SOLID) {
                return neighbor.getRegion().getCenter();
            }
        }
        
        // Fallback: retornar posição original
        return target;
    }
    
    /**
     * Obtém estatísticas do último pathfinding executado.
     */
    public PathfindingStats getLastStats() {
        return new PathfindingStats();
    }
    
    /**
     * Verifica se uma posição é navegável para o robô.
     * Usa o bounding box real da entidade para verificar colisões.
     */
    public boolean isPositionNavigable(BlockPos pos, Entity entity) {
        OctreeNode node = octree.findNode(pos);
        if (node == null || node.getState() == NodeState.SOLID) {
            return false;
        }
        
        // Usar o bounding box real da entidade para verificar colisões
        net.minecraft.world.level.Level level = octree.getLevel();
        
        // Criar AABB na posição de destino usando as dimensões da entidade
        net.minecraft.world.phys.AABB entityBounds = entity.getBoundingBox();
        double width = entityBounds.getXsize();
        double height = entityBounds.getYsize();
        double depth = entityBounds.getZsize();
        
        // Criar AABB na posição de destino
        net.minecraft.world.phys.AABB targetBounds = new net.minecraft.world.phys.AABB(
            pos.getX() + (1.0 - width) / 2.0,  // Centralizar horizontalmente
            pos.getY(),                        // Altura base
            pos.getZ() + (1.0 - depth) / 2.0,  // Centralizar horizontalmente
            pos.getX() + (1.0 + width) / 2.0,  // Largura
            pos.getY() + height,                // Altura total
            pos.getZ() + (1.0 + depth) / 2.0    // Profundidade
        );
        
        return level.noCollision(null, targetBounds);
    }
    
    public static class PathfindingStats {
        public int nodesExplored = 0;
        public int iterations = 0;
        public long executionTimeMs = 0;
        
        @Override
        public String toString() {
            return String.format("PathfindingStats[iterations=%d, time=%dms]", 
                iterations, executionTimeMs);
        }
    }
}
