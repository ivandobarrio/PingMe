
package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

public class AdminTicketsController {

    // Barra superior
    @FXML private Label adminNombreLabel;
    @FXML private Button logoutBtn;

    // Lista lateral
    @FXML private ListView<String> ticketsList;

    // Panel derecho (detalle del ticket)
    @FXML private Label ticketTituloLabel;
    @FXML private Label ticketIdLabel;
    @FXML private Label ticketAsuntoLabel;
    @FXML private ChoiceBox<String> estadoChoice;
    @FXML private ChoiceBox<String> prioridadChoice;
    @FXML private TextArea ticketDescripcionArea;

    @FXML private Button resolverBtn;
    @FXML private Button guardarBtn;

    private Map<String, Object> session;

    // ===========================================
    //              INIT SESSION
    // ===========================================
    public void initSession(Map<String, Object> session) {
        this.session = session;

        String adminName = (String) session.getOrDefault("username", "Admin");
        adminNombreLabel.setText(adminName);

        // Lista simulada de tickets
        ObservableList<String> datos = FXCollections.observableArrayList(
                "Ticket #1012 • Alta",
                "Ticket #1011 • Media",
                "Ticket #1010 • Baja",
                "Ticket #1009 • Alta",
                "Ticket #1008 • Media"
        );
        ticketsList.setItems(datos);

        // Selección de ticket
        ticketsList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            if (nuevo != null) cargarTicket(nuevo);
        });

        // Estado inicial del detalle
        ticketTituloLabel.setText("Selecciona un ticket");
        ticketIdLabel.setText("-");
        ticketAsuntoLabel.setText("-");
        ticketDescripcionArea.clear();

        // Selecciones por defecto
        if (!estadoChoice.getItems().isEmpty())
            estadoChoice.getSelectionModel().selectFirst();

        if (!prioridadChoice.getItems().isEmpty())
            prioridadChoice.getSelectionModel().selectFirst();
    }

    // ===========================================
    //         CARGA DE DATOS DEL TICKET
    // ===========================================
    private void cargarTicket(String linea) {

        String id = "-";
        String prioridad = "-";

        try {
            String[] partes = linea.split("•");
            String izquierda = partes[0].trim();           // Ticket #1012
            String derecha = partes.length > 1 ? partes[1].trim() : "-"; // Alta

            if (izquierda.contains("#")) {
                id = izquierda.substring(izquierda.indexOf('#') + 1).trim();
            }

            prioridad = derecha;

        } catch (Exception ignore) {
            // Ignorar errores simples de formato
        }

        ticketTituloLabel.setText("Ticket #" + id);
        ticketIdLabel.setText(id);
        ticketAsuntoLabel.setText("Asunto de ejemplo para #" + id);
        ticketDescripcionArea.setText("Descripción del ticket #" + id + " (simulada).");

        if (prioridad != null && !prioridad.isBlank()) {
            prioridadChoice.getSelectionModel().select(prioridad);
        }

        estadoChoice.getSelectionModel().select("Abierto");
    }

    // ===========================================
    //                 HANDLERS
    // ===========================================

    @FXML
    private void onResolver(ActionEvent event) {
        estadoChoice.getSelectionModel().select("Resuelto");
        showAlert(Alert.AlertType.INFORMATION, "Ticket", "Marcado como resuelto (simulado).");
    }

    @FXML
    private void onGuardar(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Guardado", "Cambios guardados (simulado).");
    }

    @FXML
    private void onLogout(ActionEvent event) {
        goTo("/frontend/PantallaLogin.fxml", null);
    }

    // ===========================================
    //               UTILIDADES
    // ===========================================

    private void goTo(String fxmlPath, Consumer<Object> controllerConsumer) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controllerConsumer != null)
                controllerConsumer.accept(controller);

            Stage stage = (Stage) adminNombreLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al cambiar de pantalla", "No se pudo abrir: " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
