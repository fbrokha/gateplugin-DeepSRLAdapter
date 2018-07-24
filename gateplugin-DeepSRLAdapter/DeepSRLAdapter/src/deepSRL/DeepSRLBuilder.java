package deepSRL;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeepSRLBuilder {
	
	private ExecutorService executor;
	private File DeepSRLFile;
	private File modelFile;
	private File pidmodelFile;
	private List<CommandOption> commandOptions = new ArrayList<>();
	private OutputStream errorStream = System.err;
	private String PYTHON_EXEC_PATH = "C:\\ENVpy2\\Scripts\\python.exe";
	
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
	
	public DeepSRLBuilder(File DeepSRLFile, File modelFile, File pidmodelFile) {
		this.DeepSRLFile = DeepSRLFile;
		this.modelFile = modelFile;
		this.pidmodelFile = pidmodelFile;
	}
	
	public DeepSRL build() {
		List<String> command = new ArrayList<>();
		command.add(PYTHON_EXEC_PATH);
		command.add(DeepSRLFile.getAbsolutePath());
		commandOptions.add(CommandOption.MODEL.withFile(modelFile));
		commandOptions.add(CommandOption.PIDMODEL.withFile(pidmodelFile));

		for (CommandOption commandOption : commandOptions) {
			command.addAll(commandOption.getCommandString());
		}
		
		

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(DeepSRLFile.getParentFile());
		ExecutorService executor = this.executor != null ? this.executor : Executors.newCachedThreadPool();
		return new DeepSRL(executor, errorStream, processBuilder);
	}
	
//	public DeepSRLBuilder modelPath(File modelFile) {
//		commandOptions.add(CommandOption.MODEL.withFile(modelFile));
//		return this;
//	}
//	
//	public DeepSRLBuilder pidmodelPath(File pidmodelFile) {
//		commandOptions.add(CommandOption.PIDMODEL.withFile(pidmodelFile));
//		return this;
//	}
//	
//	public DeepSRLBuilder inputDataPath(File dataFile) {
//		commandOptions.add(CommandOption.INPUTDATA.withFile(dataFile));
//		return this;
//	}

}
