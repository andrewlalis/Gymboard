import std.stdio;

import cli;
import services;

void main() {
	ServiceManager serviceManager = new ServiceManager();
	CliHandler cliHandler = new CliHandler();
	cliHandler.register("service", new ServiceCommand(serviceManager));
	writeln("Gymboard CLI: Type \"help\" for more information. Type \"exit\" to exit the CLI.");
	while (!cliHandler.shouldExit) {
		cliHandler.readAndHandleCommand();
	}
	serviceManager.stopAll();
	writeln("Goodbye!");
}
