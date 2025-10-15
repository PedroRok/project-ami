package com.pedrorok.ami.commands.parser;

import com.pedrorok.ami.commands.ParsedCommand;
import java.util.Optional;

public interface CommandMatcher {
    Optional<ParsedCommand> match(String input);
}
