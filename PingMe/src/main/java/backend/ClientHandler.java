package backend;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import entidades.ConversacionDTO;
import entidades.CredencialesDTO;
import entidades.Mensaje;
import entidades.ServerPeticion;
import entidades.ServerRespuesta;
import entidades.Usuario;
import services.MensajeService;
import services.SalaService;
import services.UsuarioService;

public class ClientHandler implements Runnable {
	private final Socket cliente;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private final UsuarioService usuarioService = new UsuarioService();
	private final MensajeService mensajeService = new MensajeService();
	private String username;

	public ClientHandler(Socket cliente) {
		this.cliente = cliente;
	}

	@Override
	public void run() {
		try {
			// LEER MENSAJES CLIENTE
			output = new ObjectOutputStream(cliente.getOutputStream());
			output.flush();
			input = new ObjectInputStream(cliente.getInputStream());

			System.out.println("Handler listo para cliente: " + cliente.getInetAddress());

			while (!cliente.isClosed()) {
				Object obj = input.readObject();
				if (!(obj instanceof ServerPeticion))
					continue;

				ServerPeticion req = (ServerPeticion) obj;
				switch (req.getComando()) {
				case INICIAR_SESION: {
					// contenido: DTO simple con usuario/email y password, o un Map
					CredencialesDTO cred = (CredencialesDTO) req.getContenido();
					Usuario u = usuarioService.validarLogin(cred.usuarioOEmail, cred.contrasena);
					if (u != null) {
						username = u.getNombre();
						ConnectionHub.register(username, this);
						sendSync(new ServerRespuesta(0, "LOGIN_OK", u));
					} else {
						sendSync(new ServerRespuesta(2, "LOGIN_FAIL", null));
					}
					break;
				}
				case ENVIAR_MENSAJE_CHAT: {
					Mensaje m = (Mensaje) req.getContenido();
					// Persistir
					mensajeService.enviar(m);
					// ACK al emisor
					sendSync(new ServerRespuesta(0, "MSG_SENT", null));
					// Push al receptor (si está online)
					ServerRespuesta push = new ServerRespuesta(0, "EVENT_RECIBIR_MENSAJE", m);
					ConnectionHub.sendToUser(m.getIdReceptor(), push);
					break;
				}
				case OBTENER_HISTORIAL: {

					ConversacionDTO dto = (ConversacionDTO) req.getContenido();
					if ("dm".equalsIgnoreCase(dto.tipo)) {
						var historial = mensajeService.historialDM(dto.a, dto.b);
						sendSync(new ServerRespuesta(0, "HISTORIAL_DM", historial));
					} else if ("sala".equalsIgnoreCase(dto.tipo)) {
						var historialSala = mensajeService.historialSala(dto.b); // receptor = nombreSala
						sendSync(new ServerRespuesta(0, "HISTORIAL_SALA", historialSala));
					} else {
						sendSync(new ServerRespuesta(2, "TIPO_CONVERSACION_DESCONOCIDO", null));
					}
					break;

				}
				case FIN: {
					sendSync(new ServerRespuesta(0, "FIN", null));
					close();
					return;
				}
				default:
					sendSync(new ServerRespuesta(1, "CMD_NOT_SUPPORTED", null));
				}
			}

		} catch (Exception e) {
			// client dropped
		} finally {
			ConnectionHub.unregister(username);
			close();
		}
	}

	public synchronized void sendAsync(Object payload) {
		try {
			output.writeObject(payload);
			output.flush();
		} catch (IOException ignored) {
		}
	}

	private void sendSync(Object payload) throws IOException {
		output.writeObject(payload);
		output.flush();
	}

	private void close() {
		try {
			if (input != null)
				input.close();
		} catch (Exception ignored) {
		}
		try {
			if (output != null)
				output.close();
		} catch (Exception ignored) {
		}
		try {
			cliente.close();
		} catch (Exception ignored) {
		}
	}
}
