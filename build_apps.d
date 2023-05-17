#!/usr/bin/env rdmd
/** 
 * A simple build script that builds all Gymboard applications and collects
 * build artifacts for deployment. You can run it with `./build_apps.d`, and it
 * will build each application for production deployment using a separate
 * thread.
 *
 * Supply the "clean" command to remove all build artifacts instead of building.
 *
 * Eventually, this will be migrated to gymboard-cli.
 */
module build_apps;

import std.stdio;
import std.process;
import std.file;
import std.path;
import std.regex;
import core.thread;

enum BUILDS_DIR = "build";

int main(string[] args) {
    if (args.length > 1 && args[1] == "clean") {
        writeln("Cleaning builds");
        if (exists(BUILDS_DIR)) rmdirRecurse(BUILDS_DIR);
        return 0;
    }
    Thread[] buildThreads = [
        runBuildInThread(API_BUILD_SPEC),
        runBuildInThread(CDN_BUILD_SPEC),
        runBuildInThread(SEARCH_BUILD_SPEC),
        runBuildInThread(APP_BUILD_SPEC),
        runBuildInThread(UPLOADS_BUILD_SPEC)
    ];
    
    foreach (t; buildThreads) t.join();

    return 0;
}

/** 
 * Specification for building an application.
 */
struct BuildSpec {
    string name;
    string workingDir;
    string buildCommand;
    typeof(ctRegex!(`\d`)) artifactRegex;
}

static immutable API_BUILD_SPEC = BuildSpec(
    "api",
    "gymboard-api",
    "./mvnw clean package spring-boot:repackage",
    ctRegex!(`target/gymboard-api-.*\.jar$`)
);

static immutable CDN_BUILD_SPEC = BuildSpec(
    "cdn",
    "gymboard-cdn",
    "./mvnw clean package spring-boot:repackage",
    ctRegex!(`target/gymboard-cdn-.*\.jar$`)
);

static immutable SEARCH_BUILD_SPEC = BuildSpec(
    "search",
    "gymboard-search",
    "./mvnw clean package spring-boot:repackage -DskipTests=true",
    ctRegex!(`target/gymboard-search-.*\.jar$`)
);

static immutable APP_BUILD_SPEC = BuildSpec(
    "app",
    "gymboard-app",
    "quasar build",
    ctRegex!(`gymboard-app/dist$`)
);

static immutable UPLOADS_BUILD_SPEC = BuildSpec(
    "uploads",
    "gymboard-uploads",
    "dub build --build=release",
    ctRegex!(`gymboard-uploads/gymboard-uploads$`)
);

/** 
 * Runs a build and returns its exit code.
 * Params:
 *   spec = The build spec to run.
 * Returns: The exit code of the build. 0 indicates success.
 */
int runBuild(const BuildSpec spec) {
    string buildDir = buildPath(BUILDS_DIR, spec.name);
    if (exists(buildDir)) rmdirRecurse(buildDir);
    mkdirRecurse(buildDir);
    const logFilePath = buildPath(buildDir, "build.log");
    const errorLogFilePath = buildPath(buildDir, "build-error.log");
    File buildLogFile = File(logFilePath, "w");
    File buildErrorLogFile = File(errorLogFilePath, "w");
    Pid pid = spawnShell(
        spec.buildCommand,
        std.stdio.stdin,
        buildLogFile,
        buildErrorLogFile,
        null,
        Config.none,
        spec.workingDir,
        nativeShell()
    );
    int result = wait(pid);
    if (result != 0) {
        writefln!"Build command failed for build \"%s\". Check %s for more info."(spec.name, errorLogFilePath);
        return result;
    }

    // Clean up unused log files.
    if (getSize(logFilePath) == 0) std.file.remove(logFilePath);
    if (getSize(errorLogFilePath) == 0) std.file.remove(errorLogFilePath);

    // Find and extract artifacts.
    bool artifactFound = false;
    foreach (DirEntry entry; dirEntries(spec.workingDir, SpanMode.breadth, false)) {
        Captures!string c = matchFirst(entry.name, spec.artifactRegex);
        if (!c.empty) {
            artifactFound = true;
            string destFilename = buildPath(buildDir, baseName(entry.name));
            writefln!"Copying artifact %s to %s"(entry.name, destFilename);
            if (entry.isFile) {
                writefln!"  Filesize: %d"(entry.size);
                copy(entry.name, destFilename);
            } else if (entry.isDir) {
                Pid cpPid = spawnShell("cp -R " ~ entry.name ~ " " ~ destFilename);
                int cpResult = wait(cpPid);
                if (cpResult != 0) {
                    writefln!"Failed to copy directory: %d"(cpResult);
                    return cpResult;
                }
            }
        }
    }
    if (!artifactFound) {
        writefln!"Warning: Build %s completed successfully, but no matching artifacts were found."(spec.name);
    }
    return 0;
}

/** 
 * Wraps building of a build spec in a thread.
 * Params:
 *   spec = The spec to build.
 * Returns: The thread running the build, which is already started.
 */
Thread runBuildInThread(const BuildSpec spec) {
    Thread t = new Thread(() {
        writefln!"Building \"%s\" in %s"(spec.name, spec.workingDir);
        int result = runBuild(spec);
        writefln!"Build \"%s\" exited with code %d."(spec.name, result);
    });
    t.start();
    return t;
}
