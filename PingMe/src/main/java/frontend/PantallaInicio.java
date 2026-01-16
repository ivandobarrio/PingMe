package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class PantallaInicio {

    // --- Elementos de la Interfaz (IDs del FXML) ---
    @FXML private Label usuarioLabel;
    @FXML private Button logoutBtn;
    @FXML private Button ticketsBtn;

    // Listas (Izquierda)
    @FXML private ListView<String> chatsList;
    @FXML private ListView<String> salasList;

    // Chat (Derecha)
    @FXML private Label tituloConversacion;
    @FXML private VBox mensajesBox;
    @FXML private TextField mensajeField;
    @FXML private Button enviarBtn;
    @FXML private Button adjuntoBtn;
    
    // Estado
    @FXML private Label estadoLabel;

    // Variables de sesión
    private Map<String, Object> session;

    // =================================================
    // MÉTODOS DE INICIALIZACIÓN
    // =================================================

    // Este método se ejecuta automáticamente al cargar el FXML
    @FXML
    public void initialize() {
        // Inicializamos las listas con datos de prueba
        // (Aquí más adelante conectarás con tu Base de Datos)
        ObservableList<String> misChats = FXCollections.observableArrayList(
                "Juan Pérez", "Soporte Técnico", "Ana García"
        );
        chatsList.setItems(misChats);

        ObservableList<String> misSalas = FXCollections.observableArrayList(
                "Sala General", "Proyecto JavaFX"
        );
        salasList.setItems(misSalas);

        // Listener: Qué pasa cuando haces clic en un Chat
        chatsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                salasList.getSelectionModel().clearSelection(); // Desmarcar sala si había una
                abrirConversacion(newVal, "Chat Privado");
            }
        });

        // Listener: Qué pasa cuando haces clic en una Sala
        salasList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                chatsList.getSelectionModel().clearSelection(); // Desmarcar chat si había uno
                abrirConversacion(newVal, "Sala Grupal");
            }
        });
    }

    // Este método lo llamamos desde el Login para pasar los datos del usuario
    public void initSession(Map<String, Object> session) {
        this.session = session;
        String username = (String) session.getOrDefault("username", "Usuario");
        usuarioLabel.setText(username);
    }

    // =================================================
    // LÓGICA DEL CHAT
    // =================================================

    private void abrirConversacion(String nombre, String tipo) {
        tituloConversacion.setText(nombre + " (" + tipo + ")");
        mensajesBox.getChildren().clear(); // Limpiamos el chat anterior

        // Mensaje de bienvenida simulado
        agregarBurbujaMensaje("Te has unido a " + nombre, false);
    }

    @FXML
    private void onEnviar(ActionEvent event) {
        String texto = mensajeField.getText().trim();
        if (!texto.isEmpty()) {
            // true = mensaje mío (se pinta distinto)
            agregarBurbujaMensaje(texto, true);
            mensajeField.clear();
        }
    }

    @FXML
    private void onAdjuntar(ActionEvent event) {
        // Lógica futura para adjuntar archivos
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Función de adjuntar archivos próximamente.");
        alert.show();
    }
    
    @FXML
    private void onInfoChat(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Estás viendo: " + tituloConversacion.getText());
        alert.show();
    }

    // Método auxiliar para pintar los mensajes bonito
    private void agregarBurbujaMensaje(String texto, boolean esMio) {
        Text textNode = new Text(texto);
        TextFlow flow = new TextFlow(textNode);

        if (esMio) {
            // Estilo para mis mensajes (Verde clarito, alineado a la derecha visualmente)
            flow.setStyle("-fx-background-color: #dcf8c6; -fx-padding: 10px; -fx-background-radius: 10px;");
            // Nota: Para alinear a la derecha de verdad, habría que meter el TextFlow en un HBox, 
            // pero para este ejemplo básico sirve así.
        } else {
            // Estilo para mensajes recibidos (Blanco/Gris)
            flow.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10px; -fx-background-radius: 10px;");
        }
        
        mensajesBox.getChildren().add(flow);
        mensajesBox.setSpacing(10); // Espacio entre burbujas
    }

    // =================================================
    // NAVEGACIÓN (LOGOUT Y TICKETS)
    // =================================================

    @FXML
    private void onTickets(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/PantallaTickets.fxml"));
            Parent root = loader.load();

            // Pasamos el usuario al controlador de tickets para saber quién lo envía
            PantallaTickets ticketsCtrl = loader.getController();
            if (session != null) {
                ticketsCtrl.setUsuario((String) session.get("username"));
            }

            // Abrir como ventana modal (bloquea la de atrás)
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nuevo Ticket de Incidencia");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la ventana de Tickets.");
        }
    }

    @FXML
    private void onLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/PantallaLogin.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error al cerrar sesión.");
        }
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(mensaje);
        alert.show();
    }
}