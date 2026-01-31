package backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import entidades.Mensaje;
import entidades.ServerPeticion;
import entidades.ServerRespuesta;
import entidades.TipoComando;
import entidades.Usuario;
import services.MensajeService;
import services.SalaService;
import services.UsuarioService;

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

					UsuarioService usuarioService = new UsuarioService();
					MensajeService mensajeService = new MensajeService();
					SalaService salaService = new SalaService();

					switch (peticion.getComando()) {
					case CREAR_USUARIO: {
						ServerRespuesta resp;
						try {
							Object c = peticion.getContenido();
							Usuario u = (c instanceof Usuario) ? (Usuario) c : new Usuario(String.valueOf(c));

							if (u.getEmail() == null || u.getEmail().isBlank()) {
								u.setEmail(u.getNombre().toLowerCase().replace(" ", ".") + "@example.com");
							}
							if (u.getContraseña() == null)
								u.setContraseña("changeme");
							if (u.getSexo() == null)
								u.setSexo("");
							if (u.getEdad() == 0)
								u.setEdad(21);
							if (u.getPregunta() == null)
								u.setPregunta("q");
							if (u.getRespuesta() == null)
								u.setRespuesta("r");

							Usuario creado = usuarioService.crearUsuario(u);
							resp = new ServerRespuesta(0, "Usuario creado", creado);
						} catch (Exception ex) {
							resp = new ServerRespuesta(2, "Error: " + ex.getMessage(), null);
						}
						respuesta = resp;
						break;

					}

					case LISTAR_USUARIOS:
						respuesta = new ServerRespuesta(0, "Lista usuarios", usuarioService.listarTodos());
						break;

					case ENVIAR_MENSAJE_CHAT: {
						try {
							Mensaje m = (Mensaje) peticion.getContenido();
							// mensajeService.enviar(m);
							respuesta = new ServerRespuesta(0, "Mensaje privado guardado", null);
						} catch (Exception ex) {
							respuesta = new ServerRespuesta(2, "Error: " + ex.getMessage(), null);
						}
						break;
					}

					/*
					 * case OBTENER_HISTORIAL: { // contenido: un DTO simple con ids A y B
					 * ConversacionDTO dto = (ConversacionDTO) peticion.getContenido(); respuesta =
					 * new ServerRespuesta(0, "Historial DM", mensajeService.historialDM(dto.a,
					 * dto.b)); break; }
					 */

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

		} catch (

		IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
