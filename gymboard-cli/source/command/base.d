module command.base;

import consolecolors;
import std.stdio;
import std.string;
import std.uni;

interface CliCommand {
    void handle(string[] args);
    string name() const;
    string description() const;
}

class CliHandler {
    private CliCommand[string] commands;
    private bool exitRequested = false;

    this() {
        commands["help"] = new HelpCommand(this);
        commands["exit"] = new ExitCommand(this);
    }

    void register(string name, CliCommand command) {
        this.commands[name.strip().toLower()] = command;
    }

    void readAndHandleCommand() {
        string[] commandAndArgs = readln().strip().split!isWhite();
        if (commandAndArgs.length == 0) return;
        string command = commandAndArgs[0].toLower();
        if (command in commands) {
            commands[command].handle(commandAndArgs.length > 1 ? commandAndArgs[1 .. $] : []);
        } else {
            cwritefln("Unknown command: %s".red, command.orange);
        }
    }

    void setExitRequested() {
        this.exitRequested = true;
    }

    bool isExitRequested() const {
        return this.exitRequested;
    }

    CliCommand[string] getCommands() {
        return this.commands;
    }
}

class HelpCommand : CliCommand {
    private CliHandler handler;

    this(CliHandler handler) {
        this.handler = handler;
    }

    void handle(string[] args) {
        import std.algorithm;

        cwriteln("<blue>Gymboard CLI Help</blue>: <grey>Information about how to use this program.</grey>");

        string[] commandNames = this.handler.getCommands().keys;
        sort(commandNames);
        uint longestCommandNameLength = commandNames.map!(n => cast(uint) n.length).maxElement;

        foreach (name; commandNames) {
            CliCommand command = this.handler.getCommands()[name];

            string formattedName = cyan(leftJustify(name, longestCommandNameLength + 1, ' '));
            cwriteln(formattedName, command.description().grey);
        }
    }

    string name() const {
        return "help";
    }

    string description() const {
        return "Shows help information.";
    }
}

class ExitCommand : CliCommand {
    private CliHandler handler;

    this(CliHandler handler) {
        this.handler = handler;
    }

    void handle(string[] args) {
        this.handler.setExitRequested();
    }

    string name() const {
        return "exit";
    }

    string description() const {
        return "Exits the CLI, gracefully stopping any services or running jobs.";
    }
}
