package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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
    
    // Simulación de base de datos de salas privadas: Clave = Código, Valor = Nombre de la Sala
    private final Map<String, String> repositorioSalasPrivadas = new HashMap<>();

    @FXML
    public void initialize() {
        salasList.setItems(FXCollections.observableArrayList("Sala General [Público]"));
        
        // Listener para seleccionar sala
        salasList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                chatsList.getSelectionModel().clearSelection();
                abrirConversacion(newVal, "Sala");
            }
        });
    }
    
    @FXML
    private void onInfoChat(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText("Estás viendo la conversación de: " + tituloConversacion.getText());
        alert.showAndWait();
    }
    
    @FXML
    private void onAdjuntar(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Adjuntar archivo");
        alert.setHeaderText(null);
        alert.setContentText("La funcionalidad para adjuntar archivos estará disponible en la próxima actualización.");
        alert.showAndWait();
    }
    
    @FXML
    private void onLogout(ActionEvent event) {
        try {
            // 1. Cargar el FXML del Login
            // Nota: Asegúrate de que la ruta sea exactamente donde está tu login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/PantallaLogin.fxml"));
            Parent root = loader.load();
            
            // 2. Obtener el Stage (ventana) actual desde cualquier botón
            // Usamos logoutBtn que es el botón de la barra superior
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            
            // 3. Cambiar la escena y centrar
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("PingMe - Iniciar Sesión");
            stage.centerOnScreen();
            stage.show();
            
            System.out.println("Sesión cerrada correctamente por el usuario.");
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo volver a la pantalla de login.");
        } catch (NullPointerException e) {
            System.err.println("Error: No se encontró el archivo FXML del Login.");
            mostrarError("Error crítico: Archivo de vista no encontrado.");
        }
    }

    // =================================================
    // LÓGICA DE CREACIÓN (CREADOR SE HACE MIEMBRO)
    // =================================================
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
            if (res.get("tipo").equals("Privado")) {
                String codigo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                repositorioSalasPrivadas.put(codigo, nombre); // Guardamos en el "servidor"
                
                // Al ser el creador, se añade a MI lista directamente
                salasList.getItems().add(nombre + " [Privado]");
                
                mensajeField.setText("He creado una sala privada. Código: " + codigo);
                mostrarInfo("Sala Privada", "Código generado: " + codigo + "\nSolo quienes tengan el código podrán unirse.");
            } else {
                salasList.getItems().add(nombre + " [Público]");
            }
        });
    }

    // =================================================
    // LÓGICA DE UNIRSE (SÓLO APARECE SI PONE EL CÓDIGO)
    // =================================================
    @FXML
    private void onUnirseSala(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Unirse a Sala Privada");
        dialog.setHeaderText("Introduce el código de invitación");
        dialog.setContentText("Código:");

        dialog.showAndWait().ifPresent(codigo -> {
            String codigoBusqueda = codigo.trim().toUpperCase();
            
            if (repositorioSalasPrivadas.containsKey(codigoBusqueda)) {
                String nombreSala = repositorioSalasPrivadas.get(codigoBusqueda);
                String itemLista = nombreSala + " [Privado]";
                
                // Evitar duplicados en la lista del usuario
                if (!salasList.getItems().contains(itemLista)) {
                    salasList.getItems().add(itemLista);
                    estadoLabel.setText("Te has unido a " + nombreSala);
                    abrirConversacion(itemLista, "Sala");
                } else {
                    mostrarError("Ya eres miembro de esta sala.");
                }
            } else {
                mostrarError("El código introducido no es válido.");
            }
        });
    }

    // =================================================
    // MÉTODOS DE SOPORTE
    // =================================================
    private void abrirConversacion(String nombre, String tipo) {
        tituloConversacion.setText(nombre);
        mensajesBox.getChildren().clear();
        agregarBurbujaMensaje("Bienvenido a la " + tipo + ": " + nombre, false);
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

    private void mostrarInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setContentText(msg); a.show();
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg); a.show();
    }

    // Métodos necesarios para la sesión y navegación (clase original)
    public void initSession(Map<String, Object> session) { 
        this.session = session; 
        usuarioLabel.setText((String)session.get("username")); 
    }
    @FXML private void onTickets() { /* lógica tickets */ }
}