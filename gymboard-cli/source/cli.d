module cli;

import std.stdio;
import std.string;
import std.uni;
import std.typecons;

import consolecolors;

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
            cwritefln("Unknown command: %s".red, command.orange);
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
            cwriteln("Missing subcommand.".red);
            return;
        }
        string subcommand = args[0];
        if (subcommand == "status") {
            auto statuses = serviceManager.getStatus();
            if (statuses.length == 0) {
                cwriteln("No services running.".orange);
            }
            foreach (status; statuses) {
                writefln!"%s: Running = %s, Exit code = %s"(status.name, status.running, status.exitCode);
            }
        } else if (subcommand == "start") {
            auto info = validateServiceNameArg(args);
            if (!info.isNull) serviceManager.startService(info.get);
        } else if (subcommand == "stop") {
            auto info = validateServiceNameArg(args);
            if (!info.isNull) serviceManager.stopService(info.get);
        } else if (subcommand == "logs") {
            auto info = validateServiceNameArg(args);
            if (!info.isNull) serviceManager.showLogs(info.get);
        } else if (subcommand == "follow") {
            auto info = validateServiceNameArg(args);
            if (!info.isNull) serviceManager.follow(info.get);
        } else {
            cwriteln("Unknown subcommand.".red);
        }
    }

    /** 
     * Validates that a service command contains as its second argument a valid
     * service name.
     * Params:
     *   args = The arguments.
     * Returns: The service name, or null if none was found.
     */
    private Nullable!(const(ServiceInfo)) validateServiceNameArg(string[] args) {
        import std.string : toLower, strip;
        if (args.length < 2) {
            cwriteln("Missing required service name as argument to the service subcommand.".red);
            return Nullable!(const(ServiceInfo)).init;
        }
        string serviceName = args[1].strip().toLower();
        Nullable!(const(ServiceInfo)) info = getServiceByName(serviceName);
        if (info.isNull) {
            cwritefln("There is no service named %s.".red, serviceName.orange);
        }
        return info;
    }
}
