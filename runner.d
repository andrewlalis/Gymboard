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

alias CommandFunction = void function(string[] args);

int main() {
    while (true) {
        string[] command = readln().split!isWhite;

    }
}