package backend;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHub {

	private static final ConcurrentHashMap<String, ClientHandler> online = new ConcurrentHashMap<>();

	public static void register(String username, ClientHandler handler) {
		online.put(username, handler);
	}

	public static void unregister(String username) {
		if (username != null)
			online.remove(username);
	}

	public static ClientHandler get(String username) {
		return online.get(username);
	}

	public static boolean isOnline(String username) {
		return online.containsKey(username);
	}

	public static void sendToUser(String username, Object payload) {
		ClientHandler ch = online.get(username);
		if (ch != null)
			ch.sendAsync(payload);
	}
}