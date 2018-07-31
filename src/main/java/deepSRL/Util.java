package deepSRL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {

	public static void copy(InputStream in, OutputStream out) throws IOException {
		int size = 0;
		byte[] buffer = new byte[1024];
		while ((size = in.read(buffer)) != -1)
			out.write(buffer, 0, size);
		out.flush();
	}

}