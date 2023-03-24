module cli;

import std.stdio;
import std.string;
import std.uni;

interface CliCommand {
    void handle(string[] args);
}

class CliHandler {
    private CliCommand[string] commands;
    public bool shouldExit = false;

    public void register(string name, CliCommand command) {
        this.commands[name] = command;
    }

    public void readAndHandleCommand() {
        string[] commandAndArgs = readln().strip().split!isWhite();
        if (commandAndArgs.length == 0) return;
        string command = commandAndArgs[0].toLower();
        if (command == "help") {
            showHelp();
        } else if (command == "exit") {
            shouldExit = true;
        } else if (command in commands) {
            commands[command].handle(commandAndArgs.length > 1 ? commandAndArgs[1 .. $] : []);
        } else {
            writefln!"Unknown command \"%s\"."(command);
        }
    }
}

void showHelp() {
    writeln(q"HELP
Gymboard CLI: A tool for streamlining development.

Commands:

help                    Shows this message.
exit                    Exits the CLI, stopping any running services.
HELP");
}

import services;

class ServiceCommand : CliCommand {
    private ServiceManager serviceManager;

    public this(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    void handle(string[] args) {
        if (args.length == 0) {
            writeln("Missing subcommand.");
            return;
        }
        string subcommand = args[0];
        if (subcommand == "status") {
            auto statuses = serviceManager.getStatus();
            if (statuses.length == 0) {
                writeln("No services running.");
            }
            foreach (status; statuses) {
                writefln!"%s: Running = %s, Exit code = %s"(status.name, status.running, status.exitCode);
            }
        } else if (subcommand == "start") {
            if (args.length < 2) {
                writeln("Missing service name.");
                return;
            }
            auto result = serviceManager.startService(args[1]);
            writeln(result.msg);
        } else if (subcommand == "stop") {
            if (args.length < 2) {
                writeln("Missing service name.");
                return;
            }
            auto result = serviceManager.stopService(args[1]);
            writeln(result.msg);
        } else {
            writeln("Unknown subcommand.");
        }
    }
}
