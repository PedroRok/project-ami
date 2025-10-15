package com.pedrorok.ami.commands.parser;

import com.pedrorok.ami.commands.ParsedCommand;
import com.pedrorok.ami.commands.StopCommand;
import java.util.Optional;
import java.util.regex.Pattern;

public class StopCommandMatcher implements CommandMatcher {
    private static final Pattern PATTERN = Pattern.compile(
        "(?:stop|parar|cancel|cancelar)",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public Optional<ParsedCommand> match(String input) {
        if (PATTERN.matcher(input.trim()).matches()) {
            return Optional.of(new StopCommand());
        }
        return Optional.empty();
    }
}
