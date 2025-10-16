package com.pedrorok.ami.pathfinding.octree;

/**
 * Configurações para o sistema de octree.
 */
public class OctreeConfig {
    public static final int MIN_NODE_SIZE = 2;
    public static final int MAX_NODE_SIZE = 64;
    public static final int DENSITY_THRESHOLD_LOW = 30;
    public static final int DENSITY_THRESHOLD_HIGH = 70;
    public static final int MAX_DEPTH = 6;
    public static final int SAMPLE_RATE = 4;

    public static final double MIXED_NODE_COST_MULTIPLIER = 1.5;
    public static final double HEIGHT_CHANGE_PENALTY = 2.0;
    public static final int MAX_PATH_LENGTH = 100;
}
