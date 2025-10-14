package com.pedrorok.ami.pathfinding.pathfinder;

import com.pedrorok.ami.pathfinding.octree.OctreeNode;
import net.minecraft.core.BlockPos;

/**
 * Nó do caminho calculado pelo algoritmo A*.
 * Contém informações de custo e referência ao nó da octree.
 */
public class PathNode implements Comparable<PathNode> {
    public final BlockPos position;
    public final OctreeNode octreeNode;
    public double gCost; // Custo do início até aqui
    public double hCost; // Heurística até o objetivo
    public PathNode parent;
    
    public PathNode(BlockPos position, OctreeNode octreeNode) {
        this.position = position;
        this.octreeNode = octreeNode;
        this.gCost = 0;
        this.hCost = 0;
        this.parent = null;
    }
    
    public double fCost() {
        return gCost + hCost;
    }
    
    @Override
    public int compareTo(PathNode other) {
        return Double.compare(fCost(), other.fCost());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PathNode pathNode = (PathNode) obj;
        return position.equals(pathNode.position);
    }
    
    @Override
    public int hashCode() {
        return position.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("PathNode[pos=%s, fCost=%.2f]", position, fCost());
    }
}
