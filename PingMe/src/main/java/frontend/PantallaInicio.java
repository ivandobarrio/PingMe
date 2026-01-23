package frontend;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PantallaInicio {

    @FXML private Label usuarioLabel;
    @FXML private ListView<String> chatsList;
    @FXML private ListView<String> salasList;
    @FXML private Label tituloConversacion;
    @FXML private VBox mensajesBox;
    @FXML private TextField mensajeField;
    @FXML private Label estadoLabel;
    @FXML private Button logoutBtn;

    private Map<String, Object> session;
    private final Map<String, String> repositorioSalasPrivadas = new HashMap<>();
    
    // MAPA PARA HISTORIA: <NombreDeSala, <Campo, Valor>>
    private final Map<String, Map<String, String>> datosHistoricos = new HashMap<>();

    @FXML
    public void initialize() {
        String salaGral = "Sala General [Público]";
        salasList.setItems(FXCollections.observableArrayList(salaGral));
        
        // Registrar datos iniciales para la Sala General
        registrarHistoria(salaGral, "Sistema", "Sala abierta para todos los usuarios.");

        salasList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                chatsList.getSelectionModel().clearSelection();
                abrirConversacion(newVal, "Sala");
            }
        });
    }

    private void registrarHistoria(String nombreCompleto, String creador, String desc) {
        Map<String, String> info = new HashMap<>();
        info.put("creador", creador);
        info.put("fecha", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        info.put("miembros", creador);
        info.put("descripcion", desc);
        datosHistoricos.put(nombreCompleto, info);
    }

    @FXML
    private void onHistoriaSala(ActionEvent event) {
        String salaActual = tituloConversacion.getText();

        if (datosHistoricos.containsKey(salaActual)) {
            Map<String, String> info = datosHistoricos.get(salaActual);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Historia de la Sala");
            alert.setHeaderText("Información de la Sala");
            
            String mensaje = String.format(
                "📌 Sala: %s\n\n" +
                "📅 Creada el: %s\n" +
                "👤 Creador por: %s\n" +
                "👥 Miembros: %s\n" +
                "📝 Descripción: %s",
                salaActual, info.get("fecha"), info.get("creador"), info.get("miembros"), info.get("descripcion")
            );
            
            alert.setContentText(mensaje);
            alert.showAndWait();
        } else {
            mostrarError("No hay registros históricos para esta conversación.");
        }
    }

    @FXML
    private void onCrearSala(ActionEvent event) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Nueva Sala");
        ButtonType crearBtnType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField nombreField = new TextField();
        ToggleGroup group = new ToggleGroup();
        RadioButton rbPub = new RadioButton("Público"); rbPub.setToggleGroup(group); rbPub.setSelected(true);
        RadioButton rbPriv = new RadioButton("Privado"); rbPriv.setToggleGroup(group);

        grid.add(new Label("Nombre:"), 0, 0); grid.add(nombreField, 1, 0);
        grid.add(rbPub, 1, 1); grid.add(rbPriv, 1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == crearBtnType) {
                Map<String, String> res = new HashMap<>();
                res.put("nombre", nombreField.getText());
                res.put("tipo", rbPub.isSelected() ? "Público" : "Privado");
                return res;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(res -> {
            String nombre = res.get("nombre");
            String tipo = res.get("tipo");
            String nombreLista = nombre + " [" + tipo + "]";
            
            // Registrar en historia
            registrarHistoria(nombreLista, usuarioLabel.getText(), "Nueva sala " + tipo + " creada por el usuario.");

            if (tipo.equals("Privado")) {
                String codigo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                repositorioSalasPrivadas.put(codigo, nombre);
                salasList.getItems().add(nombreLista);
                mostrarInfo("Sala Privada", "Código: " + codigo);
            } else {
                salasList.getItems().add(nombreLista);
            }
        });
    }

    @FXML
    private void onUnirseSala(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Unirse");
        dialog.setHeaderText("Introduce el código");
        
        dialog.showAndWait().ifPresent(codigo -> {
            String cod = codigo.trim().toUpperCase();
            if (repositorioSalasPrivadas.containsKey(cod)) {
                String nombreSala = repositorioSalasPrivadas.get(cod) + " [Privado]";
                if (!salasList.getItems().contains(nombreSala)) {
                    salasList.getItems().add(nombreSala);
                    
                    // Actualizar lista de miembros en la historia
                    if (datosHistoricos.containsKey(nombreSala)) {
                        String m = datosHistoricos.get(nombreSala).get("miembros");
                        datosHistoricos.get(nombreSala).put("miembros", m + ", " + usuarioLabel.getText());
                    }
                }
            } else {
                mostrarError("Código inválido.");
            }
        });
    }

    // --- Métodos de soporte (abrirConversacion, enviar, etc. se mantienen igual) ---
    private void abrirConversacion(String nombre, String tipo) {
        tituloConversacion.setText(nombre);
        mensajesBox.getChildren().clear();
        agregarBurbujaMensaje("Bienvenido a " + nombre, false);
    }

    @FXML
    private void onEnviar(ActionEvent event) {
        if (!mensajeField.getText().trim().isEmpty()) {
            agregarBurbujaMensaje(mensajeField.getText(), true);
            mensajeField.clear();
        }
    }

    private void agregarBurbujaMensaje(String texto, boolean esMio) {
        Text t = new Text(texto);
        TextFlow flow = new TextFlow(t);
        flow.setStyle("-fx-background-color: " + (esMio ? "#dcf8c6" : "#f0f0f0") + 
                      "; -fx-padding: 10; -fx-background-radius: 10;");
        mensajesBox.getChildren().add(flow);
    }

    private void mostrarInfo(String t, String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(m); a.show(); }
    private void mostrarError(String m) { Alert a = new Alert(Alert.AlertType.ERROR); a.setContentText(m); a.show(); }
    public void initSession(Map<String, Object> session) { this.session = session; usuarioLabel.setText((String)session.get("username")); }
    
 // --- ACCIONES DE LA CABECERA ---

    @FXML 
    private void onInfoChat(ActionEvent event) {
        String salaActual = tituloConversacion.getText();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información del Chat");
        alert.setHeaderText(null);
        alert.setContentText("Estás viendo la conversación de: " + salaActual);
        alert.showAndWait();
    }

    @FXML
    private void onTickets(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/PantallaTickets.fxml"));
            Parent root = loader.load();

            // Pasar el usuario a la pantalla de tickets
            PantallaTickets controller = loader.getController();
            controller.setUsuario(usuarioLabel.getText());

            Stage stage = new Stage();
            stage.setTitle("Soporte - Enviar Ticket");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); 
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo cargar la pantalla de tickets.");
        }
    }

    // --- ACCIONES DE LA BARRA DE MENSAJES ---

    @FXML
    private void onAdjuntar(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Adjuntar archivo");
        alert.setHeaderText(null);
        alert.setContentText("La funcionalidad para adjuntar archivos estará disponible próximamente.");
        alert.showAndWait();
    }

    @FXML
    private void onLogout(ActionEvent event) {
        try {
            // Asegúrate de que esta ruta coincida con tu archivo de Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/PantallaLogin.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PingMe - Iniciar Sesión");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error al cerrar sesión.");
        }
    }
}