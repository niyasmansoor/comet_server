package websocket.server;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import websocket.AbstractWebSocket;

public class WebSocket extends AbstractWebSocket {

	public WebSocket(Socket socket) throws IOException {
		this(socket, 0);
	}

	public WebSocket(Socket socket, Integer timeout) throws IOException {
		super(socket, timeout);
	}

	private String requestUri;

	public String getRequestUri() {
		return requestUri;
	}

	@Override
	protected void handshake(Object... args) throws IOException, NoSuchAlgorithmException {
		String line = readLine();
		String[] requestLine = line.split(" ");
		if (requestLine.length < 2)
			throw new IOException("Wrong Request-Line format: " + line);
		requestUri = requestLine[1];
		String host = null, origin = null, cookie = null;
		Boolean upgrade = false, connection = false;
		Long[] keys = new Long[2];

		line = readLine();
		while (line.equals(""))
			line = readLine();

		while (!(line.equals(""))) {
			String[] parts = line.split(": ", 2);
			if (parts.length != 2)
				throw new IOException("Wrong field format: " + line);
			String name = parts[0].toLowerCase();
			String value = parts[1].toLowerCase();

			if (name.equals("upgrade")) {
				if (!value.equals("websocket"))
					throw new IOException("Wrong value of upgrade field: " + line);
				upgrade = true;
			} else if (name.equals("connection")) {
				if (!value.equals("upgrade"))
					throw new IOException("Wrong value of connection field: " + line);
				connection = true;
			} else if (name.equals("host")) {
				host = value;
			} else if (name.equals("origin")) {
				origin = value;
			} else if ((name.equals("sec-websocket-key1")) || (name.equals("sec-websocket-key2"))) {
				Integer spaces = new Integer(0);
				Long number = new Long(0);
				for (Character c : value.toCharArray()) {
					if (c.equals(' '))
						++spaces;
					if (Character.isDigit(c)) {
						number *= 10;
						number += Character.digit(c, 10);
					}
				}
				number /= spaces;
				if (name.endsWith("key1"))
					keys[0] = number;
				else
					keys[1] = number;
			} else if (name.equals("cookie")) {
				cookie = value;
			} else {
				throw new IOException("Unexpected header field: " + line);
			}
			line = readLine();
		}
		if ((host == null) || (!upgrade) || (!connection) || (origin == null) || (keys[0] == null) || (keys[1] == null))
			throw new IOException("Missing handshake arguments");
		byte[] token = readBytes(8);

		writeLine("HTTP/1.1 101 WebSocket Protocol Handshake");
		writeLine("Upgrade: WebSocket");
		writeLine("Connection: Upgrade");
		writeLine("Sec-WebSocket-Origin: " + origin);
		writeLine("Sec-WebSocket-Location: ws://" + host + requestUri);
		if (cookie != null)
			writeLine("cookie: " + cookie);
		writeLine("");
		out.write(makeResponseToken(keys[0].intValue(), keys[1].intValue(), token));
		out.flush();
	}

}
