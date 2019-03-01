package deepSRL;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import deepSRL.mapping.Document;
import deepSRL.mapping.ResultParser;

public class DeepSRL implements Closeable {

	private static final String TEXT_END = "textsend\n";
	private Boolean closed = false;

	private Process process;
	private OutputStream processOutputStream;
	private InputStream processInputStream;
	private InputStream processErrorStream;

	private ExecutorService executor = Executors.newCachedThreadPool();

	protected DeepSRL(ProcessBuilder processBuilder, OutputStream outStream, OutputStream errorStream)
			throws IOException {
		this.process = processBuilder.start();
		this.processOutputStream = process.getOutputStream();
		this.processInputStream = process.getInputStream();
		this.processErrorStream = process.getErrorStream();

		checkAlive();

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

		ResultParser.checkInitialization(processInputStream, processErrorStream,
				outStream != null ? new PrintStream(outStream) : null);

		checkAlive();
	}

	private void checkAlive() {
		if (closed) {
			throw new RuntimeException("deep_srl already closed");
		}
		if (!process.isAlive()) {
			throw new RuntimeException("deep_srl script closed unexpectly (exit code " + process.exitValue() + ")");
		}
	}

	public synchronized void execute(final Document document)
			throws IOException, InterruptedException, ExecutionException {
		checkAlive();
		executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ByteArrayInputStream in = new ByteArrayInputStream(
						document.getDeepSRLText().concat(TEXT_END).getBytes());
				Util.copy(in, processOutputStream);
				in.close();
				return null;
			}
		});

		ResultParser.extractSRL(document, processInputStream);
	}

	@Override
	public synchronized void close() throws IOException {
		checkAlive();
		processOutputStream.close();
		processInputStream.close();
		processErrorStream.close();
		process.destroy();
		executor.shutdown();
		closed = true;
	}
}
