package com.pedrorok.ami.commands.parser;

import com.pedrorok.ami.commands.ParsedCommand;
import com.pedrorok.ami.commands.StatusCommand;
import java.util.Optional;
import java.util.regex.Pattern;

public class StatusCommandMatcher implements CommandMatcher {
    private static final Pattern PATTERN = Pattern.compile(
        "(?:status|estado|info)",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public Optional<ParsedCommand> match(String input) {
        if (PATTERN.matcher(input.trim()).matches()) {
            return Optional.of(new StatusCommand());
        }
        return Optional.empty();
    }
}
