package com.pedrorok.ami.system.dialog.actions;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.system.dialog.DialogueAnimationHelper;

/**
 * Ação para executar animações em entidades durante o diálogo
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class MoodAction implements DialogueAction {

    private final String moodName;
    private final int timeInSec;

    public MoodAction() {
        this.moodName = "";
        this.timeInSec = 0;
    }

    public MoodAction(String moodName, int timeInSec) {
        this.moodName = moodName;
        this.timeInSec = timeInSec;
    }
    
    @Override
    public boolean execute(ActionContext context) {
        if (context.entity() instanceof RobotEntity robot && !moodName.isEmpty()) {
            robot.playMood(moodName, timeInSec);
            return true;
        }
        return false;
    }
    
    @Override
    public String getCommandPattern() {
        return "mood_\\w+_\\d+";
    }

    public static MoodAction forMood(String moodName, int timeInSec) {
        return new MoodAction(moodName, timeInSec);
    }
}
