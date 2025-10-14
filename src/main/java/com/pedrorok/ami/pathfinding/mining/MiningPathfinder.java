package com.pedrorok.ami.pathfinding.mining;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.pathfinding.octree.OctreeConfig;
import com.pedrorok.ami.pathfinding.octree.OctreeRegion;
import com.pedrorok.ami.pathfinding.octree.OctreeNode;
import com.pedrorok.ami.pathfinding.octree.SpatialOctree;
import com.pedrorok.ami.pathfinding.pathfinder.OctreePathfinder;
import com.pedrorok.ami.pathfinding.pathfinder.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pathfinder especializado para operações de mineração.
 * Integra octree pathfinding com lógica específica de mineração.
 */
public class MiningPathfinder {
    private OctreePathfinder pathfinder;
    private SpatialOctree octree;
    
    public MiningPathfinder() {
        this.pathfinder = null; // Será inicializado quando octree for construída
    }
    
    /**
     * Planeja uma operação de mineração completa usando octree.
     */
    public MiningPathPlan planMining(MiningTaskData task, ServerLevel level) {
        ProjectAmi.LOGGER.info("[MiningPathfinder] Planning mining operation: {} blocks, pattern: {}", 
            task.getTotalBlocks(), task.getPattern());
        
        // 1. Construir octree para área de mineração
        OctreeRegion miningRegion = calculateMiningRegion(task);
        octree = new SpatialOctree(level);
        octree.build(miningRegion);
        
        // 2. Gerar lista de blocos a serem minerados
        List<BlockPos> blocksToMine = generateMiningBlocks(task);
        
        // 2.5. Filtrar blocos já minerados (que são ar)
        List<BlockPos> solidBlocksToMine = blocksToMine.stream()
            .filter(pos -> !level.getBlockState(pos).isAir())
            .toList();
        
        ProjectAmi.LOGGER.info("[MiningPathfinder] Generated {} total blocks, {} solid blocks to mine", 
            blocksToMine.size(), solidBlocksToMine.size());
        
        if (solidBlocksToMine.isEmpty()) {
            ProjectAmi.LOGGER.warn("[MiningPathfinder] No solid blocks found to mine!");
            return new MiningPathPlan("Nenhum bloco sólido encontrado para minerar");
        }
        
        // 3. Ordenar blocos otimizado por octree
        List<BlockPos> optimizedSequence = optimizeBlockSequence(solidBlocksToMine, task.getStartPos());
        
        // 4. Identificar obstáculos na navegação
        List<BlockPos> obstacles = findNavigationObstacles(task.getStartPos(), optimizedSequence);
        
        // 5. Criar plano de mineração
        MiningPathPlan plan = new MiningPathPlan(optimizedSequence, obstacles, octree, this);
        
        ProjectAmi.LOGGER.info("[MiningPathfinder] Mining plan created: {} blocks, {} obstacles", 
            optimizedSequence.size(), obstacles.size());
        
        return plan;
    }
    
    /**
     * Calcula a região que precisa ser coberta pela octree.
     */
    private OctreeRegion calculateMiningRegion(MiningTaskData task) {
        BlockPos start = task.getStartPos();
        Direction dir = task.getDirection();
        
        // Calcular bounds baseado no pattern e número de blocos
        int maxDistance = task.getTotalBlocks();
        
        // Expandir região para incluir área de navegação
        int padding = Math.max(16, maxDistance / 4);
        
        BlockPos min = start.offset(-padding, -padding, -padding);
        BlockPos max = start.offset(
            dir.getStepX() * maxDistance + padding,
            dir.getStepY() * maxDistance + padding,
            dir.getStepZ() * maxDistance + padding
        );
        
        // Garantir que min <= max
        int minX = Math.min(min.getX(), max.getX());
        int maxX = Math.max(min.getX(), max.getX());
        int minY = Math.min(min.getY(), max.getY());
        int maxY = Math.max(min.getY(), max.getY());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxZ = Math.max(min.getZ(), max.getZ());
        
        return new OctreeRegion(
            new BlockPos(minX, minY, minZ),
            new BlockPos(maxX, maxY, maxZ)
        );
    }
    
