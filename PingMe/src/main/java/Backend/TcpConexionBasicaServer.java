package Backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import entidades.ServerPeticion;
import entidades.ServerRespuesta;
import entidades.TipoComando;
import entidades.Usuario;

public class TcpConexionBasicaServer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int puerto = 5000;

		try {
			System.out.println("Iniciando el servidor...");
			ServerSocket server = new ServerSocket(puerto);

			System.out.println("Esperando la conexion del cliente");
			Socket cliente = server.accept();
			
			System.out.println("Cliente conectado con exito");
			System.out.println("IP cliente: " + cliente.getInetAddress().getHostAddress());

			// LEER MENSAJES CLIENTE
			ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());
			ObjectOutputStream output = new ObjectOutputStream(cliente.getOutputStream());

			while (true) {
				ServerRespuesta respuesta = null;
				try {
					ServerPeticion peticion = (ServerPeticion) input.readObject();
					System.out.println("Mensaje -> " + peticion);

					switch (peticion.getComando()) {
					case CREAR_USUARIO:
						respuesta = UserManager.crearUsuario(peticion);
						break;
					case FIN:
						respuesta = new ServerRespuesta(0, "FIN", null);
						break;
					default:
						respuesta = new ServerRespuesta(1, "ERROR: comando not exist", null);
						break;
					}
					output.writeObject(respuesta);
					output.flush();
					
					if (peticion.getComando().equals(TipoComando.FIN)) {
						break;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					respuesta = new ServerRespuesta(1, "ERROR: error generico", null);
					output.writeObject(respuesta);
					output.flush();
				}
			}
			System.out.println("Cerrando conexion");
			cliente.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
