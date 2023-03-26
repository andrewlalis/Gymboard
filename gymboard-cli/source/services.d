module services;

import std.array;
import std.process;
import std.stdio;
import std.string;
import std.typecons;
import core.thread;
import core.atomic;

import consolecolors;

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

    public bool startService(const ServiceInfo service) {
        if (service.name !in serviceRunners || !serviceRunners[service.name].isRunning) {
            // Start all dependencies first.
            foreach (string depName; service.dependencies) {
                bool result = startService(getServiceByName(depName).get);
                if (!result) {
                    cwritefln("Can't start %s because dependency %s couldn't be started.".red, service.name.orange, depName.orange);
                    return false;
                }
            }
            // Then start the process.
            cwritefln("Starting service %s".green, service.name.white);
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
            cwritefln("Service %s started.".green, service.name.white);
        }
        cwritefln("Service %s is already started.".green, service.name.white);
        return true;
    }

    public bool stopService(const ServiceInfo service) {
        if (service.name in serviceRunners && serviceRunners[service.name].isRunning) {
            int exitStatus = serviceRunners[service.name].stopService();
            cwritefln("Service %s exited with code <orange>%d</orange>.".green, service.name.white, exitStatus);
            return true;
        }
        cwritefln("Service %s already stopped.".green, service.name.white);
        return true;
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

    public void showLogs(const ServiceInfo service, size_t lineCount = 50) {
        if (service.name in serviceRunners) {
            serviceRunners[service.name].showOutput(lineCount);
        } else {
            cwritefln("Service %s has not been started.".red, service.name.orange);
        }
    }

    public void follow(const ServiceInfo service) {
        if (service.name in serviceRunners) {
            serviceRunners[service.name].setFollowing(true);
        } else {
            cwritefln("Service %s has not been started.".red, service.name.orange);
        }
    }
}

class ServiceRunner : Thread {
    private Pid processId;
    private File processStdin;
    private File processStdout;
    private File processStderr;
    public Nullable!int exitStatus;
    private FileGobbler stdoutGobbler;
    private FileGobbler stderrGobbler;

    public this(ProcessPipes pipes) {
        super(&this.run);
        this.processId = pipes.pid();
        this.processStdin = pipes.stdin();
        this.processStdout = pipes.stdout();
        this.processStderr = pipes.stderr();
        this.stdoutGobbler = new FileGobbler(pipes.stdout());
        this.stderrGobbler = new FileGobbler(pipes.stderr());
    }

    private void run() {
        this.stdoutGobbler.start();
        this.stderrGobbler.start();
        Tuple!(bool, "terminated", int, "status") result = tryWait(this.processId);
        while (!result.terminated) {
            Thread.sleep(msecs(1000));
            result = tryWait(this.processId);
        }
        this.exitStatus = result.status;
    }

    public int stopService() {
        if (!exitStatus.isNull) return exitStatus.get();

        version(Posix) {
            import core.sys.posix.signal : SIGINT, SIGTERM;
            kill(this.processId, SIGINT);
            kill(this.processId, SIGTERM);
        } else version(Windows) {
            kill(this.processId);
        }
        return wait(this.processId);
    }

    public void showOutput(size_t lineCount = 50) {
        this.stdoutGobbler.showOutput(lineCount);
    }

    public void showErrorOutput(size_t lineCount = 50) {
        this.stderrGobbler.showOutput(lineCount);
    }

    public void setFollowing(bool following) {
        this.stdoutGobbler.setFollowing(following);
        this.stderrGobbler.setFollowing(following);
    }
}

class FileGobbler : Thread {
    private string[] lines;
    private File file;
    private bool following;

    public this(File file) {
        super(&this.run);
        this.file = file;
        this.following = false;
    }

    private void run() {
        string line;
        while ((line = stripRight(this.file.readln())) !is null) {
            if (atomicLoad(this.following)) {
                writeln(line);
            }
            synchronized(this) {
                this.lines ~= line;
            }
        }
    }

    public void showOutput(size_t lineCount = 50) {
        synchronized(this) {
            size_t startIdx = 0;
            if (lines.length > lineCount) {
                startIdx = lines.length - lineCount;
            }
            foreach (line; lines[startIdx .. $]) writeln(line);
        }
    }

    public void setFollowing(bool f) {
        atomicStore(this.following, f);
    }
}
