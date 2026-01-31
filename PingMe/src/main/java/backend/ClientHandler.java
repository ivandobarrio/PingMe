package backend;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import entidades.ConversacionDTO;
import entidades.Mensaje;
import entidades.ServerPeticion;
import entidades.ServerRespuesta;
import services.MensajeService;
import services.SalaService;
import services.UsuarioService;

public class ClientHandler implements Runnable {
	private Socket cliente;
	private final UsuarioService usuarioService = new UsuarioService();
	private final MensajeService mensajeService = new MensajeService();
	private final SalaService salaService = new SalaService();

	public ClientHandler(Socket cliente) {
		this.cliente = cliente;

	}

	@Override
	public void run() {
		try {
			// LEER MENSAJES CLIENTE
			ObjectOutputStream output = new ObjectOutputStream(cliente.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());

			output.flush();
			System.out.println("Handler listo para cliente: " + cliente.getInetAddress());

			while (true) {
				ServerPeticion peticion = (ServerPeticion) input.readObject();
				ServerRespuesta respuesta;
				try {
					switch (peticion.getComando()) {

					case LISTAR_USUARIOS:
						respuesta = new ServerRespuesta(0, "OK", usuarioService.listarTodos());
						break;

					case ENVIAR_MENSAJE_CHAT:
						mensajeService.enviar((Mensaje) peticion.getContenido());
						respuesta = new ServerRespuesta(0, "Mensaje guardado", null);
						break;

					case ENVIAR_MENSAJE_SALA:
						mensajeService.enviar((Mensaje) peticion.getContenido());
						respuesta = new ServerRespuesta(0, "Mensaje guardado", null);
						break;

					case OBTENER_HISTORIAL:
						ConversacionDTO dto = (ConversacionDTO) peticion.getContenido();
						if (dto.tipo.equals("dm"))
							respuesta = new ServerRespuesta(0, "Historial DM",
									mensajeService.historialDM(dto.a, dto.b));
						else
							respuesta = new ServerRespuesta(0, "Historial Sala", mensajeService.historialSala(dto.a));
						break;

					case FIN:
						respuesta = new ServerRespuesta(0, "FIN", null);
						output.writeObject(respuesta);
						output.flush();
						return;

					default:
						respuesta = new ServerRespuesta(1, "ERROR: comando not exist", null);
						break;
					}
				} catch (Exception e) {
					// TODO: handle exception
					respuesta = new ServerRespuesta(1, "ERROR: error generico", null);
					System.out.println("Error: " + e.getMessage());
				}

				output.reset();
				output.writeObject(respuesta);
				output.flush();

			}

		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Error con el cliente " + cliente.getInetAddress() + ": " + e.getMessage());
		}

		try {
			System.err.println("Desconectando al cliente " + cliente.getInetAddress());
			cliente.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
