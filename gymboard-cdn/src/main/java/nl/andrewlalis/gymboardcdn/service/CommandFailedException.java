package nl.andrewlalis.gymboardcdn.service;

import java.io.IOException;
import java.nio.file.Path;

public class CommandFailedException extends IOException {
	private final Path stdoutFile;
	private final Path stderrFile;
	private final int exitCode;
	private final String[] command;

	public CommandFailedException(String[] command, int exitCode, Path stdoutFile, Path stderrFile) {
		super(
				String.format(
						"Command \"%s\" exited with code %d. stdout available at %s and stderr available at %s.",
						String.join(" ", command),
						exitCode,
						stdoutFile.toAbsolutePath(),
						stderrFile.toAbsolutePath()
				)
		);
		this.command = command;
		this.exitCode = exitCode;
		this.stdoutFile = stdoutFile;
		this.stderrFile = stderrFile;
	}

	public Path getStdoutFile() {
		return stdoutFile;
	}

	public Path getStderrFile() {
		return stderrFile;
	}

	public int getExitCode() {
		return exitCode;
	}

	public String[] getCommand() {
		return command;
	}
}
