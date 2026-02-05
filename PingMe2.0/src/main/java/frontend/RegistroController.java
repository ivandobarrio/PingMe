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
    
    // Método para manejar el evento de registro
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
        
        // Construir el comando de registro con todos los campos, usando "|" como separador
        StringBuilder comando = new StringBuilder("REGISTER|");
        comando.append(usuario).append("|");
        comando.append(email).append("|");
        comando.append(password).append("|");
        comando.append(edad).append("|");
        comando.append(sexo != null ? sexo : "").append("|");
        comando.append(pregunta != null ? pregunta : "").append("|");
        comando.append(respuesta);
        
        // Enviar el comando al servidor
        conexion.enviarLinea(comando.toString());
    }

    // Método del callback para recibir mensajes del servidor
    @Override
    public void onMensajeRecibido(String mensaje) {
        String[] partes = mensaje.split("\\|", -1);
        
        // Procesar la respuesta del servidor después de intentar registrar un nuevo usuario
        if (partes[0].equals("REGISTER_OK")) {
            mostrarAlerta("Éxito", "Usuario registrado correctamente");
            App.cambiarEscena("Login.fxml", 720, 460);
        } else if (partes[0].equals("REGISTER_ERROR")) {
            String motivo = partes.length > 1 ? partes[1] : "Error desconocido";
            mostrarAlerta("Error de Registro", motivo);
        }
    }
    
    // Método para mostrar una alerta con un título y mensaje específico
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
