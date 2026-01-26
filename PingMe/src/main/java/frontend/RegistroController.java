
package frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistroController {

    @FXML private TextField emailField;
    @FXML private TextField usuarioField;
    @FXML private PasswordField passwordField;
    @FXML private TextField edadField;
    @FXML private ComboBox<String> sexoCombo;
    @FXML private Button registrarBtn;

    @FXML
    private void onRegistrar(ActionEvent event) {

        String email = safeTxt(emailField);
        String usuario = safeTxt(usuarioField);
        String pass = safeTxt(passwordField);
        String edadTxt = safeTxt(edadField);
        String sexo = sexoCombo.getValue();

        // ==========================
        // Validaciones básicas
        // ==========================

        if (email.isEmpty() || usuario.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Campos obligatorios",
                    "Email, usuario y contraseña son obligatorios.");
            return;
        }

        if (!email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[A-Za-z]{2,}$")) {
            showAlert(Alert.AlertType.WARNING,
                    "Email inválido",
                    "Introduce un email con formato válido.");
            return;
        }

        // Validación numérica de edad (opcional)
        Integer edad = null;

        if (!edadTxt.isEmpty()) {
            try {
                edad = Integer.parseInt(edadTxt);

                if (edad < 0 || edad > 120) {
                    showAlert(Alert.AlertType.WARNING,
                            "Edad inválida",
                            "Introduce una edad entre 0 y 120.");
                    return;
                }

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING,
                        "Edad inválida",
                        "La edad debe ser un número.");
                return;
            }
        }

        // ==========================
        // Regla de roles por dominio
        // ==========================

        boolean isAdmin =
                email.toLowerCase().endsWith("@pingme.com") ||
                email.toLowerCase().endsWith("@pingme.net");

        // ==========================
        // Simulación de guardado
        // ==========================

        showAlert(Alert.AlertType.INFORMATION,
                "Registro completado",
                "Usuario registrado correctamente.\n" +
                        (isAdmin
                                ? "Se te asignará rol ADMIN por dominio corporativo."
                                : "Se te asignará rol de usuario estándar."));

        // Cerrar ventana modal
        Stage stage = (Stage) registrarBtn.getScene().getWindow();
        stage.close();
    }

    // ------------------------------------------
    //               UTILIDADES
    // ------------------------------------------

    private String safeTxt(TextInputControl c) {
        return c.getText() != null ? c.getText().trim() : "";
    }

    private void showAlert(Alert.AlertType type, String header, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