    /**
     * Gera a lista de blocos que serão minerados baseado na task.
     */
    private List<BlockPos> generateMiningBlocks(MiningTaskData task) {
        List<BlockPos> blocks = new ArrayList<>();
        
        for (int i = 0; i < task.getTotalBlocks(); i++) {
            BlockPos blockPos = switch (task.getPattern()) {
                case STRAIGHT -> {
                    BlockPos offset = new BlockPos(
                        task.getDirection().getStepX() * i,
                        task.getDirection().getStepY() * i,
                        task.getDirection().getStepZ() * i
                    );
                    yield task.getStartPos().offset(offset);
                }
                case TUNNEL_2X1 -> {
                    int layer = i / 2; // Cada camada tem 2 blocos
                    int blockInLayer = i % 2; // Índice dentro da camada (0-1)
                    BlockPos layerOffset = new BlockPos(
                        task.getDirection().getStepX() * layer,
                        task.getDirection().getStepY() * layer,
                        task.getDirection().getStepZ() * layer
                    );
                    yield calculateTunnelBlock(task.getStartPos().offset(layerOffset), task.getDirection(), 2, blockInLayer);
                }
                case TUNNEL_3X3 -> {
                    int layer = i / 9; // Cada camada tem 9 blocos (3x3)
                    int blockInLayer = i % 9; // Índice dentro da camada (0-8)
                    BlockPos layerOffset = new BlockPos(
                        task.getDirection().getStepX() * layer,
                        task.getDirection().getStepY() * layer,
                        task.getDirection().getStepZ() * layer
                    );
                    yield calculateTunnelBlock(task.getStartPos().offset(layerOffset), task.getDirection(), 3, blockInLayer);
                }
                case STAIRCASE -> {
                    BlockPos offset = new BlockPos(
                        task.getDirection().getStepX() * i,
                        task.getDirection().getStepY() * i,
                        task.getDirection().getStepZ() * i
                    );
                    yield calculateStaircaseBlock(task.getStartPos().offset(offset), i);
                }
                case BRANCH -> {
                    BlockPos offset = new BlockPos(
                        task.getDirection().getStepX() * i,
                        task.getDirection().getStepY() * i,
                        task.getDirection().getStepZ() * i
                    );
                    yield calculateBranchBlock(task.getStartPos().offset(offset), task.getDirection(), i);
                }
            };
            
            blocks.add(blockPos);
        }
        
        return blocks;
    }
    
    /**
     * Otimiza a sequência de blocos usando informações da octree.
     */
    private List<BlockPos> optimizeBlockSequence(List<BlockPos> blocks, BlockPos start) {
        // Fallback se octree não estiver disponível
        if (octree == null) {
            ProjectAmi.LOGGER.warn("[MiningPathfinder] Octree não disponível, retornando blocos ordenados por distância");
            List<BlockPos> sorted = new ArrayList<>(blocks);
            sorted.sort(Comparator.comparingDouble(pos -> pos.distSqr(start)));
            return sorted;
        }
        
        // Separar blocos dentro e fora da octree
        Map<Boolean, List<BlockPos>> partitioned = blocks.stream()
            .collect(Collectors.partitioningBy(pos -> octree.findNode(pos) != null));
        
        List<BlockPos> blocksInOctree = partitioned.get(true);
        List<BlockPos> blocksOutsideOctree = partitioned.get(false);
        
        // Processar blocos DENTRO da octree
        List<BlockPos> result = new ArrayList<>();
        
        if (!blocksInOctree.isEmpty()) {
            // Agrupar blocos por nó da octree
            Map<OctreeNode, List<BlockPos>> blocksByNode = blocksInOctree.stream()
                .collect(Collectors.groupingBy(pos -> octree.findNode(pos)));
            
            // Ordenar nós por distância do start
            List<OctreeNode> sortedNodes = blocksByNode.keySet().stream()
                .sorted(Comparator.comparingDouble(node -> 
                    node.getRegion().getCenter().distSqr(start)))
                .toList();
            
            // Dentro de cada nó, ordenar do centro para fora
            for (OctreeNode node : sortedNodes) {
                BlockPos nodeCenter = node.getRegion().getCenter();
                List<BlockPos> nodeBlocks = blocksByNode.get(node);
                
                nodeBlocks.sort(Comparator.comparingDouble(
                    pos -> pos.distSqr(nodeCenter)
                ));
                
                result.addAll(nodeBlocks);
            }
        }
        
        // Adicionar blocos FORA da octree ao final (ordenados por distância)
        if (!blocksOutsideOctree.isEmpty()) {
            blocksOutsideOctree.sort(Comparator.comparingDouble(pos -> pos.distSqr(start)));
            result.addAll(blocksOutsideOctree);
            
            ProjectAmi.LOGGER.warn("[MiningPathfinder] {} blocos fora da octree foram adicionados ao final", 
                blocksOutsideOctree.size());
        }
        
        return result;
    }
    
