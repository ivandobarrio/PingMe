package backend;

import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

	public static void main(String[] args) throws Exception {
		int puerto = 5000;
		System.out.println("Servidor escuchando en puerto " + puerto);
		try (ServerSocket server = new ServerSocket(puerto)) {
			while (true) {
				Socket cliente = server.accept();
				System.out.println("Cliente conectado: " + cliente.getInetAddress());
				ClientHandler handler = new ClientHandler(cliente);
				new Thread(handler, "client-" + cliente.getPort()).start();
			}
		}
	}
}
