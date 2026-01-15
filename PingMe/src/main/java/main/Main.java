
package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Ruta del FXML inicial dentro de src/main/resources/frontend/
        final String fxmlPath = "/frontend/PantallaLogin.fxml";

        URL fxmlURL = getClass().getResource(fxmlPath);

        if (fxmlURL == null) {
            throw new IllegalStateException(
                "No se encontró el archivo FXML en el classpath: " + fxmlPath
                + "\nAsegúrate de que está en src/main/resources/frontend/"
            );
        }

        Parent root = FXMLLoader.load(fxmlURL);

        Scene scene = new Scene(root);

        primaryStage.setTitle("PingMe - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
