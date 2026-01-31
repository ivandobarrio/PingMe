
package frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.UsuarioService;

import java.util.Optional;
import java.util.Properties;

import entidades.Usuario;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class RecuperarContraseñaController {

	final String ADMIN = "24dm.janire.martinez@arangoya.net";
	final String PASSWORD = "xdcr lfsm nvth wdma";

	@FXML
	private TextField emailField;
	@FXML
	private Button recuperarBtn;

	@FXML
	private void onRecuperar(ActionEvent event) throws AddressException, MessagingException {
		String emailUser = emailField.getText() != null ? emailField.getText().trim() : "";

		if (emailUser.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Campo requerido", "Introduce tu email.");
			return;
		}
		// Validación básica de email
		if (!emailUser.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[A-Za-z]{2,}$")) {
			showAlert(Alert.AlertType.WARNING, "Email inválido", "Introduce un email con formato válido.");
			return;
		}

		try {
			UsuarioService usuarioService = new UsuarioService();
			Usuario u = usuarioService.obtenerPorEmail(emailUser);

			if (u == null) {
				showAlert(Alert.AlertType.WARNING, "No encontrado", "No existe un usuario con ese email.");
				return;
			}

			// 1) Mostrar la PREGUNTA DE SEGURIDAD desde la BD
			String pregunta = (u.getPregunta() != null && !u.getPregunta().isBlank()) ? u.getPregunta()
					: "Pregunta de seguridad";

			TextInputDialog dPregunta = new TextInputDialog();
			dPregunta.setTitle("Validación de usuario");
			dPregunta.setHeaderText(pregunta);
			dPregunta.setContentText("Respuesta:");
			Optional<String> r = dPregunta.showAndWait();

			if (r.isEmpty() || r.get().trim().isEmpty()) {
				showInfo("Cancelado", "Operación cancelada.");
				return;
			}

			String respuestaIntroducida = r.get().trim();
			boolean ok = usuarioService.validarRespuestaSeguridad(u, respuestaIntroducida);
			if (!ok) {
				showAlert(Alert.AlertType.ERROR, "Respuesta incorrecta", "La respuesta no coincide.");
				return;
			}

			// 3) Enviar por correo la contraseña actual de la BD
			enviarContrasenaPorEmail(emailUser, u.getContraseña());

			showInfo("Correo enviado", "Se envió tu contraseña al email indicado.");

			// 4) Cerrar la ventana
			Stage stage = (Stage) recuperarBtn.getScene().getWindow();
			stage.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error", "No se pudo completar la operación.\n" + ex.getMessage());
		}
	}

	// Envío de email con la contraseña
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
				+ contrasenaActual + "\n\n" + "Por seguridad, te recomendamos cambiarla después de iniciar sesión.\n\n"
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

	private void showInfo(String header, String content) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setHeaderText(header);
		a.setContentText(content);
		a.showAndWait();
	}

	private void showAlert(Alert.AlertType type, String header, String content) {
		Alert alert = new Alert(type);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

}
