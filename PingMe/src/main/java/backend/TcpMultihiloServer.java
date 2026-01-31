package backend;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hibernate.HibernateUtil;

public class TcpMultihiloServer {

	public static void main(String[] args) {
		int puerto = 5000;
		ExecutorService pool = Executors.newCachedThreadPool();

		try {
			System.out.println("Iniciando el servidor...");
			ServerSocket server = new ServerSocket(puerto);
			HibernateUtil.getSessionFactory();

			while (true) {
				System.out.println("Esperando la conexion del cliente");
				Socket cliente = server.accept();
				System.out.println("Cliente conectado con exito");
				System.out.println("IP cliente: " + cliente.getInetAddress().getHostAddress());

				pool.execute(new ClientHandler(cliente));
			}
		} catch (Exception e) {
			System.err.println("Error en el servidor: " + e.getMessage());
		} finally {
			pool.shutdown();
		}
	}
}
