package conexionFrontend;

// Interfaz de callback para recibir mensajes del servidor
public interface MensajeCallback {
    void onMensajeRecibido(String mensaje); // Método que se llamará cuando se reciba un mensaje del servidor
}
