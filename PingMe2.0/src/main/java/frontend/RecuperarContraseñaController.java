package frontend;

import java.util.Optional;
import java.util.Properties;

import app.App;
import dao.UsuarioDAO;
import entidades.Usuario;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RecuperarContraseñaController {

	final String ADMIN = "24dm.janire.martinez@arangoya.net";
	final String PASSWORD = "xdcr lfsm nvth wdma";

	@FXML
	private TextField emailField;
	@FXML
	private Button recuperarBtn;

	@FXML
	private void onRecuperar() {
		String email = emailField.getText().trim();

		if (email.isEmpty()) {
			mostrarAlerta("Error", "Por favor ingresa tu email");
			return;
		}

		
		if (!email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[A-Za-z]{2,}$")) {
			mostrarAlerta("Email inválido", "Introduce un email con formato válido.");
			return;
		}

		try {
			UsuarioDAO usuario = new UsuarioDAO();
			Usuario u = usuario.buscarPorEmail(email);

			if (u == null) {
				mostrarAlerta("No encontrado", "No existe un usuario con ese email.");
				return;
			}

			
			String pregunta = (u.getPreguntaSeguridad() != null && !u.getPreguntaSeguridad().isBlank()) ? u.getPreguntaSeguridad()
					: "Pregunta de seguridad";

			TextInputDialog dPregunta = new TextInputDialog();
			dPregunta.setTitle("Validación de usuario");
			dPregunta.setHeaderText(pregunta);
			dPregunta.setContentText("Respuesta:");
			Optional<String> r = dPregunta.showAndWait();

			if (r.isEmpty() || r.get().trim().isEmpty()) {
				mostrarAlerta("Cancelado", "Operación cancelada.");
				return;
			}

			String respuestaIntroducida = r.get().trim();
			boolean ok = usuario.validarRespuestaSeguridad(u, respuestaIntroducida);
			if (!ok) {
				mostrarAlerta("Respuesta incorrecta", "La respuesta no coincide.");
				return;
			}

			
			enviarContrasenaPorEmail(email, u.getPassword());

			mostrarAlerta("Correo enviado", "Se envió tu contraseña al email indicado.");

			
			Stage stage = (Stage) recuperarBtn.getScene().getWindow();
			stage.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			mostrarAlerta("Error", "No se pudo completar la operación.\n" + ex.getMessage());
		}
	}

	
	private void enviarContrasenaPorEmail(String emailDestino, String contrasenaActual) throws Exception {
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");

		jakarta.mail.Session session = jakarta.mail.Session.getInstance(props);

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(ADMIN));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
		message.setSubject("Recuperación de contraseña - PingMe");

		String cuerpo = "Hola,\n\n" + "Has solicitado recuperar tu contraseña.\n\n" + "Tu contraseña actual es: "
				+ contrasenaActual + "\n\n"
				+ "Un saludo,\nPingMe";
		message.setText(cuerpo);

		Transport transport = session.getTransport();
		try {
			transport.connect("smtp.gmail.com", ADMIN, PASSWORD);
			transport.sendMessage(message, message.getAllRecipients());
		} finally {
			transport.close();
		}
	}

	private void mostrarAlerta(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}
}