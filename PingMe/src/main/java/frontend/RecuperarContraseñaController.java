
package frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Properties;

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
		String prueba = "janiremla@gmail.com";

		if (emailUser.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Campo requerido", "Introduce tu email.");
			return;
		}
		// Validación básica de email
		if (!emailUser.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[A-Za-z]{2,}$")) {
			showAlert(Alert.AlertType.WARNING, "Email inválido", "Introduce un email con formato válido.");
			return;
		}

		if (!emailUser.equals(prueba)) {
			showAlert(Alert.AlertType.WARNING, "Email invalido", "Introduce un email ya registrado.");
			return;

		} else {

			// Pregunta de seguridad 1
			TextInputDialog d1 = new TextInputDialog();
			d1.setTitle("Validación de usuario");
			d1.setHeaderText("Pregunta seguridad 1");
			Optional<String> r1 = d1.showAndWait();

			if (!r1.isPresent() || r1.get().trim().isEmpty()) {
				showAlert(Alert.AlertType.INFORMATION, "Cancelado", "Operación cancelada.");
				return;
			}
			String resp1 = r1.get().trim();

			// Pregunta de seguridad 2
			TextInputDialog d2 = new TextInputDialog();
			d2.setTitle("Validación de usuario");
			d2.setHeaderText("Pregunta seguridad 2");
			Optional<String> r2 = d2.showAndWait();

			if (!r2.isPresent() || r2.get().trim().isEmpty()) {
				showAlert(Alert.AlertType.INFORMATION, "Cancelado", "Operación cancelada.");
				return;
			}
			String resp2 = r2.get().trim();

			// Valida respuestas (ajusta a la BD)
			boolean ok = "janire".equalsIgnoreCase(resp1) && "janire".equalsIgnoreCase(resp2);
			if (!ok) {
				showAlert(Alert.AlertType.ERROR, "Respuestas incorrectas", "Alguna de las respuestas no coincide.");
				return;
			}

			// ENVIAR CORREO
			// 1) Propiedades SMTP
			Properties props = new Properties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true"); // STARTTLS

			// 2) Session
			jakarta.mail.Session session = Session.getInstance(props);

			// 3) Message
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ADMIN));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailUser));
			message.setSubject("Contraseña PingMe");
			message.setText("Aqui iria la contraseña del usuario"); // Poner la contraseña del usaurio desde la base de
																	// datos

			// 4) Conexión + envío
			Transport transport = session.getTransport();
			try {
				transport.connect("smtp.gmail.com", ADMIN, PASSWORD);
				transport.sendMessage(message, message.getAllRecipients());
				System.out.println("Email enviado correctamente.");
				showAlert(Alert.AlertType.INFORMATION, "Email enviado correctamente",
						"Mire su correo eletronico introducido.");
			} finally {
				// 5) Cerrar
				transport.close();
			}
		}

		// Cierra la ventana modal
		Stage stage = (Stage) recuperarBtn.getScene().getWindow();
		stage.close();
	}

	// ---------------------------------------------
	// UTILIDAD
	// ---------------------------------------------
	private void showAlert(Alert.AlertType type, String header, String content) {
		Alert alert = new Alert(type);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
