package deepSRL;

import java.io.File;
import java.io.IOException;
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

		private List<String> getCommand() {
			if (file != null) {
				return new ArrayList<>(Arrays.asList(command, file.getAbsolutePath()));
			} else {
				return new ArrayList<>(Arrays.asList(command));
			}
		}
	}

	public DeepSRLBuilder(File pythonExecutable, File deepSRLFile, File modelFile, File pidmodelFile) {
		this.pythonExecutable = pythonExecutable;
		this.deepSRLFile = deepSRLFile;
		this.modelFile = modelFile;
		this.pidmodelFile = pidmodelFile;
	}

	public DeepSRLBuilder withErrorStream(OutputStream errorStream) {
		this.errorStream = errorStream;
		return this;
	}

	public DeepSRLBuilder withExecutor(ExecutorService executor) {
		this.executor = executor;
		return this;
	}

	public DeepSRL build() throws IOException {
		List<String> command = new ArrayList<>();
		command.add(pythonExecutable.getAbsolutePath());
		command.add(deepSRLFile.getAbsolutePath());
		command.addAll(CommandOption.MODEL.withFile(modelFile).getCommand());
		command.addAll(CommandOption.PIDMODEL.withFile(pidmodelFile).getCommand());

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(deepSRLFile.getParentFile());
		ExecutorService executor = this.executor != null ? this.executor : Executors.newCachedThreadPool();
		return new DeepSRL(executor, errorStream, processBuilder.start());
	}

}
