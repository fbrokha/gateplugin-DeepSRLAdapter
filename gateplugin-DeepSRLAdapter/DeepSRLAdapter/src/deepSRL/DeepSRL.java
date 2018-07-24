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
import gate.deepSRL.DeepSRLAdapter;
import deepSRL.DeepSRL;
import deepSRL.Util;

public class DeepSRL {
	
	private static final String USERTOKEN = "1\n";
	private static final String DEEPSRLTOKEN = "0\n";
	
	private ExecutorService executor;
	private ProcessBuilder processBuilder;
	private InputStream inputStream;
	private OutputStream outputStream;
	private ByteArrayInputStream in;
	private OutputStream outErrorStream;
	private InputStream inErrorStream;
	private Process process;
	
	private static final String TEXT_END = "textsend\n";

	protected DeepSRL(ExecutorService executor, OutputStream errorStream,
			ProcessBuilder processBuilder) {
		this.executor = executor;
		this.outErrorStream = errorStream;
		this.processBuilder = processBuilder;
	}
	
	public void init () throws Exception {
		process = processBuilder.start();
		outputStream = process.getOutputStream();
		inErrorStream = process.getErrorStream();
		inputStream = process.getInputStream();
	}
	
	public void execute(final Document document) throws IOException, InterruptedException, ExecutionException {
			executeProcess(document);
	}
	
	private void executeProcess(final Document document) throws IOException, InterruptedException, ExecutionException {
		try {
			
			if(DeepSRLAdapter.getUserTokens()) {
				executor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						in = new ByteArrayInputStream(USERTOKEN.concat(document.getDeepSRLText().concat(TEXT_END)).getBytes());
						Util.copy(in, outputStream);
						in.close();
						return null;
					}
				});
			}
			else {
				executor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						in = new ByteArrayInputStream(DEEPSRLTOKEN.concat(document.getDeepSRLText().concat(TEXT_END)).getBytes());
						Util.copy(in, outputStream);
						in.close();
						return null;
					}
				});
			}

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
			
		} catch (IOException e) {
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
		
		try {
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		executor.shutdownNow();
//		process.destroy();
	}


}
