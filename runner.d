#!/usr/bin/env rdmd

/**
 * TODO: This module will eventually serve as some sort of setup script for
 * if/when it becomes too complicated to just start the services. It should
 * run as a CLI thing for entering commands to start/stop things.
 */
module runner;

import std.process;
import std.stdio;
import std.string;
import std.uni;
import core.thread;

int main() {
    bool running = true;
    writeln("Gymboard CLI: Type \"help\" for more information. Type \"exit\" to exit the CLI.");
    while (running) {
        string[] commandAndArgs = readln().strip.split!isWhite;
        if (commandAndArgs.length == 0) continue;
        string command = commandAndArgs[0].toLower();
        if (command == "help") {
            showHelp();
        } else if (command == "exit") {
            running = false;
        } else if (command in commands) {
            commands[command](commandAndArgs.length > 1 ? commandAndArgs[1 .. $] : []);
        } else {
            writefln!"Unknown command \"%s\"."(command);
        }
    }
    writeln("Goodbye!");
    return 0;
}

alias CommandFunction = void function(string[] args);

CommandFunction[string] commands;

void registerCommand(string name, CommandFunction func) {
    commands[name] = func;
}

void showHelp() {
    writeln(q"HELP
Gymboard CLI: A tool for streamlining development.

Commands:

help                    Shows this message.
exit                    Exits the CLI, stopping any running services.
HELP");
}

class ProcessRunner : Thread {
    private Pid processId;

    public this(ProcessPipes pipes) {
        super(&this.run);
        this.processId = pipes.pid();
    }

    private void run() {

    }
}
