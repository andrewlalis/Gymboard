module services;

import std.process;
import std.stdio;
import std.string;
import std.typecons;
import core.thread;

struct ServiceInfo {
    string name;
    string[] dependencies;
    string workingDir;
    string startupCommand;
}

const SERVICES = [
    ServiceInfo(
        "api",
        ["docker-deps"],
        "../gymboard-api",
        "./gen_keys.d && ./mvnw spring-boot:run -Dspring-boot.run.profiles=development"
    ),
    ServiceInfo(
        "cdn",
        [],
        "../gymboard-cdn",
        "./mvnw spring-boot:run -Dspring-boot.run.profiles=development"
    ),
    ServiceInfo(
        "search",
        ["docker-deps"],
        "../gymboard-search",
        "./mvnw spring-boot:run -Dspring-boot.run.profiles=development"
    ),
    ServiceInfo(
        "app",
        [],
        "../gymboard-app",
        "npm install && quasar dev"
    ),
    ServiceInfo(
        "docker-deps",
        [],
        "../",
        "docker-compose up"
    )
];

Nullable!(const(ServiceInfo)) getServiceByName(string name) {
    static foreach (service; SERVICES) {
        if (service.name == name) return nullable(service);
    }
    return Nullable!(const(ServiceInfo)).init;
}

struct ServiceStatus {
    string name;
    bool running;
    Nullable!int exitCode;
}

class ServiceManager {
    private ServiceRunner[string] serviceRunners;

    public Tuple!(bool, "started", string, "msg") startService(string name) {
        auto info = getServiceByName(name);
        if (info.isNull) return tuple!("started", "msg")(false, "Invalid service name.");
        const ServiceInfo service = info.get();
        if (service.name !in serviceRunners || !serviceRunners[service.name].isRunning) {
            // Start all dependencies first.
            foreach (string depName; service.dependencies) {
                auto result = startService(depName);
                if (!result.started) {
                    return tuple!("started", "msg")(
                        false,
                        format!"Couldn't start dependency \"%s\": %s"(depName, result.msg)
                    );
                }
            }
            // Then start the process.
            writefln!"Starting service: %s"(service.name);
            ProcessPipes pipes = pipeShell(
                service.startupCommand,
                Redirect.all,
                null,
                Config.none,
                service.workingDir
            );
            ServiceRunner runner = new ServiceRunner(pipes);
            runner.start();
            serviceRunners[service.name] = runner;
            return tuple!("started", "msg")(true, "Service started.");
        }
        return tuple!("started", "msg")(true, "Service already running.");
    }

    public Tuple!(bool, "stopped", string, "msg") stopService(string name) {
        auto info = getServiceByName(name);
        if (info.isNull) return tuple!("stopped", "msg")(false, "Invalid service name.");
        const ServiceInfo service = info.get();
        if (service.name in serviceRunners && serviceRunners[service.name].isRunning) {
            int exitStatus = serviceRunners[service.name].stopService();
            return tuple!("stopped", "msg")(
                true,
                format!"Service exited with status %d."(exitStatus)
            );
        }
        return tuple!("stopped", "msg")(true, "Service already stopped.");
    }

    public void stopAll() {
        foreach (name, runner; serviceRunners) {
            runner.stopService();
        }
    }

    public ServiceStatus[] getStatus() {
        ServiceStatus[] statuses;
        foreach (name, runner; serviceRunners) {
            statuses ~= ServiceStatus(name, runner.isRunning, runner.exitStatus);
        }
        return statuses;
    }
}

class ServiceRunner : Thread {
    private Pid processId;
    private File processStdin;
    private File processStdout;
    private File processStderr;
    public Nullable!int exitStatus;

    public this(ProcessPipes pipes) {
        super(&this.run);
        this.processId = pipes.pid();
        this.processStdin = pipes.stdin();
        this.processStdout = pipes.stdout();
        this.processStderr = pipes.stderr();
    }

    private void run() {
        Tuple!(bool, "terminated", int, "status") result = tryWait(this.processId);
        while (!result.terminated) {
            Thread.sleep(msecs(1000));
            result = tryWait(this.processId);
        }
        this.exitStatus = result.status;
    }

    public int stopService() {
        version(Posix) {
            import core.sys.posix.signal : SIGTERM;
            kill(this.processId, SIGTERM);
        } else version(Windows) {
            kill(this.processId);
        }
        return wait(this.processId);
    }
}
