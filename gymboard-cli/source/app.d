import std.stdio;

import cli;
import services;

import consolecolors;

void main() {
	ServiceManager serviceManager = new ServiceManager();
	CliHandler cliHandler = new CliHandler();
	cliHandler.register("service", new ServiceCommand(serviceManager));
	cwriteln("Gymboard CLI: Type <cyan>help</cyan> for more information. Type <red>exit</red> to exit the CLI.");
	while (!cliHandler.shouldExit) {
		cwrite("&gt; ".blue);
		cliHandler.readAndHandleCommand();
	}
	serviceManager.stopAll();
	cwriteln("Goodbye!".green);
}
