package deepSRL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class DeepSRLClientBuilder {

	private String host;
	private Integer port;

	public DeepSRLClientBuilder(String host, Integer port) {
		this.host = host;
		this.port = port;
	}

	public DeepSRL build() throws IOException {
		Socket socket = new Socket(host, port);
		OutputStream outputStream = socket.getOutputStream();
		InputStream inputStream = socket.getInputStream();
		return new DeepSRL(outputStream, inputStream) {
			@Override
			public synchronized void close() throws IOException {
				socket.close();
				super.close();
			}
		};
	}

}
