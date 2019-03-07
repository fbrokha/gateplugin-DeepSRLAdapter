package deepSRL;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeepSRLProcessBuilder {

	private static final String INIT_SUCCESS = "initsuccessful";
	private static final String UNBUFFERED_OUTPUT = "-u";

	private Map<String, String> env;
	private File pythonExecutable;
	private OutputStream outStream = System.out;
	private OutputStream errorStream = System.err;

	private File deepSRLFile;
	private File modelFile;
	private File pidmodelFile;

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

	public DeepSRLProcessBuilder(File pythonExecutable, File deepSRLFile, File modelFile, File pidmodelFile) {
		this.pythonExecutable = pythonExecutable;
		this.deepSRLFile = deepSRLFile;
		this.modelFile = modelFile;
		this.pidmodelFile = pidmodelFile;
	}

	public DeepSRLProcessBuilder withOutputStream(OutputStream outStream) {
		this.outStream = outStream;
		return this;
	}

	public DeepSRLProcessBuilder withErrorStream(OutputStream errorStream) {
		this.errorStream = errorStream;
		return this;
	}

	public DeepSRLProcessBuilder withEnvironment(Map<String, String> env) {
		this.env = env;
		return this;
	}

	public DeepSRL build() throws IOException {
		List<String> command = new ArrayList<>();
		command.add(pythonExecutable.getAbsolutePath());
		command.add(UNBUFFERED_OUTPUT);
		command.add(deepSRLFile.getAbsolutePath());
		command.addAll(CommandOption.MODEL.withFile(modelFile).getCommand());
		command.addAll(CommandOption.PIDMODEL.withFile(pidmodelFile).getCommand());

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(deepSRLFile.getParentFile());
		if (this.env != null) {
			processBuilder.environment().putAll(this.env);
		}
		Process process = processBuilder.start();

		OutputStream processOutputStream = process.getOutputStream();
		InputStream processInputStream = process.getInputStream();
		InputStream processErrorStream = process.getErrorStream();

		ExecutorService executor = Executors.newCachedThreadPool();

		if (errorStream != null) {
			executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Util.copy(processErrorStream, errorStream);
					return null;
				}
			});
		} else {
			executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					while (processErrorStream.read() >= 0)
						;
					return null;
				}
			});
		}

		checkInitialization(processInputStream, outStream != null ? new PrintStream(outStream) : null);

		return new DeepSRL(processOutputStream, processInputStream) {
			@Override
			public synchronized void close() throws IOException {
				process.destroy();
				executor.shutdown();
				super.close();
			}
		};
	}

	public static void checkInitialization(InputStream processInputStream, PrintStream outStream) throws IOException {
		BufferedReader inReader = new BufferedReader(new InputStreamReader(processInputStream));
		String inLine;
		while ((inLine = inReader.readLine()) != null) {
			if (inLine.matches(INIT_SUCCESS)) {
				break;
			} else {
				if (outStream != null) {
					outStream.println(inLine);
				}
			}
		}
	}

}
