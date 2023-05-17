#!/usr/bin/env rdmd
/**
 * Run this script with `./build.d` to prepare the latest version of the CLI
 * for use. It compiles the CLI application, and copies a "cli" executable to
 * the project's root directory for use.
 */
module build_cli;

import std.stdio;
import std.process;
import std.file;
import std.path;

int main() {
    writeln("Building...");
    const mainDir = getcwd();
    chdir("gymboard-cli");
    auto result = executeShell("dub build --build=release");
    if (result.status != 0) {
        stderr.writefln!"Build failed: %d"(result.status);
        stderr.writeln(result.output);
        return result.status;
    }
    string finalPath = buildPath("..", "cli");
    if (exists(finalPath)) std.file.remove(finalPath);
    version (Posix) {
        string sourceExecutable = "gymboard-cli";
    }
    version (Windows) {
        string sourceExecutable = "gymboard-cli.exe";
    }
    std.file.copy(sourceExecutable, finalPath);
    version (Posix) {
        result = executeShell("chmod +x " ~ finalPath);
        if (result.status != 0) {
            stderr.writefln!"Failed to enable executable permission: %d"(result.status);
            return result.status;
        }
    }
    writeln("Done! Run ./cli to start using Gymboard CLI.");
    return 0;
}
