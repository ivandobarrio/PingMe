package app;

import conexionFrontend.TcpConexionBasicaClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    private static Stage stagePrincipal;
    private static TcpConexionBasicaClient conexion;
    private static String usuarioActual;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
    	
        stagePrincipal = primaryStage;
        
        
        conexion = new TcpConexionBasicaClient();
        
        if (!conexion.conectar()) {
            System.err.println("No se pudo conectar al servidor");
            System.exit(1);
        }
        
        
        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        Scene scene = new Scene(root, 720, 460);
        
        primaryStage.setTitle("PingMe - Cliente");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        
        primaryStage.setOnCloseRequest(event -> {
            if (conexion != null) {
                conexion.desconectar();
            }
        });
    }
    
    public static void cambiarEscena(String fxmlFile, int width, int height) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource("/" + fxmlFile));
            Scene scene = new Scene(root, width, height);
            stagePrincipal.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error al cambiar escena: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static TcpConexionBasicaClient getConexion() {
        return conexion;
    }
    
    public static String getUsuarioActual() {
        return usuarioActual;
    }
    
    public static void setUsuarioActual(String usuario) {
        usuarioActual = usuario;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
