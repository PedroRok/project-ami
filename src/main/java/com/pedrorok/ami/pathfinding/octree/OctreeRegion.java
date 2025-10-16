package com.pedrorok.ami.pathfinding.octree;

import net.minecraft.core.BlockPos;

/**
 * Representa uma região 3D no espaço do Minecraft.
 * Usado para definir os limites de nós da octree.
 */
public record OctreeRegion(BlockPos min, BlockPos max) {
    
    public int getSize() {
        return Math.max(Math.max(
            max.getX() - min.getX() + 1,
            max.getY() - min.getY() + 1
        ), max.getZ() - min.getZ() + 1);
    }
    
    public BlockPos getCenter() {
        return new BlockPos(
            (min.getX() + max.getX()) / 2,
            (min.getY() + max.getY()) / 2,
            (min.getZ() + max.getZ()) / 2
        );
    }
    
    public boolean contains(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
               pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
               pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }
    
    public OctreeRegion[] subdivide() {
        BlockPos center = getCenter();

        return new OctreeRegion[] {
            new OctreeRegion(min, center),
            new OctreeRegion(
                new BlockPos(center.getX() + 1, min.getY(), min.getZ()),
                new BlockPos(max.getX(), center.getY(), center.getZ())
            ),
            new OctreeRegion(
                new BlockPos(min.getX(), min.getY(), center.getZ() + 1),
                new BlockPos(center.getX(), center.getY(), max.getZ())
            ),
            new OctreeRegion(
                new BlockPos(center.getX() + 1, min.getY(), center.getZ() + 1),
                new BlockPos(max.getX(), center.getY(), max.getZ())
            ),
            new OctreeRegion(
                new BlockPos(min.getX(), center.getY() + 1, min.getZ()),
                new BlockPos(center.getX(), max.getY(), center.getZ())
            ),
            new OctreeRegion(
                new BlockPos(center.getX() + 1, center.getY() + 1, min.getZ()),
                new BlockPos(max.getX(), max.getY(), center.getZ())
            ),
            new OctreeRegion(
                new BlockPos(min.getX(), center.getY() + 1, center.getZ() + 1),
                new BlockPos(center.getX(), max.getY(), max.getZ())
            ),
            new OctreeRegion(
                new BlockPos(center.getX() + 1, center.getY() + 1, center.getZ() + 1),
                max
            )
        };
    }
    
    public int getVolume() {
        return (max.getX() - min.getX() + 1) *
               (max.getY() - min.getY() + 1) *
               (max.getZ() - min.getZ() + 1);
    }
    
    @Override
    public String toString() {
        return String.format("OctreeRegion[min=%s, max=%s, size=%d]", 
            min, max, getSize());
    }
}
