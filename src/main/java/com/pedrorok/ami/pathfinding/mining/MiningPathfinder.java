package com.pedrorok.ami.pathfinding.mining;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningConfig;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.pathfinding.octree.OctreeConfig;
import com.pedrorok.ami.pathfinding.octree.OctreeRegion;
import com.pedrorok.ami.pathfinding.octree.OctreeNode;
import com.pedrorok.ami.pathfinding.octree.SpatialOctree;
import com.pedrorok.ami.pathfinding.pathfinder.OctreePathfinder;
import com.pedrorok.ami.pathfinding.pathfinder.PathResult;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.*;

@Slf4j
public class MiningPathfinder {
    private OctreePathfinder pathfinder;
    private SpatialOctree octree;
    
    public MiningPathfinder() {
        this.pathfinder = null;
    }
    
    public MiningPathPlan planMining(MiningTaskData task, ServerLevel level, RobotEntity robot) {
        log.info("[MiningPathfinder] Planning mining operation: {} blocks distance, pattern: {}",
            task.getDistance(), task.getPattern());

        OctreeRegion miningRegion = calculateMiningRegion(task);
        octree = new SpatialOctree(level);
        octree.build(miningRegion);

        if (pathfinder == null) {
            this.pathfinder = new OctreePathfinder(octree);
        }

        List<BlockPos> blocksToMine = generateMiningBlocks(task, robot);

        log.info("[MiningPathfinder] Generated {} blocks with safe vertical ordering", blocksToMine.size());

        validateSequenceSafety(blocksToMine, robot);

        List<BlockPos> solidBlocksToMine = blocksToMine.stream()
            .filter(pos -> !level.getBlockState(pos).isAir())
            .toList();

        log.info("[MiningPathfinder] Generated {} total blocks, {} solid blocks to mine",
            blocksToMine.size(), solidBlocksToMine.size());

        if (solidBlocksToMine.isEmpty()) {
            log.warn("[MiningPathfinder] No solid blocks found to mine!");
            return new MiningPathPlan("Nenhum bloco sólido encontrado para minerar");
        }
		
	    List<BlockPos> obstacles = findNavigationObstacles(task.getStartPos(), solidBlocksToMine, robot);
        MiningPathPlan plan = new MiningPathPlan(solidBlocksToMine, obstacles, octree, this);

        log.info("[MiningPathfinder] Mining plan created: {} blocks, {} obstacles",
            solidBlocksToMine.size(), obstacles.size());

        return plan;
    }
    
