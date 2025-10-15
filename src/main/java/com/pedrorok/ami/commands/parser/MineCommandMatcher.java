package com.pedrorok.ami.commands.parser;

import com.pedrorok.ami.commands.MineCommand;
import com.pedrorok.ami.commands.ParsedCommand;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData.MiningPattern;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MineCommandMatcher implements CommandMatcher {
    private static final Pattern PATTERN = Pattern.compile(
        "(?:mine|minere|cave|escave)\\s+" +
        "(?<pattern>straight|tunnel2x1|tunnel3x3|staircase|branch|reto|tunel2x1|tunel3x3|escada|galho)\\s+" +
        "(?<distance>\\d+)",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public Optional<ParsedCommand> match(String input) {
        Matcher m = PATTERN.matcher(input.trim());
        if (!m.matches()) return Optional.empty();
        
        String patternStr = m.group("pattern");
        int distance = Integer.parseInt(m.group("distance"));
        
        return Optional.of(new MineCommand(
            parsePattern(patternStr),
            distance
        ));
    }
    
    private MiningPattern parsePattern(String patternStr) {
        return switch (patternStr.toLowerCase()) {
            case "straight", "reto" -> MiningPattern.STRAIGHT;
            case "tunnel2x1", "tunel2x1" -> MiningPattern.TUNNEL_2X1;
            case "tunnel3x3", "tunel3x3" -> MiningPattern.TUNNEL_3X3;
            case "staircase", "escada" -> MiningPattern.STAIRCASE;
            case "branch", "galho" -> MiningPattern.BRANCH;
            default -> MiningPattern.STRAIGHT;
        };
    }
}
