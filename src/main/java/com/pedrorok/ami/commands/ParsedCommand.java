package com.pedrorok.ami.commands;

import com.pedrorok.ami.entities.robot.RobotEntity;
import net.minecraft.world.entity.player.Player;

public interface ParsedCommand {
    boolean execute(RobotEntity robot, Player player);
    String getDescription();
}