    /**
     * Identifica obstáculos na navegação usando pathfinding.
     */
    private List<BlockPos> findNavigationObstacles(BlockPos start, List<BlockPos> targets) {
        List<BlockPos> obstacles = new ArrayList<>();
        
        // Inicializar pathfinder se ainda não foi feito
        if (pathfinder == null) {
            this.pathfinder = new OctreePathfinder(octree);
        }
        
        // Verificar caminho para cada bloco de destino
        for (BlockPos target : targets) {
            PathResult path = pathfinder.findPath(start, target, null);
            
            if (!path.isSuccess()) {
                // Adicionar obstáculos encontrados
                obstacles.addAll(path.getObstacles());
                
                // Tentar encontrar posição navegável mais próxima
                BlockPos nearest = pathfinder.findNearestNavigablePosition(target);
                if (!nearest.equals(target)) {
                    obstacles.add(nearest);
                }
            }
        }
        
        return obstacles.stream().distinct().toList();
    }
    
    /**
     * Encontra o próximo bloco acessível para mineração.
     */
    public BlockPos findNextAccessibleBlock(RobotEntity robot, MiningPathPlan plan) {
        BlockPos robotPos = robot.blockPosition();
        
        // Fallback para comportamento simples se octree não estiver disponível
        if (octree == null || pathfinder == null) {
            ProjectAmi.LOGGER.warn("[MiningPathfinder] Octree não disponível, usando fallback simples");
            return findNextAccessibleBlockSimple(robotPos, plan.getRemainingBlocks());
        }
        
        // Inicializar pathfinder se ainda não foi feito
        if (pathfinder == null) {
            this.pathfinder = new OctreePathfinder(octree);
        }
        
        // Usar octree para busca espacial eficiente
        OctreeNode robotNode = octree.findNode(robotPos);
        
        for (BlockPos candidate : plan.getRemainingBlocks()) {
            // 1. Verificar distância usando octree
            OctreeNode candidateNode = octree.findNode(candidate);
            if (candidateNode == null || !octree.areNodesReachable(robotNode, candidateNode)) {
                continue;
            }
            
            // 2. Pathfinding rápido
            PathResult path = pathfinder.findPath(robotPos, candidate, robot);
            
            if (path.isSuccess() && path.getPathLength() < OctreeConfig.MAX_PATH_LENGTH) {
                return candidate;
            }
        }
        
        return null;
    }
    
    /**
     * Método helper para encontrar próximo bloco usando algoritmo simples (fallback).
     */
    private BlockPos findNextAccessibleBlockSimple(BlockPos robotPos, List<BlockPos> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        
        return candidates.stream()
            .min(Comparator.comparingDouble(pos -> pos.distSqr(robotPos)))
            .orElse(null);
    }
    
    /**
     * Atualiza a octree após uma mudança no mundo.
     */
    public void updateOctree(BlockPos pos, net.minecraft.world.level.block.state.BlockState newState) {
        if (octree != null) {
            octree.update(pos, newState);
        }
    }
    
    // Métodos auxiliares para cálculo de blocos (copiados de MiningPlan.java)
    
    private BlockPos calculateTunnelBlock(BlockPos basePos, Direction direction, int width, int blockIndex) {
        if (width == 2) {
            // Túnel 2x1: alternar entre linha principal e superior
            if (blockIndex % 2 == 0) {
                return basePos;
            } else {
                return basePos.above();
            }
        } else if (width == 3) {
            // Túnel 3x3: minerar em um padrão 3x3
            int blockInRow = blockIndex % 9;
            Direction perpendicular = direction.getClockWise();
            
            if (blockInRow < 3) {
                // Linha esquerda (-1 perpendicular)
                int height = blockInRow;
                return basePos.relative(perpendicular.getOpposite()).above(height);
            } else if (blockInRow < 6) {
                // Linha centro (sem deslocamento lateral)
                int height = blockInRow - 3;
                return basePos.above(height);
            } else {
                // Linha direita (+1 perpendicular)
                int height = blockInRow - 6;
                return basePos.relative(perpendicular).above(height);
            }
        }
        
        return basePos;
    }
    
    private BlockPos calculateStaircaseBlock(BlockPos basePos, int blockIndex) {
        // Escada descendente - a cada 3 blocos desce 1
        if (blockIndex % 3 == 0) {
            return basePos.below();
        }
        return basePos;
    }
    
    private BlockPos calculateBranchBlock(BlockPos basePos, Direction direction, int blockIndex) {
        // Branch mining - criar galhos laterais a cada 5 blocos
        int cycle = blockIndex % 10;
        if (cycle < 3) {
            return basePos.relative(direction.getClockWise(), cycle);
        } else if (cycle < 6) {
            return basePos.relative(direction.getCounterClockWise(), cycle - 3);
        }
        return basePos;
    }
}
