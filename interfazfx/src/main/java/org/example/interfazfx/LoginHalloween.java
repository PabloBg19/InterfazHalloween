package org.example.interfazfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoginHalloween extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar el archivo FXML con ruta completa
        URL fxmlLocation = LoginHalloween.class.getResource("/org/example/interfazfx/hello-view.fxml");
        if (fxmlLocation == null) {
            throw new IOException("No se pudo encontrar el archivo FXML: hello-view.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);

        // Cargar la escena
        Scene scene = new Scene(fxmlLoader.load());

        // Cargar el archivo CSS principal
        URL cssLocation = getClass().getResource("/org/example/interfazfx/halloween-style.css");
        if (cssLocation == null) {
            throw new IOException("No se pudo encontrar el archivo CSS: halloween-style.css");
        }
        scene.getStylesheets().add(cssLocation.toExternalForm());

        // Cargar el archivo CSS para diálogos
        URL errorCssLocation = getClass().getResource("/org/example/interfazfx/error.css");
        if (errorCssLocation == null) {
            throw new IOException("No se pudo encontrar el archivo CSS: error.css");
        }
        scene.getStylesheets().add(errorCssLocation.toExternalForm());

        // Configurar y mostrar la ventana
        stage.setTitle("Halloween Login");
        stage.setScene(scene);
        stage.show();
    }
}