package deepSRL;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import deepSRL.mapping.Document;
import deepSRL.mapping.Parser;

public class DeepSRL implements Closeable {

	private boolean closed = false;
	private OutputStream outputStream;
	private InputStream inputStream;

	protected DeepSRL(OutputStream outputStream, InputStream inputStream) {
		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}

	protected void checkAlive() {
		if (isClosed()) {
			throw new RuntimeException("deep_srl already closed");
		}
	}

	public synchronized void execute(final Document document) throws IOException {
		checkAlive();
		if (Parser.writeDocument(outputStream, document)) {
			checkAlive();
			Parser.extractSRL(inputStream, document);
		}
		checkAlive();
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			outputStream.close();
			inputStream.close();
			closed = true;
		}
	}

	public boolean isClosed() {
		return closed;
	}

}
