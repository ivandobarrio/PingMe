package frontend;

import app.App;
import conexionFrontend.MensajeCallback;
import conexionFrontend.TcpConexionBasicaClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController implements MensajeCallback {
    
    @FXML private TextField usuarioField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Button forgotBtn;
    
    private TcpConexionBasicaClient conexion;
    
    @FXML
    public void initialize() {
    	// Obtener la conexión TCP desde la clase App
        conexion = App.getConexion();
        
    }
    
    // Método para manejar el evento de login
    // Valida los campos y envía la solicitud de login al servidor
    // Luego, espera la respuesta del servidor para determinar si el login fue exitoso o no
    @FXML
    private void onLogin() {
        String usuario = usuarioField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Error", "Por favor completa todos los campos");
            return;
        }   
        
        conexion.setCallback(this); 
        
        conexion.enviarLinea("LOGIN|" + usuario + "|" + password);
    }

    // Método del callback para recibir mensajes del servidor
    // Procesa la respuesta del servidor después de intentar iniciar sesión
    @Override
    public void onMensajeRecibido(String mensaje) {
        String[] partes = mensaje.split("\\|", -1);
        
        if (partes[0].equals("LOGIN_OK")) {
            App.setUsuarioActual(usuarioField.getText().trim());
            App.cambiarEscena("PantallaInicio.fxml", 980, 620);
        } else if (partes[0].equals("LOGIN_ERROR")) {
            String motivo = partes.length > 1 ? partes[1] : "Error desconocido";
            mostrarAlerta("Error de Login", motivo);
        }
    }
    
    // Método para manejar el evento de registro
    @FXML
    private void onRegister() {
        App.cambiarEscena("Registro.fxml", 720, 500);
    }
    
    // Método para manejar el evento de recuperación de contraseña
    @FXML
    private void onForgot() {
        App.cambiarEscena("RecuperarContraseña.fxml", 780, 340);
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
