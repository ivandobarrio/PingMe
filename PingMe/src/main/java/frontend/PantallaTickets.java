package frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PantallaTickets {

    // --- Elementos de la interfaz (Ids deben coincidir con el FXML) ---
    @FXML private TextField asuntoField;
    @FXML private TextArea descripcionArea;
    @FXML private Button enviarBtn;
    @FXML private Button cerrarBtn;
    @FXML private Label estadoLabel;

    // Variable para guardar quién está enviando el ticket
    private String usuarioActual;

    /**
     * Este método recibe el nombre del usuario desde la PantallaInicio.
     * Es necesario para saber quién crea el ticket.
     */
    public void setUsuario(String usuario) {
        this.usuarioActual = usuario;
        // Opcional: Podrías poner el nombre en el estadoLabel si quisieras
        // if (estadoLabel != null) estadoLabel.setText("Ticket de: " + usuario);
    }

    /**
     * Acción al pulsar "Enviar Ticket".
     * Valida los campos y simula el envío.
     */
    @FXML
    private void onEnviarTicket(ActionEvent event) {
        String asunto = asuntoField.getText();
        String descripcion = descripcionArea.getText();

        // 1. Validación básica
        if (asunto == null || asunto.trim().isEmpty() || descripcion == null || descripcion.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Por favor, escribe un asunto y una descripción detallada.");
            return;
        }

        // 2. Simulación de envío (Aquí iría la llamada a Hibernate/Base de Datos)
        System.out.println("=== NUEVO TICKET ===");
        System.out.println("Usuario: " + (usuarioActual != null ? usuarioActual : "Anónimo"));
        System.out.println("Asunto: " + asunto);
        System.out.println("Descripción: " + descripcion);
        System.out.println("====================");

        // 3. Feedback al usuario
        mostrarAlerta(Alert.AlertType.INFORMATION, "Ticket Enviado", "Tu incidencia ha sido registrada correctamente.");

        // 4. Cerrar la ventana
        cerrarVentana();
    }

    /**
     * Acción al pulsar "Cerrar ticket" (Cancelar).
     */
    @FXML
    private void onCerrarTicket(ActionEvent event) {
        cerrarVentana();
    }

    // --- Métodos auxiliares ---

    private void cerrarVentana() {
        // Obtenemos el Stage (ventana) a partir de uno de los botones y lo cerramos
        Stage stage = (Stage) enviarBtn.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}