package deepSRL;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeepSRLBuilder {

	private ExecutorService executor;
	private File pythonExecutable;
	private File deepSRLFile;
	private File modelFile;
	private File pidmodelFile;
	private List<CommandOption> commandOptions = new ArrayList<>();
	private OutputStream errorStream = System.err;

	private static enum CommandOption {

		/** Path to the DeepSRL Model */
		MODEL("--model"),

		/** Path to the desired Propid Model */
		PIDMODEL("--pidmodel");

		private String command;
		private File file;

		private CommandOption(String command) {
			this.command = command;
		}

		private CommandOption withFile(File file) {
			this.file = file;
			return this;
		}

		private List<String> getCommandString() {
			if (file != null) {
				return new ArrayList<>(Arrays.asList(command, file.getAbsolutePath()));
			} else {
				return new ArrayList<>(Arrays.asList(command));
			}
		}
	}

	public DeepSRLBuilder(File deepSRLFile, File modelFile, File pidmodelFile, File pythonExecutable) {
		this.deepSRLFile = deepSRLFile;
		this.modelFile = modelFile;
		this.pidmodelFile = pidmodelFile;
		this.pythonExecutable = pythonExecutable;
	}

	public DeepSRL build() {
		List<String> command = new ArrayList<>();
		command.add(pythonExecutable.getAbsolutePath());
		command.add(deepSRLFile.getAbsolutePath());
		commandOptions.add(CommandOption.MODEL.withFile(modelFile));
		commandOptions.add(CommandOption.PIDMODEL.withFile(pidmodelFile));

		for (CommandOption commandOption : commandOptions) {
			command.addAll(commandOption.getCommandString());
		}

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(deepSRLFile.getParentFile());
		ExecutorService executor = this.executor != null ? this.executor : Executors.newCachedThreadPool();
		return new DeepSRL(executor, errorStream, processBuilder);
	}

}
