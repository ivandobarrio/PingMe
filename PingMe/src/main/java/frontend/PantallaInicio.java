package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

// Importaciones para JasperReports
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import conexionFrontend.ChatClient;
import entidades.ConversacionDTO;
import entidades.Mensaje;
import entidades.ServerPeticion;
import entidades.ServerRespuesta;
import entidades.TipoComando;

public class PantallaInicio {

	@FXML
	private Label usuarioLabel;
	@FXML
	private ListView<String> chatsList;
	@FXML
	private ListView<String> salasList;
	@FXML
	private Label tituloConversacion;
	@FXML
	private VBox mensajesBox;
	@FXML
	private TextField mensajeField;
	@FXML
	private Label estadoLabel;
	@FXML
	private Button logoutBtn;

	private Map<String, Object> session;
	private final Map<String, String> repositorioSalasPrivadas = new HashMap<>();
	private final Map<String, Map<String, String>> datosHistoricos = new HashMap<>();
	private ChatClient chat;

	private final Map<String, ObservableList<Mensaje>> conversaciones = new HashMap<>();
	private String conversacionActiva = null;

	@FXML
	public void initialize() {
		String salaGral = "Sala General [Público]";
		salasList.setItems(FXCollections.observableArrayList(salaGral));
		registrarHistoria(salaGral, "Sistema", "Sala abierta para todos los usuarios.");

		salasList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				chatsList.getSelectionModel().clearSelection();
				abrirConversacion(newVal, "Sala");
			}
		});
	}

	// ==========================================
	// LÓGICA DE JASPER REPORTS
	// ==========================================

	@FXML
	private void onGenerarInforme(ActionEvent event) {
		try {
			List<UsuarioReporte> lista = new ArrayList<>();
			lista.add(new UsuarioReporte("Soporte_Tecnico", "soporte@pingme.com", 50, 10, 2));
			lista.add(new UsuarioReporte(usuarioLabel.getText(), usuarioLabel.getText().toLowerCase() + "@gmail.com",
					chatsList.getItems().size(), salasList.getItems().size(), 0));

			net.sf.jasperreports.engine.design.JasperDesign jd = new net.sf.jasperreports.engine.design.JasperDesign();
			jd.setName("InformeCompleto");
			jd.setPageWidth(595);
			jd.setColumnWidth(555);

			// --- DEFINIR CAMPOS (Igual que en tu clase UsuarioReporte) ---
			String[] campos = { "nombreUsuario", "emailUsuario", "numChats", "numSalas" };
			for (String nombreCampo : campos) {
				net.sf.jasperreports.engine.design.JRDesignField field = new net.sf.jasperreports.engine.design.JRDesignField();
				field.setName(nombreCampo);
				field.setValueClass(nombreCampo.contains("num") ? Integer.class : String.class);
				jd.addField(field);
			}

			// --- TÍTULO ---
			net.sf.jasperreports.engine.design.JRDesignBand titleBand = new net.sf.jasperreports.engine.design.JRDesignBand();
			titleBand.setHeight(50);
			net.sf.jasperreports.engine.design.JRDesignStaticText titleText = new net.sf.jasperreports.engine.design.JRDesignStaticText();
			titleText.setText("PINGME - REPORTE DE USUARIOS Y CORREOS");
			titleText.setX(0);
			titleText.setY(10);
			titleText.setWidth(555);
			titleText.setHeight(30);
			titleText.setFontSize(16f);
			titleBand.addElement(titleText);
			jd.setTitle(titleBand);

			// --- DETALLE (FILAS) ---
			net.sf.jasperreports.engine.design.JRDesignBand detailBand = new net.sf.jasperreports.engine.design.JRDesignBand();
			detailBand.setHeight(30);

			// Columna Nombre
			detailBand.addElement(crearCelda("$F{nombreUsuario}", 0, 150));
			// Columna Email (¡Aquí aparecerán los Gmail!)
			detailBand.addElement(crearCelda("$F{emailUsuario}", 150, 250));
			// Columna Chats
			detailBand.addElement(crearCelda("$F{numChats}", 400, 50));

			((net.sf.jasperreports.engine.design.JRDesignSection) jd.getDetailSection()).addBand(detailBand);

			// Compilar y lanzar
			JasperReport jr = JasperCompileManager.compileReport(jd);
			JasperPrint jp = JasperFillManager.fillReport(jr, new HashMap<>(), new JRBeanCollectionDataSource(lista));
			JasperViewer.viewReport(jp, false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Método auxiliar para no repetir código de diseño
	private net.sf.jasperreports.engine.design.JRDesignTextField crearCelda(String expression, int x, int width) {
		net.sf.jasperreports.engine.design.JRDesignTextField tf = new net.sf.jasperreports.engine.design.JRDesignTextField();
		tf.setExpression(new net.sf.jasperreports.engine.design.JRDesignExpression(expression));
		tf.setX(x);
		tf.setY(5);
		tf.setWidth(width);
		tf.setHeight(20);
		return tf;
	}

	// Clase POJO para Jasper (Debe ser pública y tener getters)
	public static class UsuarioReporte {
		private String nombreUsuario;
		private String emailUsuario;
		private int numChats;
		private int numSalas;
		private int numTickets;

		public UsuarioReporte(String nombre, String email, int chats, int salas, int tickets) {
			this.nombreUsuario = nombre;
			this.emailUsuario = email;
			this.numChats = chats;
			this.numSalas = salas;
			this.numTickets = tickets;
		}

		public String getNombreUsuario() {
			return nombreUsuario;
		}

		public String getEmailUsuario() {
			return emailUsuario;
		}

		public int getNumChats() {
			return numChats;
		}

		public int getNumSalas() {
			return numSalas;
		}

		public int getNumTickets() {
			return numTickets;
		}
	}

	// ==========================================
	// RESTO DE MÉTODOS (HISTORIA, TICKETS, ETC)
	// ==========================================

	private void registrarHistoria(String nombreCompleto, String creador, String desc) {
		Map<String, String> info = new HashMap<>();
		info.put("creador", creador);
		info.put("fecha", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
		info.put("miembros", creador);
		info.put("descripcion", desc);
		datosHistoricos.put(nombreCompleto, info);
	}

	@FXML
	private void onHistoriaSala(ActionEvent event) {
		String salaActual = tituloConversacion.getText();
		if (datosHistoricos.containsKey(salaActual)) {
			Map<String, String> info = datosHistoricos.get(salaActual);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Historia");
			alert.setHeaderText("Información de: " + salaActual);
			alert.setContentText(String.format("📅 Fecha: %s\n👤 Creador: %s\n👥 Miembros: %s\n📝 Descripción: %s",
					info.get("fecha"), info.get("creador"), info.get("miembros"), info.get("descripcion")));
			alert.showAndWait();
		} else {
			mostrarError("No hay historia para esta sala.");
		}
	}

	@FXML
	private void onTickets(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/PantallaTickets.fxml"));
			Parent root = loader.load();
			PantallaTickets controller = loader.getController();
			controller.setUsuario(usuarioLabel.getText());
			Stage stage = new Stage();
			stage.setTitle("Tickets");
			stage.setScene(new Scene(root));
			stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
			stage.show();
		} catch (IOException e) {
			mostrarError("Error al cargar tickets.");
		}
	}

	@FXML
	private void onInfoChat(ActionEvent event) {
		mostrarInfo("Info", "Conversación activa: " + tituloConversacion.getText());
	}

	@FXML
	private void onAdjuntar(ActionEvent event) {
		mostrarInfo("Adjuntar", "Próximamente disponible.");
	}

	@FXML
	private void onLogout(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/PantallaLogin.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) logoutBtn.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			mostrarError("Error al cerrar sesión.");
		}
	}

	@FXML
	private void onCrearSala(ActionEvent event) {
		// ... (Tu lógica existente de crear sala)
	}

	@FXML
	private void onUnirseSala(ActionEvent event) {
		// ... (Tu lógica existente de unirse sala)
	}

	private void abrirConversacion(String nombre, String tipo) {
		tituloConversacion.setText(nombre);
		mensajesBox.getChildren().clear();
		agregarBurbujaMensaje("Bienvenido a " + nombre, false);
	}

	@FXML
	private void onEnviar(ActionEvent event) {

		String txt = mensajeField.getText().trim();
		if (txt.isEmpty() || conversacionActiva == null)
			return;

		boolean esSala = tituloConversacion.getText().startsWith("Sala:");
		String yo = usuarioLabel.getText();

		// pinta local
		agregarBurbujaMensaje(txt, true);
		mensajeField.clear();

		// Arma el Mensaje (receptor = otro usuario o nombreSala)
		Mensaje m = new Mensaje(txt, yo, conversacionActiva);

		try {
			TipoComando cmd = esSala ? TipoComando.ENVIAR_MENSAJE_SALA : TipoComando.ENVIAR_MENSAJE_CHAT;
			chat.send(new ServerPeticion(cmd, m));
		} catch (Exception ex) {
			mostrarError("No se pudo enviar el mensaje.");
		}
	}

	private void agregarBurbujaMensaje(String texto, boolean esMio) {
		Text t = new Text(texto);
		TextFlow flow = new TextFlow(t);
		flow.setStyle("-fx-background-color: " + (esMio ? "#dcf8c6" : "#f0f0f0")
				+ "; -fx-padding: 10; -fx-background-radius: 10;");
		mensajesBox.getChildren().add(flow);
	}

	private void mostrarInfo(String t, String m) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setTitle(t);
		a.setContentText(m);
		a.show();
	}

	private void mostrarError(String m) {
		Alert a = new Alert(Alert.AlertType.ERROR);
		a.setContentText(m);
		a.show();
	}

	public void initSession(Map<String, Object> session) {

		this.session = session;
		usuarioLabel.setText((String) session.get("username"));

		// Recoge el ChatClient si ya lo pasas en la sesión al hacer login
		this.chat = (conexionFrontend.ChatClient) session.get("chatClient");
		if (this.chat != null) {
			this.chat.setOnEvent(this::onServerEvent);
		}

		// Listeners de selección (para abrir conversaciones)
		chatsList.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
			if (nuevo != null) {
				salasList.getSelectionModel().clearSelection();
				abrirDM(nuevo);
			}
		});

		salasList.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
			if (nuevo != null) {
				chatsList.getSelectionModel().clearSelection();
				abrirSala(nuevo);
			}
		});

		// Enter para enviar
		mensajeField.setOnAction(e -> onEnviar(null));
	}

	private void cargarHistorialSiFalta(String otro) {
		if (conversaciones.containsKey(otro))
			return;
		try {
			chat.send(new ServerPeticion(TipoComando.OBTENER_HISTORIAL,
					new ConversacionDTO("dm", usuarioLabel.getText(), otro)));
			// El servidor contestará con ServerRespuesta("HISTORIAL_DM", List<Mensaje>)
			// y llegará por onServerEvent(...)
		} catch (Exception ex) {
			estadoLabel.setText("No se pudo cargar historial");
		}
	}

	private void onServerEvent(ServerRespuesta resp) {

		switch (resp.getMensaje()) {
		case "HISTORIAL_DM": {
			@SuppressWarnings("unchecked")
			List<Mensaje> lista = (List<Mensaje>) resp.getContenido();
			// Aplica a la conversación activa si esta era DM
			repintarMensajes(lista);
			break;
		}
		case "HISTORIAL_SALA": {
			@SuppressWarnings("unchecked")
			List<Mensaje> lista = (List<Mensaje>) resp.getContenido();
			repintarMensajes(lista);
			break;
		}
		case "EVENT_RECIBIR_MENSAJE": {

			entidades.Mensaje m = (entidades.Mensaje) resp.getContenido();

			// Si el receptor es una sala, clave = nombre sala; si no, clave = emisor (DM)
			boolean esSala = esSalaNombre(m.getIdReceptor());
			String clave = esSala ? m.getIdReceptor() : m.getIdEmisor();

			conversaciones.computeIfAbsent(clave, k -> FXCollections.observableArrayList()).add(m);

			if (clave.equals(conversacionActiva)) {
				agregarBurbujaMensaje(m.getContenido(), false);
			} else {
				marcarNoLeido(clave);
			}
			break;

		}
		}
	}

	private boolean esSalaNombre(String nombre) {
		if (nombre == null)
			return false;
		return salasList.getItems() != null && salasList.getItems().contains(nombre);
	}

	private void repintarMensajes(List<Mensaje> lista) {
		mensajesBox.getChildren().clear();
		for (Mensaje m : lista) {
			boolean esMio = m.getIdEmisor().equals(usuarioLabel.getText());
			agregarBurbujaMensaje(m.getContenido(), esMio);
		}
	}

	private void pintarConversacion(String otro) {
		tituloConversacion.setText("Chat: " + otro);
		ObservableList<Mensaje> lista = conversaciones.getOrDefault(otro, FXCollections.observableArrayList());
		repintarMensajes(lista);
	}

	private void marcarNoLeido(String contacto) {
		// PoC: prefijo "* " en el ListView
		var items = chatsList.getItems();
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).equals(contacto)) {
				items.set(i, "* " + contacto);
				break;
			}
		}
	}

	private void abrirDM(String otroUsuario) {
		conversacionActiva = otroUsuario;
		tituloConversacion.setText("Chat: " + otroUsuario);
		mensajesBox.getChildren().clear();

		// Pedir historial DM: tipo=dm, a=yo, b=otro
		try {
			if (chat != null) {
				chat.send(new entidades.ServerPeticion(entidades.TipoComando.OBTENER_HISTORIAL,
						new entidades.ConversacionDTO("dm", usuarioLabel.getText(), otroUsuario)));
			} else {
				estadoLabel.setText("Sin conexión al servidor (chat).");
			}
		} catch (Exception e) {
			estadoLabel.setText("No se pudo cargar historial DM");
		}
	}

	private void abrirSala(String nombreSala) {
		conversacionActiva = nombreSala;
		tituloConversacion.setText("Sala: " + nombreSala);
		mensajesBox.getChildren().clear();

		// Pedir historial sala: tipo=sala, a=yo (no se usa), b=nombreSala
		try {
			if (chat != null) {
				chat.send(new entidades.ServerPeticion(entidades.TipoComando.OBTENER_HISTORIAL,
						new entidades.ConversacionDTO("sala", usuarioLabel.getText(), nombreSala)));
			} else {
				estadoLabel.setText("Sin conexión al servidor (chat).");
			}
		} catch (Exception e) {
			estadoLabel.setText("No se pudo cargar historial de sala");
		}
	}

}