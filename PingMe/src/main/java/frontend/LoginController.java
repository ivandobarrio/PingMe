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

    // --- Elementos del FXML ---
    @FXML private TextField usuarioField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Button forgotBtn;
    @FXML private Button registerBtn;

    // =============================================================
    //   CREDENCIALES DE PRUEBA (Para testear el Frontend)
    // =============================================================
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    private static final String NORMAL_USER = "user";
    private static final String NORMAL_PASS = "user123";
    // =============================================================

    @FXML
    private void onLogin(ActionEvent event) {
        String usuario = usuarioField.getText() != null ? usuarioField.getText().trim() : "";
        String pass = passwordField.getText() != null ? passwordField.getText().trim() : "";

        // 1. Validar que no estén vacíos
        if (usuario.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos requeridos", "Por favor, introduce usuario y contraseña.");
            return;
        }

        // 2. Comprobar credenciales
        boolean esAdmin = usuario.equals(ADMIN_USER) && pass.equals(ADMIN_PASS);
        boolean esUser = usuario.equals(NORMAL_USER) && pass.equals(NORMAL_PASS);

        if (!esAdmin && !esUser) {
            showAlert(Alert.AlertType.ERROR, "Acceso denegado", "Usuario o contraseña incorrectos.\n\nPrueba con:\nadmin / admin123\nuser / user123");
            return;
        }

        // 3. Crear sesión básica
        Map<String, Object> session = new HashMap<>();
        session.put("username", usuario);
        session.put("isAdmin", esAdmin); // Guardamos si es admin o no

        // 4. Redirigir según el tipo de usuario
        if (esAdmin) {
            System.out.println(">> Iniciando como ADMINISTRADOR");
            goTo("/frontend/PantallaInicioAdmin.fxml", controller -> {
                if (controller instanceof AdminTicketsController) {
                    ((AdminTicketsController) controller).initSession(session);
                }
            });

        } else {
            System.out.println(">> Iniciando como USUARIO NORMAL");
            goTo("/frontend/PantallaInicio.fxml", controller -> {
                if (controller instanceof PantallaInicio) {
                    ((PantallaInicio) controller).initSession(session);
                }
            });
        }
    }

    // ===========================================
    //           OTRAS ACCIONES
    // ===========================================

    @FXML
    private void onForgot(ActionEvent event) {
        openModal("/frontend/PantallaRecuperarContraseña.fxml", "Recuperar contraseña");
    }

    @FXML
    private void onRegister(ActionEvent event) {
        goTo("/frontend/PantallaRegistro.fxml", null);
    }

    // ===========================================
    //           UTILIDADES DE NAVEGACIÓN
    // ===========================================

    private void goTo(String fxmlPath, Consumer<Object> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (controllerConsumer != null) {
                Object controller = loader.getController();
                controllerConsumer.accept(controller);
            }

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de navegación", "No se pudo cargar la pantalla: " + fxmlPath + "\nVerifica que el archivo existe y el controller está bien asignado.");
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
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana: " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}