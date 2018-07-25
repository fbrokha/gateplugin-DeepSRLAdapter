package main.java.deepSRL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import main.java.deepSRL.mapping.Document;
import main.java.deepSRL.mapping.ResultParser;

public class DeepSRL {

	private static final String TEXT_END = "textsend\n";

	private ExecutorService executor;
	private ProcessBuilder processBuilder;
	private InputStream inputStream;
	private OutputStream outputStream;
	private ByteArrayInputStream in;
	private OutputStream outErrorStream;
	private InputStream inErrorStream;
	private Process process;

	private Boolean cancelled = false;

	protected DeepSRL(ExecutorService executor, OutputStream errorStream, ProcessBuilder processBuilder) {
		this.executor = executor;
		this.outErrorStream = errorStream;
		this.processBuilder = processBuilder;
	}

	public void init() throws Exception {
		process = processBuilder.start();
		outputStream = process.getOutputStream();
		inErrorStream = process.getErrorStream();
		inputStream = process.getInputStream();
	}

	public void execute(final Document document) throws IOException, InterruptedException, ExecutionException {
		if (cancelled) {
			inputStream = process.getInputStream();
			outputStream = process.getOutputStream();
		}
		try {
			executeProcess(document);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void executeProcess(final Document document)
			throws IOException, InterruptedException, ExecutionException, Throwable {
		try {
			executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					in = new ByteArrayInputStream(document.getDeepSRLText().concat(TEXT_END).getBytes());
					Util.copy(in, outputStream);
					in.close();
					return null;
				}
			});

			executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Util.copy(inErrorStream, DeepSRL.this.outErrorStream);
					outErrorStream.close();
					inErrorStream.close();
					return null;
				}
			});

			ResultParser.extractSRL(document, inputStream);

		} catch (Throwable e) {
			cancel();
			throw e;
		}
	}

	public ExecutorService getExec() {
		return executor;
	}

	public Process getProcess() {
		return process;
	}

	public void shutdownService() {
		process.destroy();
		executor.shutdown();
	}

	private void cancel() {
		cancelled = true;
		try {
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// executor.shutdownNow();
		// process.destroy();
	}

}
