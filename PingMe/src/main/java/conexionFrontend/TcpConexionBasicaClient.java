package conexionFrontend;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import entidades.Mensaje;
import entidades.ServerPeticion;
import entidades.ServerRespuesta;
import entidades.TipoComando;

public class TcpConexionBasicaClient {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ip = "10.249.20.41";
		int puerto = 5000;


		try {
			System.out.println("Conectadonos al servidor");
			Socket server = new Socket(ip, puerto);
			System.out.println("Conexion realizada con exito");

			// MANDANDO MENSAJES AL SERVIDOR
			Scanner sc = new Scanner(System.in);
			ServerPeticion sp = null;
			ObjectOutputStream output = new ObjectOutputStream(server.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(server.getInputStream());
			
			while (true) {
				System.out.println("INTRODUCE EL COMANDO");
				String mensaje = sc.nextLine();
				String comando = mensaje.split(" ")[0];
				mensaje = mensaje.substring(comando.length()+1, mensaje.length());
				
				switch (comando) {
				case "Crear":
					sp = new ServerPeticion(TipoComando.CREAR_USUARIO, mensaje);
					break;
				case "Eliminar":
					sp = new ServerPeticion(TipoComando.ELIMINAR_USUARIO, new Mensaje());
					break;
				case "Fin":
					sp = new ServerPeticion(TipoComando.FIN, new Mensaje());
					break;
				default:
					System.out.println("Comando inexistente");
					continue;
				}

				output.writeObject(sp);
				output.flush();

				ServerRespuesta mensajeServidor = (ServerRespuesta) input.readObject();
				System.out.println("Server -> " + mensajeServidor);

				if (mensajeServidor.getMensaje().equals("FIN")) {
					break;
				}
			}

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		System.out.println("Conexion cerrada con exito");
	}

}
