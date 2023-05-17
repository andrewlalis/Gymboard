module cli;

import std.stdio;
import std.string;
import std.uni;
import std.typecons;

import consolecolors;

import command.base;
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

    string name() const {
        return "Service";
    }

    string description() const {
        return "bleh";
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
