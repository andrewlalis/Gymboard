import std.stdio;

import cli;
import command;
import services;

import consolecolors;

void main() {
	ServiceManager serviceManager = new ServiceManager();
	CliHandler cliHandler = new CliHandler();
	cliHandler.register("service", new ServiceCommand(serviceManager));
	cwriteln("\n<blue>Gymboard CLI</blue>: <grey>Command-line interface for managing Gymboard services.</grey>");
	cwriteln("  Type <cyan>help</cyan> for more information.\n  Type <red>exit</red> to exit the CLI.\n");
	while (!cliHandler.isExitRequested) {
		cwrite("&gt; ".blue);
		cliHandler.readAndHandleCommand();
	}
	serviceManager.stopAll();
	cwriteln("Goodbye!".blue);
}
