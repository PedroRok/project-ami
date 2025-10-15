package com.pedrorok.ami.commands.parser;

import com.pedrorok.ami.commands.ParsedCommand;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CommandParser {
    private final List<CommandMatcher> matchers = List.of(
        new MineCommandMatcher(),
        new StopCommandMatcher(),
        new StatusCommandMatcher()
    );
    
    public Optional<ParsedCommand> parse(String input) {
        return matchers.stream()
            .map(matcher -> matcher.match(input))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
    }
}
