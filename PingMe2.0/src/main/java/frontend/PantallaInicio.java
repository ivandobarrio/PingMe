package frontend;

import conexionFrontend.MensajeCallback;
import conexionFrontend.TcpConexionBasicaClient;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.FileOutputStream;
import java.util.Optional;

import app.App;

public class PantallaInicio implements MensajeCallback {
    
    @FXML private Label usuarioLabel;
    @FXML private Button logoutBtn;
    @FXML private ListView<String> chatsList;
    @FXML private ListView<String> salasList;
    @FXML private Label tituloConversacion;
    @FXML private VBox mensajesBox;
    @FXML private TextField mensajeField;
    @FXML private Label estadoLabel;
    
    private TcpConexionBasicaClient conexion;
    private String conversacionActual;
    private String tipoConversacion; 
    
    @FXML
    public void initialize() {
        conexion = App.getConexion();
        usuarioLabel.setText(App.getUsuarioActual());
        
        
        conexion.setCallback(this);
        
        
        salasList.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                chatsList.getSelectionModel().clearSelection();
                seleccionarSala(nuevo);
            }
        });
        
        chatsList.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                salasList.getSelectionModel().clearSelection();
                seleccionarUsuario(nuevo);
            }
        });
        
        
        onRefrescarUsuarios();
    }
    
    @Override
    public void onMensajeRecibido(String mensaje) {
        String[] partes = mensaje.split("\\|", -1);
        String comando = partes[0];
        
        
        if (comando.equals("PDF_SIZE")) {
            recibirPDF(Integer.parseInt(partes[1]));
            return;
        }

        switch (comando) {
            case "ROOM_CREATED":
                String codigo = partes[1];
                String nombre = partes[2];
                salasList.getItems().add(codigo + " - " + nombre);
                estadoLabel.setText("Sala creada: " + codigo);
                break;
                
            case "ROOM_JOINED":
                String codigoSala = partes[1];
                String nombreSala = partes[2];
                if (!salasList.getItems().contains(codigoSala + " - " + nombreSala)) {
                    salasList.getItems().add(codigoSala + " - " + nombreSala);
                }
                estadoLabel.setText("Te has unido a la sala: " + nombreSala);
                break;
                
            case "MSG_ROOM_EVENT":
                String sala = partes[1];
                String emisor = partes[2];
                String contenido = partes[3];
                
                if (tipoConversacion != null && tipoConversacion.equals("SALA") && 
                    conversacionActual != null && conversacionActual.equals(sala)) {
                    agregarMensaje(emisor, contenido, false);
                }
                break;
                
            case "MSG_PRIVATE_EVENT":
                String origen = partes[1];
                String texto = partes[2];
                
                if (tipoConversacion != null && tipoConversacion.equals("PRIVADO") && 
                    conversacionActual != null && conversacionActual.equals(origen)) {
                    agregarMensaje(origen, texto, false);
                }
                break;
                
            case "MSG_SENT":
                estadoLabel.setText("Mensaje enviado");
                break;
                
            case "USERS_LIST":
                chatsList.getItems().clear();
                break;
                
            case "USER_ITEM":
                chatsList.getItems().add(partes[1]);
                break;
                
            case "HISTORIA_SALA":
            case "HISTORIA_PRIVADA":
                mensajesBox.getChildren().clear();
                estadoLabel.setText("Cargando historial...");
                break;
                
            case "HISTORIA_MSG":
                String emisorHist = partes[1];
                String contenidoHist = partes[2];
                agregarMensaje(emisorHist, contenidoHist, true);
                break;
        }
    }
    
    @FXML
    private void onCrearSala() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crear Sala");
        dialog.setHeaderText("Nueva Sala de Chat");
        dialog.setContentText("Nombre de la sala:");
        
        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(nombre -> {
            if (!nombre.trim().isEmpty()) {
                conexion.enviarLinea("CREATE_ROOM|" + nombre.trim());
            }
        });
    }
    
    @FXML
    private void onUnirseSala() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Unirse a Sala");
        dialog.setHeaderText("Unirse a Sala Existente");
        dialog.setContentText("Código de la sala:");
        
        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(codigo -> {
            if (!codigo.trim().isEmpty()) {
                conexion.enviarLinea("JOIN_ROOM|" + codigo.trim());
            }
        });
    }
    
    @FXML
    private void onRefrescarUsuarios() {
        conexion.enviarLinea("LIST_USERS");
    }
    
    @FXML
    private void onEnviar() {
        String texto = mensajeField.getText().trim();
        
        if (texto.isEmpty() || conversacionActual == null) {
            return;
        }
        
        if (tipoConversacion.equals("SALA")) {
            conexion.enviarLinea("MSG_ROOM|" + conversacionActual + "|" + texto);
            
            
        } else if (tipoConversacion.equals("PRIVADO")) {
            conexion.enviarLinea("MSG_PRIVATE|" + conversacionActual + "|" + texto);
            
            agregarMensaje("Tú", texto, false);
        }
        
        mensajeField.clear();
    }
    
    @FXML
    private void onAdjuntar() {
        estadoLabel.setText("Función de adjuntar archivos no implementada");
    }
    
    @FXML
    private void onHistoriaSala() {
        if (conversacionActual != null && tipoConversacion.equals("SALA")) {
            conexion.enviarLinea("HISTORIA_SALA|" + conversacionActual);
        }
    }
    
    @FXML
    private void onInfoChat() {
        if (conversacionActual != null) {
            mostrarAlerta("Información", "Conversación actual: " + conversacionActual);
        }
    }
    
    @FXML
    private void onGenerarInforme() {
        conexion.enviarLinea("REPORT_USERS");
        estadoLabel.setText("Generando informe...");
    }

    private void recibirPDF(int tamaño) {
        new Thread(() -> {
            try {
                byte[] pdfBytes = conexion.recibirBytes(tamaño);
                String rutaPdf = "informe_usuarios.pdf";
                try (FileOutputStream fos = new FileOutputStream(rutaPdf)) {
                    fos.write(pdfBytes);
                }
                
                javafx.application.Platform.runLater(() -> {
                    estadoLabel.setText("Informe guardado: " + rutaPdf);
                    mostrarAlerta("Informe Generado", "El informe se ha guardado en: " + rutaPdf);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    estadoLabel.setText("Error al recibir PDF");
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void onLogout() {
        App.cambiarEscena("Login.fxml", 720, 460);
    }
    
    private void seleccionarSala(String item) {
        String codigo = item.split(" - ")[0];
        conversacionActual = codigo;
        tipoConversacion = "SALA";
        tituloConversacion.setText("Sala: " + item);
        mensajesBox.getChildren().clear();
        conexion.enviarLinea("HISTORIA_SALA|" + codigo);
    }
    
    private void seleccionarUsuario(String usuario) {
        conversacionActual = usuario;
        tipoConversacion = "PRIVADO";
        tituloConversacion.setText("Chat Privado: " + usuario);
        mensajesBox.getChildren().clear();
        conexion.enviarLinea("HISTORIA_PRIVADA|" + usuario);
    }
    
    private void agregarMensaje(String emisor, String contenido, boolean esHistorial) {
        Label lblEmisor = new Label(emisor + ":");
        lblEmisor.setStyle("-fx-font-weight: bold;");
        
        Label lblContenido = new Label(contenido);
        lblContenido.setWrapText(true);
        lblContenido.setMaxWidth(600);
        
        VBox msgBox = new VBox(2, lblEmisor, lblContenido);
        msgBox.setStyle("-fx-padding: 5; -fx-background-color: #f0f0f0; -fx-background-radius: 5;");
        
        mensajesBox.getChildren().add(msgBox);
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
