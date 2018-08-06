package deepSRL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import deepSRL.mapping.Document;
import deepSRL.mapping.ResultParser;

public class DeepSRL {

	private static final String TEXT_END = "textsend\n";

	private Process process;
	private ExecutorService executor;

	private OutputStream outputStream;
	private InputStream inputStream;
	private InputStream errorStream;

	protected DeepSRL(ExecutorService executor, OutputStream outErrorStream, Process process) throws IOException {
		this.executor = executor;
		this.outputStream = process.getOutputStream();
		this.inputStream = process.getInputStream();
		this.errorStream = process.getErrorStream();

		ResultParser.checkInitialization(inputStream, errorStream, new PrintStream(outErrorStream));

		executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Util.copy(errorStream, outErrorStream);
				return null;
			}
		});
	}

	public void execute(final Document document) throws IOException, InterruptedException, ExecutionException {
		executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ByteArrayInputStream in = new ByteArrayInputStream(
						document.getDeepSRLText().concat(TEXT_END).getBytes());
				Util.copy(in, outputStream);
				in.close();
				return null;
			}
		});

		ResultParser.extractSRL(document, inputStream);
	}

	public void shutdownService() throws IOException {
		outputStream.close();
		inputStream.close();
		errorStream.close();
		process.destroy();
		executor.shutdown();
	}
}
