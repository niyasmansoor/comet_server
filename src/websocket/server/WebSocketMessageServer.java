package websocket.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WebSocketMessageServer extends Thread {

	public static ConcurrentHashMap<Integer, WebSocket> clients = new ConcurrentHashMap<Integer, WebSocket>();

	private static final List<String> USERS = new ArrayList<>();

	@Override
	public void run() {

		AtomicInteger msgCntr = new AtomicInteger(1);
		final int MAX_CLIENT_SIZE = 6;
		final int MIN_CLIENT_SIZE = 1;
		while (true) {
			if (clients.size() != 0) {
				Random rand = new Random();
				int randomNum = 0;
				try {
					randomNum = rand.nextInt((MAX_CLIENT_SIZE - MIN_CLIENT_SIZE) + 1);
					WebSocket client = clients.get(randomNum);
					if (!(null == client)) {
						if (!client.isClosed()) {
							String message = "New message ( Message # " + msgCntr + " ) for : " + USERS.get(randomNum);
							client.sendMessage(message);
							msgCntr.incrementAndGet();
						} else
							clients.remove(clients.get(randomNum));
					}
				} catch (Exception e) {
					clients.remove(clients.get(randomNum));
				}
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		final int PORT = 9000;

		USERS.add("guest");
		USERS.add("user1");
		USERS.add("user2");
		USERS.add("user3");
		USERS.add("user4");
		USERS.add("user5");

		try {
			WebServerSocket socket = new WebServerSocket(PORT);
			boolean firstClient = true;
			System.out.println("Streaming server ready. Listen on: " + PORT);
			while (true) {
				
				WebSocket ws = socket.accept();
				String user = ws.getMessage().trim().toLowerCase();
				System.out.println("User :" + user);
				if (USERS.contains(user)) {
					clients.put(USERS.indexOf(user), ws);
					if (firstClient) {
						new WebSocketMessageServer().start();
						firstClient = false;
					}
				} else {
					System.out.println("User not authenticated !!!");
					ws.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
