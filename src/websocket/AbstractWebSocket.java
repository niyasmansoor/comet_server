package websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public abstract class AbstractWebSocket {

	protected Socket socket;
	protected InputStream in;
	protected OutputStream out;
	private Object[] args;

	private class HandshakeRunner implements Runnable {

		private Exception exception = null;

		public Exception getException() {
			return exception;
		}

		@Override
		public void run() {
			try {
				handshake(args);
			} catch (Exception e) {
				System.out.println("exception run handshake " + e);
				exception = e;
			}
		}

	}

	public AbstractWebSocket(Socket socket, Integer timeout, Object... args) throws IOException {
		this.socket = socket;
		in = socket.getInputStream();
		out = socket.getOutputStream();
		this.args = args;
		HandshakeRunner taskBody = new HandshakeRunner();

		FutureTask<Object> task = new FutureTask<Object>(taskBody, null);
		try {
			(new Thread(task)).start();
			if (timeout > 0) {
				task.get(timeout, TimeUnit.MILLISECONDS);
			} else {
				task.get();
			}
		} catch (Exception e) {
			socket.close();
			throw new IOException("Handshake failed", e);
		}
		if (taskBody.getException() != null) {
			socket.close();
			throw new IOException("Handshake failed", taskBody.getException());
		}
	}

	protected String byteCollectionToString(Collection<Byte> collection) {
		byte[] byteArray = new byte[collection.size()];
		Integer i = 0;
		for (Iterator<Byte> iterator = collection.iterator(); iterator.hasNext();) {
			byteArray[i++] = iterator.next();
		}
		return new String(byteArray, Charset.forName("UTF-8"));
	}

	public void close() throws IOException {
		socket.close();
	}

	public String getMessage() throws IOException, SocketTimeoutException {

		Vector<Byte> message = new Vector<Byte>();
		synchronized (in) {
			try {
				Integer current = in.read();
				if (!current.equals(0))
					throw new IOException("Wrong format");
				// System.out.println("read byte: " + current);
				current = in.read();
				while (!current.equals(0xFF)) {
					if (current.equals(-1))
						throw new IOException("End of stream");
					message.add(current.byteValue());
					current = in.read();
				}
			} catch (SocketTimeoutException e) {
				throw new SocketTimeoutException();
			}
		}
		return byteCollectionToString(message);
	}

	protected abstract void handshake(Object... args) throws IOException, NoSuchAlgorithmException;

	public boolean isClosed() {
		return socket.isClosed();
	}

	protected byte[] readBytes(Integer count) throws IOException {
		if (count <= 0)
			return new byte[0];
		byte[] bytes = new byte[count];
		for (int i = 0; i < count; ++i) {
			Integer current = in.read();
			if (current.equals(-1))
				throw new IOException("End of stream");
			bytes[i] = current.byteValue();
		}
		return bytes;
	}

	protected String readLine() throws IOException {
		Vector<Byte> line = new Vector<Byte>();
		Integer last = in.read();
		if (last.equals(-1))
			throw new IOException("End of stream");
		Integer current = in.read();
		while (!((last.equals(0x0D)) && (current.equals(0x0A)))) {
			if (current.equals(-1))
				throw new IOException("End of stream");
			line.add(last.byteValue());
			last = current;
			current = in.read();
		}
		return byteCollectionToString(line);
	}

	public void sendMessage(String message) throws IOException {
		synchronized (out) {
			out.write(0x00);
			out.write(message.getBytes(Charset.forName("UTF-8")));
			out.write(0xFF);
			out.flush();
		}
	}

	protected void writeLine(String line) throws IOException {
		out.write(line.getBytes(Charset.forName("UTF-8")));
		out.write(0x0D);
		out.write(0x0A);
	}

	protected byte[] makeResponseToken(int key1, int key2, byte[] token) throws NoSuchAlgorithmException {
		MessageDigest md5digest = MessageDigest.getInstance("MD5");
		for (Integer i = 0; i < 2; ++i) {
			byte[] asByte = new byte[4];
			int key = (i == 0) ? key1 : key2;
			asByte[0] = (byte) (key >> 24);
			asByte[1] = (byte) ((key << 8) >> 24);
			asByte[2] = (byte) ((key << 16) >> 24);
			asByte[3] = (byte) ((key << 24) >> 24);
			md5digest.update(asByte);
		}
		md5digest.update(token);
		return md5digest.digest();
	}

}
