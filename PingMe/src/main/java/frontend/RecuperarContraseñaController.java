
package frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RecuperarContraseñaController {

    @FXML private TextField emailField;
    @FXML private Button recuperarBtn;

    @FXML
    private void onRecuperar(ActionEvent event) {

        String email = emailField.getText() != null
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
        alert.showAndWait();
    }
}