    private OctreeRegion calculateMiningRegion(MiningTaskData task) {
        BlockPos start = task.getStartPos();
        Direction dir = task.getDirection();
        
        int maxDistance = task.getDistance();
        int padding = Math.max(16, maxDistance / 4);
        
        BlockPos min = start.offset(-padding, -padding, -padding);
        BlockPos max = start.offset(
            dir.getStepX() * maxDistance + padding,
            dir.getStepY() * maxDistance + padding,
            dir.getStepZ() * maxDistance + padding
        );
        
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
    
    private List<BlockPos> generateMiningBlocks(MiningTaskData task, RobotEntity robot) {
        List<BlockPos> blocks = new ArrayList<>();
        int minHeight = getMinimumTunnelHeight(robot);

        for (int distance = 1; distance <= task.getDistance(); distance++) {
            BlockPos layerBase = task.getStartPos().offset(
                task.getDirection().getStepX() * distance,
                task.getDirection().getStepY() * distance,
                task.getDirection().getStepZ() * distance
            );

            List<BlockPos> layerBlocks = generateLayerBlocks(layerBase, task.getPattern(), robot, minHeight, task.getDirection());
            blocks.addAll(layerBlocks);
        }

        return blocks;
    }
    
    private List<BlockPos> generateLayerBlocks(BlockPos layerBase, MiningTaskData.MiningPattern pattern, RobotEntity robot, int minHeight, Direction direction) {
        List<BlockPos> layerBlocks = new ArrayList<>();
        
        switch (pattern) {
            case STRAIGHT -> {
                for (int y = 0; y < minHeight; y++) {
                    layerBlocks.add(layerBase.above(y));
                }
            }
            case TUNNEL_2X1, TUNNEL_3X3 -> {
                layerBlocks.addAll(generateTunnelLayer(layerBase, pattern, robot, minHeight, direction));
            }
            case STAIRCASE -> {
                layerBlocks.addAll(generateStaircaseLayer(layerBase, robot, minHeight));
            }
            case BRANCH -> {
                layerBlocks.addAll(generateBranchLayer(layerBase, robot, minHeight));
            }
        }
        
        return layerBlocks;
    }
    
    private List<BlockPos> generateTunnelLayer(BlockPos layerBase, MiningTaskData.MiningPattern pattern, RobotEntity robot, int minHeight, Direction direction) {
        List<BlockPos> layerBlocks = new ArrayList<>();
        Direction perpendicular = direction.getClockWise();
        
        int width = pattern == MiningTaskData.MiningPattern.TUNNEL_2X1 ? 2 : 3;
        int effectiveWidth = Math.max(width, MiningConfig.getRobotWidth(robot));
        int effectiveHeight = Math.max(width, minHeight);
        
        for (int col = 0; col < effectiveWidth; col++) {
            BlockPos columnBase = layerBase.relative(perpendicular, col - effectiveWidth/2);
            
            for (int y = 0; y < effectiveHeight; y++) {
                layerBlocks.add(columnBase.above(y));
            }
        }
        
        return layerBlocks;
    }
    
    private List<BlockPos> generateStaircaseLayer(BlockPos layerBase, RobotEntity robot, int minHeight) {
        List<BlockPos> layerBlocks = new ArrayList<>();
        
        for (int y = 0; y < minHeight; y++) {
            layerBlocks.add(layerBase.below().above(y));
        }
        
        return layerBlocks;
    }
    
    private List<BlockPos> generateBranchLayer(BlockPos layerBase, RobotEntity robot, int minHeight) {
        List<BlockPos> layerBlocks = new ArrayList<>();
        
        for (int y = 0; y < minHeight; y++) {
            layerBlocks.add(layerBase.above(y));
        }
        
        return layerBlocks;
    }
    
    private int getMinimumTunnelHeight(RobotEntity robot) {
        if (robot == null) {
            return 2;
        }
        double robotHeight = robot.getBoundingBox().getYsize();
        return (int) Math.ceil(robotHeight);
    }
	
    private void validateSequenceSafety(List<BlockPos> blocks, RobotEntity robot) {
        Set<BlockPos> mined = new HashSet<>();
        int robotHeight = (int) Math.ceil(robot.getBoundingBox().getYsize());
        
        for (int i = 0; i < blocks.size(); i++) {
            BlockPos block = blocks.get(i);
            
            boolean hasSpace = false;
            for (int y = 0; y < robotHeight; y++) {
                BlockPos checkPos = block.below(y);
                if (mined.contains(checkPos)) {
                    hasSpace = true;
                    break;
                }
            }
            
            if (!hasSpace && i > 0) {
                log.warn("[MiningPathfinder] UNSAFE SEQUENCE at block {}: Robot may get stuck!", i);
            }
            
            mined.add(block);
        }
        
        log.info("[MiningPathfinder] Sequence validated: Safe for execution");
    }
    
    private List<BlockPos> findNavigationObstacles(BlockPos start, List<BlockPos> targets, RobotEntity entity) {
        List<BlockPos> obstacles = new ArrayList<>();
        
        if (pathfinder == null) {
            this.pathfinder = new OctreePathfinder(octree);
        }
        
        for (BlockPos target : targets) {
            PathResult path = pathfinder.findPath(start, target, entity);
            
            if (!path.isSuccess()) {
                obstacles.addAll(path.getObstacles());
                
                BlockPos nearest = pathfinder.findNearestNavigablePosition(target);
                if (!nearest.equals(target)) {
                    obstacles.add(nearest);
                }
            }
        }
        
        return obstacles.stream().distinct().toList();
    }
    
    public BlockPos findNextAccessibleBlock(RobotEntity robot, MiningPathPlan plan) {
        BlockPos robotPos = robot.blockPosition();
        
        if (octree == null || pathfinder == null) {
            log.warn("[MiningPathfinder] Octree não disponível, usando fallback simples");
            return findNextAccessibleBlockSimple(robotPos, plan.getRemainingBlocks());
        }
        
        if (pathfinder == null) {
            this.pathfinder = new OctreePathfinder(octree);
        }
        
        OctreeNode robotNode = octree.findNode(robotPos);
        
        for (BlockPos candidate : plan.getRemainingBlocks()) {
            OctreeNode candidateNode = octree.findNode(candidate);
            if (candidateNode == null || !octree.areNodesReachable(robotNode, candidateNode)) {
                continue;
            }
            
            PathResult path = pathfinder.findPath(robotPos, candidate, robot);
            
            if (path.isSuccess() && path.getPathLength() < OctreeConfig.MAX_PATH_LENGTH) {
                return candidate;
            }
        }
        
        return null;
    }
    
    private BlockPos findNextAccessibleBlockSimple(BlockPos robotPos, List<BlockPos> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        
        return candidates.stream()
            .min(Comparator.comparingDouble(pos -> pos.distSqr(robotPos)))
            .orElse(null);
    }
    
    public void updateOctree(BlockPos pos, net.minecraft.world.level.block.state.BlockState newState) {
        if (octree != null) {
            octree.update(pos, newState);
        }
    }
    
    private BlockPos calculateTunnelBlock(BlockPos basePos, Direction direction, int width, int blockIndex) {
        if (width == 2) {
            if (blockIndex % 2 == 0) {
                return basePos;
            } else {
                return basePos.above();
            }
        } else if (width == 3) {
            int blockInRow = blockIndex % 9;
            Direction perpendicular = direction.getClockWise();
            
            if (blockInRow < 3) {
                int height = blockInRow;
                return basePos.relative(perpendicular.getOpposite()).above(height);
            } else if (blockInRow < 6) {
                int height = blockInRow - 3;
                return basePos.above(height);
            } else {
                int height = blockInRow - 6;
                return basePos.relative(perpendicular).above(height);
            }
        }
        
        return basePos;
    }
    
    private BlockPos calculateStaircaseBlock(BlockPos basePos, int blockIndex) {
        if (blockIndex % 3 == 0) {
            return basePos.below();
        }
        return basePos;
    }
    
    private BlockPos calculateBranchBlock(BlockPos basePos, Direction direction, int blockIndex) {
        int cycle = blockIndex % 10;
        if (cycle < 3) {
            return basePos.relative(direction.getClockWise(), cycle);
        } else if (cycle < 6) {
            return basePos.relative(direction.getCounterClockWise(), cycle - 3);
        }
        return basePos;
    }
}

