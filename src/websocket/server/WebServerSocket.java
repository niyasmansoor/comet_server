package websocket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class WebServerSocket {

	private ServerSocket serverSocket;
	private Integer timeout = 0;

	public WebServerSocket() throws IOException {
		this(0);
	}

	public WebServerSocket(Integer port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	public void setSoTimeout(Integer timeout) throws SocketException {
		serverSocket.setSoTimeout(timeout);
		this.timeout = timeout;
	}

	public Integer getLocalPort() {
		return serverSocket.getLocalPort();
	}

	public Boolean isClosed() {
		return serverSocket.isClosed();
	}

	public void close() throws IOException {
		serverSocket.close();
	}

	public WebSocket accept() throws IOException {
		try {
			Socket socket = serverSocket.accept();
			return new WebSocket(socket, timeout);
		} catch (Exception e) {
			throw new IOException("accept");
		}
	}

}
