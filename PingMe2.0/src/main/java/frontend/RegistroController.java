package frontend;

import app.App;
import conexionFrontend.MensajeCallback;
import conexionFrontend.TcpConexionBasicaClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegistroController implements MensajeCallback {
    
    @FXML private TextField emailField;
    @FXML private TextField usuarioField;
    @FXML private PasswordField passwordField;
    @FXML private TextField edadField;
    @FXML private ComboBox<String> sexoCombo;
    @FXML private ComboBox<String> pregunta1Combo;
    @FXML private TextField respuesta1Field;
    @FXML private Button registrarBtn;
    
    private TcpConexionBasicaClient conexion;
    
    @FXML
    public void initialize() {
        conexion = App.getConexion();
        
        
        pregunta1Combo.getItems().addAll(
            "¿Cuál es tu color favorito?",
            "¿Nombre de tu primera mascota?",
            "¿Ciudad donde naciste?",
            "¿Comida favorita?"
        );
    }
    
    @FXML
    private void onRegistrar() {
        String email = emailField.getText().trim();
        String usuario = usuarioField.getText().trim();
        String password = passwordField.getText().trim();
        String edad = edadField.getText().trim();
        String sexo = sexoCombo.getValue();
        String pregunta = pregunta1Combo.getValue();
        String respuesta = respuesta1Field.getText().trim();
        
        if (email.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Error", "Por favor completa los campos obligatorios");
            return;
        }
        
        
        conexion.setCallback(this);
        
        
        StringBuilder comando = new StringBuilder("REGISTER|");
        comando.append(usuario).append("|");
        comando.append(email).append("|");
        comando.append(password).append("|");
        comando.append(edad).append("|");
        comando.append(sexo != null ? sexo : "").append("|");
        comando.append(pregunta != null ? pregunta : "").append("|");
        comando.append(respuesta);
        
        
        conexion.enviarLinea(comando.toString());
    }

    @Override
    public void onMensajeRecibido(String mensaje) {
        String[] partes = mensaje.split("\\|", -1);
        
        if (partes[0].equals("REGISTER_OK")) {
            mostrarAlerta("Éxito", "Usuario registrado correctamente");
            App.cambiarEscena("Login.fxml", 720, 460);
        } else if (partes[0].equals("REGISTER_ERROR")) {
            String motivo = partes.length > 1 ? partes[1] : "Error desconocido";
            mostrarAlerta("Error de Registro", motivo);
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
