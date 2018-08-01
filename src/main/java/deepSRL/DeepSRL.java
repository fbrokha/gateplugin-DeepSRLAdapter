package deepSRL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import deepSRL.mapping.Document;
import deepSRL.mapping.ResultParser;
import gate.creole.ResourceInstantiationException;

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

	protected DeepSRL(ExecutorService executor, OutputStream errorStream, ProcessBuilder processBuilder) {
		this.executor = executor;
		this.outErrorStream = errorStream;
		this.processBuilder = processBuilder;
	}

	public void init() throws Exception, ResourceInstantiationException, IOException {
		process = processBuilder.start();
		outputStream = process.getOutputStream();
		inErrorStream = process.getErrorStream();
		inputStream = process.getInputStream();

		ResultParser.checkInitialization(inErrorStream, inputStream);

		executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Util.copy(inErrorStream, outErrorStream);
				return null;
			}
		});
	}

	public void execute(final Document document) throws IOException, InterruptedException, ExecutionException {
		try {
			executeProcess(document);
		} catch (Exception e) {
			throw e;
		}
	}

	private void executeProcess(final Document document) throws IOException, InterruptedException, ExecutionException {
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

			ResultParser.extractSRL(document, inputStream);

		} catch (Exception e) {
			throw e;
		}
	}

	public void shutdownService() {
		try {
			outErrorStream.close();
			outputStream.close();
			inputStream.close();
			inErrorStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		process.destroy();
		executor.shutdown();
	}
}
