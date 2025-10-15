package com.pedrorok.ami.commands;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningConfig;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Slf4j
@Data
@AllArgsConstructor
public class MineCommand implements ParsedCommand {
    private final MiningTaskData.MiningPattern pattern;
    private final int distance;
    
    @Override
    public boolean execute(RobotEntity robot, Player player) {
        if (!validatePreconditions(robot, player)) return false;
        
        Direction direction = calculateDirection(player);
        BlockPos startPos = findStartPosition(player, direction);
        
        if (startPos == null) {
            sendMessage(player, "<red>[A.M.I.]</red> <gray>No solid blocks found in that direction!</gray>");
            return false;
        }
        
        MiningTaskData task = new MiningTaskData(
            direction, 
            distance, 
            pattern, 
            startPos
        );
        
        robot.getBrain().setMemory(ModMemoryModuleTypes.CURRENT_TASK.get(), task);
        
        String patternName = pattern.getSerializedName();
        String directionName = direction.getName();
        sendMessage(player, "<green>[A.M.I.]</green> <gray>Mining task started! " + distance + " blocks " + directionName + " using " + patternName + " pattern.</gray>");
        return true;
    }
    
    @Override
    public String getDescription() {
        return "Mine " + distance + " blocks using " + pattern.getSerializedName() + " pattern";
    }
    
    private boolean validatePreconditions(RobotEntity robot, Player player) {
        if (distance <= 0 || distance > MiningConfig.MAX_DISTANCE) {
            sendMessage(player, "<red>[A.M.I.]</red> <gray>Invalid distance! Use between 1 and " + MiningConfig.MAX_DISTANCE + ".</gray>");
            return false;
        }
        
        if (!(robot.getMainHandItem().getItem() instanceof net.minecraft.world.item.DiggerItem)) {
            sendMessage(player, "<red>[A.M.I.]</red> <gray>I need a pickaxe to mine!</gray>");
            return false;
        }
        
        return true;
    }
    
    private Direction calculateDirection(Player player) {
        return Direction.fromYRot(player.getYRot());
    }
    
    private BlockPos findStartPosition(Player player, Direction direction) {
        BlockPos scanPos = player.blockPosition();
        Level level = player.level();
        
        for (int i = 0; i < MiningConfig.SCAN_RANGE; i++) {
            if (!level.getBlockState(scanPos).isAir()) {
                return scanPos.relative(direction.getOpposite());
            }
            scanPos = scanPos.relative(direction);
        }
        
        return null;
    }
    
    private void sendMessage(Player player, String message) {
        com.pedrorok.ami.ProjectAmi.adventure().audience(player).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
