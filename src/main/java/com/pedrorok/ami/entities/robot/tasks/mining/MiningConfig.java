package com.pedrorok.ami.entities.robot.tasks.mining;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData.MiningPattern;
import net.minecraft.world.phys.AABB;

public class MiningConfig {
    public static final int MAX_DISTANCE = 100;
    public static final int MIN_DISTANCE = 1;
    public static final int SCAN_RANGE = 10;
    public static final int MIN_ROBOT_HEIGHT = 2;
    
    public static int getBlocksPerLayer(MiningPattern pattern, RobotEntity robot) {
        int robotHeight = getRobotHeight(robot);
        int robotWidth = getRobotWidth(robot);
        
        return switch (pattern) {
            case STRAIGHT, BRANCH -> Math.max(robotWidth, 1) * robotHeight;
            case TUNNEL_2X1 -> Math.max(robotWidth, 2) * robotHeight;
            case TUNNEL_3X3 -> Math.max(robotWidth, 3) * Math.max(robotHeight, 3);
            case STAIRCASE -> Math.max(robotWidth, 1) * (robotHeight + 1);
        };
    }
    
    public static int getRobotHeight(RobotEntity robot) {
        AABB boundingBox = robot.getBoundingBox();
        return Math.max((int) Math.ceil(boundingBox.getYsize()), MIN_ROBOT_HEIGHT);
    }
    
    public static int getRobotWidth(RobotEntity robot) {
        AABB boundingBox = robot.getBoundingBox();
        return Math.max((int) Math.ceil(Math.max(boundingBox.getXsize(), boundingBox.getZsize())), 1);
    }
    
    public static boolean canRobotFitInTunnel(RobotEntity robot, MiningPattern pattern) {
        int robotHeight = getRobotHeight(robot);
        int robotWidth = getRobotWidth(robot);
        
        return switch (pattern) {
            case STRAIGHT -> robotWidth <= 1 && robotHeight <= 2;
            case TUNNEL_2X1 -> robotWidth <= 2 && robotHeight <= 2;
            case TUNNEL_3X3 -> robotWidth <= 3 && robotHeight <= 3;
            case STAIRCASE -> robotWidth <= 1 && robotHeight <= 2;
            case BRANCH -> robotWidth <= 1 && robotHeight <= 2;
        };
    }
}
