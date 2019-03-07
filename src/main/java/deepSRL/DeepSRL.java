package deepSRL;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import deepSRL.mapping.Document;
import deepSRL.mapping.ResultParser;

public class DeepSRL implements Closeable {

	private static final String TEXT_END = "textsend\n";

	private boolean closed = false;

	private OutputStream outputStream;
	private InputStream inputStream;

	protected DeepSRL(OutputStream outputStream, InputStream inputStream) {
		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}

	protected void checkAlive() {
		if (closed) {
			throw new RuntimeException("deep_srl already closed");
		}
	}

	public synchronized void execute(final Document document) throws IOException {
		checkAlive();
		ByteArrayInputStream in = new ByteArrayInputStream(document.getDeepSRLText().concat(TEXT_END).getBytes());
		Util.copy(in, outputStream);
		in.close();
		outputStream.flush();
		checkAlive();

		ResultParser.extractSRL(document, inputStream);

		checkAlive();
	}

	@Override
	public void close() throws IOException {
		outputStream.close();
		inputStream.close();
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}

}
