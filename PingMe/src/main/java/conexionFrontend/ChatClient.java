package conexionFrontend;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

import entidades.CredencialesDTO;
import entidades.ServerPeticion;
import entidades.ServerRespuesta;
import entidades.TipoComando;
import javafx.application.Platform;

public class ChatClient implements AutoCloseable {
	private final String host;
	private final int puerto;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Thread reader;
	private Consumer<ServerRespuesta> onEvent; // callback para eventos del server

	public ChatClient(String host, int puerto) {
		this.host = host;
		this.puerto = puerto;
	}

	public void setOnEvent(Consumer<ServerRespuesta> onEvent) {
		this.onEvent = onEvent;
	}

	public boolean conectarYLoguear(String usuarioOEmail, String pass) throws Exception {
		socket = new Socket(host, puerto);
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());

		// Enviar login
		ServerPeticion login = new ServerPeticion(TipoComando.INICIAR_SESION, new CredencialesDTO(usuarioOEmail, pass));
		send(login);

		// Esperar respuesta
		ServerRespuesta resp = (ServerRespuesta) in.readObject();
		if (resp.getCodigo() != 0 || !"LOGIN_OK".equals(resp.getMensaje())) {
			return false;
		}

		// Lanzar hilo lector
		reader = new Thread(this::leerEventos, "reader");
		reader.setDaemon(true);
		reader.start();
		return true;
	}

	private void leerEventos() {
		try {
			while (!socket.isClosed()) {
				Object obj = in.readObject();
				if (obj instanceof ServerRespuesta resp) {
					if (onEvent != null) {
						// Asegura UI-safe
						Platform.runLater(() -> onEvent.accept(resp));
					}
				}
			}
		} catch (Exception ignored) {
		}
	}

	public synchronized void send(ServerPeticion peticion) throws Exception {
		out.writeObject(peticion);
		out.flush();
	}

	@Override
	public void close() {
		try {
			if (out != null)
				out.writeObject(new ServerPeticion(TipoComando.FIN, null));
		} catch (Exception ignored) {
		}
		try {
			if (in != null)
				in.close();
		} catch (Exception ignored) {
		}
		try {
			if (out != null)
				out.close();
		} catch (Exception ignored) {
		}
		try {
			if (socket != null)
				socket.close();
		} catch (Exception ignored) {
		}
		if (reader != null)
			reader.interrupt();
	}
}
