
package frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LoginController {

    @FXML private TextField usuarioField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Button forgotBtn;

    @FXML
    private void onLogin(ActionEvent event) {
        String usuario = usuarioField.getText() != null ? usuarioField.getText().trim() : "";
        String pass = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (usuario.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos requeridos", "Introduce usuario y contraseña.");
            return;
        }

        // Simulación de autenticación (quítalo cuando conectes tu backend)
        boolean credencialesCorrectas =
                ("admin".equalsIgnoreCase(usuario) && "admin".equals(pass)) ||
                ("user".equalsIgnoreCase(usuario) && "user".equals(pass));

        if (!credencialesCorrectas) {
            showAlert(Alert.AlertType.ERROR, "Credenciales incorrectas", "Usuario o contraseña no válidos.");
            return;
        }

        boolean esAdmin = "admin".equalsIgnoreCase(usuario);

        Map<String, Object> session = new HashMap<>();
        session.put("username", usuario);
        session.put("email", "");
        session.put("isAdmin", esAdmin);

        if (esAdmin) {
            goTo("/frontend/PantallaTickets.fxml", controller -> {
                if (controller instanceof AdminTicketsController) {
                    AdminTicketsController ctrl = (AdminTicketsController) controller;
                    ctrl.initSession(session);
                }
            });
        } else {
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Inicio de sesión correcto",
                    "Has iniciado sesión como usuario normal.\nAún no existe una pantalla vinculada."
            );
        }
    }

    @FXML
    private void onForgot(ActionEvent event) {
        openModal("/frontend/PantallaRecuperarContraseña.fxml", "Recuperar contraseña");
    }

    @FXML
    private void onOpenRegistrar(ActionEvent event) {
        openModal("/frontend/PantallaRegistro.fxml", "Crear cuenta");
    }

    // ==== Utilidades =====

    private void goTo(String fxmlPath, Consumer<Object> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controllerConsumer != null) controllerConsumer.accept(controller);

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de navegación", "No se pudo abrir la ventana solicitada: " + fxmlPath);
        }
    }

    private void openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage modal = new Stage();
            modal.initOwner((Stage) loginBtn.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(title);
            modal.setScene(new Scene(root));
            modal.centerOnScreen();
            modal.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al abrir ventana", "No se pudo abrir la ventana: " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
