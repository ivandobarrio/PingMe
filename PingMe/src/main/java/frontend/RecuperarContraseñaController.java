
package frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    @FXML private TextField emailField;
    @FXML private Button recuperarBtn;

    @FXML
    private void onRecuperar(ActionEvent event) throws AddressException, MessagingException {
    			
    			String emailUser = emailField.getText();
    	
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
    			message.setSubject("Hello world");
    			message.setText("...");
    			
    			// 4) Conexión + envío
    			Transport transport = session.getTransport();
    			try {
    				transport.connect("smtp.gmail.com", ADMIN, PASSWORD);
    				transport.sendMessage(message, message.getAllRecipients());
    				System.out.println("Email enviado correctamente.");
    			} finally {
    				// 5) Cerrar
    				transport.close();
    			}

        /*String email = emailField.getText() != null
                ? emailField.getText().trim()
                : "";

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Campo requerido",
                    "Introduce tu email.");
            return;
        }

        // Validación básica de email
        if (!email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[A-Za-z]{2,}$")) {
            showAlert(Alert.AlertType.WARNING,
                    "Email inválido",
                    "Introduce un email con formato válido.");
            return;
        }

        // ==== Simulación ====
        // Aquí deberás integrar tu backend real de recuperación
        showAlert(Alert.AlertType.INFORMATION,
                "Correo enviado",
                "Hemos enviado un correo con instrucciones para recuperar tu contraseña (simulado).");

        // Cierra la ventana modal
        Stage stage = (Stage) recuperarBtn.getScene().getWindow();
        stage.close();
    }

    // ---------------------------------------------
    //                  UTILIDAD
    // ---------------------------------------------
    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();*/
    }
}
