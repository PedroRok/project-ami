package com.pedrorok.ami.entities.robot.tasks.mining;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import com.pedrorok.ami.entities.robot.RobotEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MiningPlan {
	
    private final List<BlockPos> blocksToMine;
    private final List<BlockPos> obstaclesInPath;
    private final int estimatedTime;
    private final boolean isViable;
    private String failureReason;
    
    private int currentBlockIndex = 0;
    private int completedBlocks = 0;
    
    public MiningPlan(List<BlockPos> blocksToMine, List<BlockPos> obstaclesInPath) {
        this.blocksToMine = new ArrayList<>(blocksToMine);
        this.obstaclesInPath = new ArrayList<>(obstaclesInPath);
        this.estimatedTime = blocksToMine.size() * 20;
        this.isViable = !blocksToMine.isEmpty();
        this.failureReason = null;
        
        log.info("[MiningPlan] Criado plano com {} blocos e {} obstáculos", 
            blocksToMine.size(), obstaclesInPath.size());
    }
    
    public MiningPlan(String failureReason) {
        this.blocksToMine = new ArrayList<>();
        this.obstaclesInPath = new ArrayList<>();
        this.estimatedTime = 0;
        this.isViable = false;
        this.failureReason = failureReason;
        
        log.warn("[MiningPlan] Plano inviável: {}", failureReason);
    }
    
    // Getters
    public List<BlockPos> getBlocksToMine() { return blocksToMine; }
    public List<BlockPos> getObstaclesInPath() { return obstaclesInPath; }
    public int getEstimatedTime() { return estimatedTime; }
    public boolean isViable() { return isViable; }
    public String getFailureReason() { return failureReason; }
    public int getTotalBlocks() { return blocksToMine.size(); }
    public int getCompletedBlocks() { return completedBlocks; }
    public int getCurrentBlockIndex() { return currentBlockIndex; }
    
    /**
     * Retorna o próximo bloco a ser minerado
     */
    public BlockPos getNextBlockForRobot(RobotEntity robot, int searchRadius, Set<BlockPos> blacklist) {
        if (currentBlockIndex >= blocksToMine.size()) {
            return null;
        }
        
        BlockPos robotPos = robot.blockPosition();
        int searchLimit = Math.min(currentBlockIndex + searchRadius, blocksToMine.size());
        
        BlockPos closestBlock = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (int i = currentBlockIndex; i < searchLimit; i++) {
            BlockPos candidate = blocksToMine.get(i);
            
            if (blacklist != null && blacklist.contains(candidate)) {
                continue;
            }
            
            double distance = robotPos.distSqr(candidate);
            
            if (distance < closestDistance) {
                closestDistance = distance;
                closestBlock = candidate;
            }
        }
        
        return closestBlock != null ? closestBlock : (currentBlockIndex < blocksToMine.size() ? blocksToMine.get(currentBlockIndex) : null);
    }
    
    public void markBlockCompletedAt(BlockPos pos) {
        for (int i = currentBlockIndex; i < blocksToMine.size(); i++) {
            if (blocksToMine.get(i).equals(pos)) {
                if (i == currentBlockIndex) {
                    markBlockCompleted();
                } else {
                    blocksToMine.remove(i);
                    completedBlocks++;
                    log.debug("Bloco fora de ordem {} completado", pos);
                }
                return;
            }
        }
    }
    
    public BlockPos getNextBlock() {
        if (currentBlockIndex >= blocksToMine.size()) {
            return null;
        }
        return blocksToMine.get(currentBlockIndex);
    }
    
    public BlockPos getCurrentBlock() {
        if (currentBlockIndex >= blocksToMine.size()) {
            return null;
        }
        return blocksToMine.get(currentBlockIndex);
    }
    
    /**
     * Marca o bloco atual como completo e avança para o próximo
     */
    public void markBlockCompleted() {
        if (currentBlockIndex < blocksToMine.size()) {
            completedBlocks++;
            currentBlockIndex++;
            
            log.debug("[MiningPlan] Bloco {} completado. Progresso: {}/{}", 
                currentBlockIndex - 1, completedBlocks, blocksToMine.size());
        }
    }
    
    /**
     * Verifica se o plano está completo
     */
    public boolean isComplete() {
        return currentBlockIndex >= blocksToMine.size();
    }
    
    /**
     * Retorna o progresso em porcentagem (0-100)
     */
    public int getProgressPercentage() {
        if (blocksToMine.isEmpty()) return 100;
        return (completedBlocks * 100) / blocksToMine.size();
    }
    
    /**
     * Valida se o plano ainda é viável (ex: blocos ainda existem)
     */
    public boolean validatePlan(Level level) {
        if (!isViable) return false;
        
        // Verificar se todos os blocos ainda existem e são mineráveis
        for (BlockPos block : blocksToMine) {
            if (level.getBlockState(block).isAir()) {
                log.warn("[MiningPlan] Bloco {} já foi minerado!", block);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Ordenação inteligente que respeita o pattern mas otimiza a sequência.
     * Estratégia: Híbrida + Manter todos + Centro para fora
     */
    private static List<BlockPos> smartSort(List<BlockPos> blocks, MiningTaskData task, Level level) {
        return switch (task.getPattern()) {
            case STRAIGHT -> 
                // Já é sequencial, ordenar por distância do startPos
                blocks.stream()
                    .sorted(Comparator.comparingDouble(pos -> 
                        pos.distSqr(task.getStartPos())))
                    .toList();
                
            case TUNNEL_2X1, TUNNEL_3X3 -> 
                // Minerar em camadas, cada camada do centro para fora
                sortTunnelBlocks(blocks, task);
                
            case STAIRCASE -> 
                // Escada: minerar de cima para baixo (evita queda)
                blocks.stream()
                    .sorted(Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed()
                        .thenComparingDouble(pos -> pos.distSqr(task.getStartPos())))
                    .toList();
                
            case BRANCH -> 
                // Branch: alternar entre corredor principal e galhos
                sortBranchBlocks(blocks, task);
        };
    }
    
    /**
     * Ordenação para túneis: agrupar por camadas (distância na direção do túnel),
     * dentro de cada camada ordenar do centro para fora.
     */
    private static List<BlockPos> sortTunnelBlocks(List<BlockPos> blocks, MiningTaskData task) {
        Direction dir = task.getDirection();
        BlockPos start = task.getStartPos();
        
        // Agrupar por camadas (distância na direção principal)
        Map<Integer, List<BlockPos>> layers = blocks.stream()
            .collect(Collectors.groupingBy(pos -> {
                // Distância na direção do túnel (absoluta)
                return Math.abs(
                    (pos.getX() - start.getX()) * dir.getStepX() +
                    (pos.getZ() - start.getZ()) * dir.getStepZ()
                );
            }));
        
        // Ordenar camadas e dentro de cada camada do centro para fora
        return layers.entrySet().stream()
            .sorted(Map.Entry.comparingByKey()) // Camadas por ordem de distância
            .flatMap(entry -> {
                List<BlockPos> layerBlocks = entry.getValue();
                
                // Encontrar centro da camada
                BlockPos layerCenter = findLayerCenter(layerBlocks);
                
                // Ordenar blocos da camada por distância do centro
                return layerBlocks.stream()
                    .sorted(Comparator.comparingDouble(pos -> 
                        pos.distSqr(layerCenter)));
            })
            .toList();
    }
    
    /**
     * Ordenação para branch mining: corredor principal primeiro,
     * depois galhos ordenados por proximidade ao corredor.
     */
    private static List<BlockPos> sortBranchBlocks(List<BlockPos> blocks, MiningTaskData task) {
        Direction dir = task.getDirection();
        BlockPos start = task.getStartPos();
        Direction perpendicular = dir.getClockWise();
        
        // Separar em corredor principal e galhos
        List<BlockPos> mainCorridor = new ArrayList<>();
        List<BlockPos> branches = new ArrayList<>();
        
        for (BlockPos pos : blocks) {
            int perpDist = Math.abs(
                (pos.getX() - start.getX()) * perpendicular.getStepX() +
                (pos.getZ() - start.getZ()) * perpendicular.getStepZ()
            );
            
            if (perpDist == 0) {
                mainCorridor.add(pos); // Está no corredor principal
            } else {
                branches.add(pos); // Está em um galho
            }
        }
        
        // Ordenar corredor por distância do start
        mainCorridor.sort(Comparator.comparingDouble(pos -> pos.distSqr(start)));
        
        // Ordenar galhos por distância do ponto mais próximo no corredor
        branches.sort(Comparator.comparingDouble(pos -> {
            // Encontrar distância do ponto mais próximo no corredor
            return mainCorridor.stream()
                .mapToDouble(pos::distSqr)
                .min()
                .orElse(Double.MAX_VALUE);
        }));
        
        // Mesclar: corredor principal primeiro, depois galhos
        List<BlockPos> result = new ArrayList<>(mainCorridor);
        result.addAll(branches);
        
        return result;
    }
    
    /**
     * Encontra o centro geométrico de uma lista de blocos.
     * Usado para ordenar blocos de uma camada do centro para fora.
     */
    private static BlockPos findLayerCenter(List<BlockPos> layerBlocks) {
        if (layerBlocks.isEmpty()) return BlockPos.ZERO;
        
        int avgX = (int) layerBlocks.stream().mapToInt(BlockPos::getX).average().orElse(0);
        int avgY = (int) layerBlocks.stream().mapToInt(BlockPos::getY).average().orElse(0);
        int avgZ = (int) layerBlocks.stream().mapToInt(BlockPos::getZ).average().orElse(0);
        
        return new BlockPos(avgX, avgY, avgZ);
    }
    
    private static BlockPos calculateTunnelBlock(BlockPos basePos, net.minecraft.core.Direction direction, int width, int blockIndex) {
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
            net.minecraft.core.Direction perpendicular = direction.getClockWise();
            
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
    
    private static BlockPos calculateStaircaseBlock(BlockPos basePos, int blockIndex) {
        // Escada descendente - a cada 3 blocos desce 1
        if (blockIndex % 3 == 0) {
            return basePos.below();
        }
        return basePos;
    }
    
    private static BlockPos calculateBranchBlock(BlockPos basePos, net.minecraft.core.Direction direction, int blockIndex) {
        // Branch mining - criar galhos laterais a cada 5 blocos
        int cycle = blockIndex % 10;
        if (cycle < 3) {
            return basePos.relative(direction.getClockWise(), cycle);
        } else if (cycle < 6) {
            return basePos.relative(direction.getCounterClockWise(), cycle - 3);
        }
        return basePos;
    }
    
    private static List<BlockPos> findNavigationObstacles(BlockPos startPos, List<BlockPos> targetBlocks, Level level) {
        List<BlockPos> obstacles = new ArrayList<>();
        
        // Simplificado: verificar se há obstáculos entre startPos e o primeiro bloco
        if (!targetBlocks.isEmpty()) {
            BlockPos firstBlock = targetBlocks.get(0);
            
            // Verificar linha de visão simples
            BlockPos current = startPos;
            while (!current.equals(firstBlock)) {
                if (!level.getBlockState(current).isAir()) {
                    obstacles.add(current);
                }
                
                // Mover em direção ao primeiro bloco
                int dx = Integer.compare(firstBlock.getX(), current.getX());
                int dy = Integer.compare(firstBlock.getY(), current.getY());
                int dz = Integer.compare(firstBlock.getZ(), current.getZ());
                
                current = current.offset(dx, dy, dz);
            }
        }
        
        return obstacles;
    }
}
