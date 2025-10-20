package org.example.interfazfx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;

public class HelloController {
    @FXML private Button accederButton;
    @FXML private TextField userTextField;
    @FXML private TextField passwordTextField;
    @FXML private ComboBox<String> cursoComboBox;

    @FXML
    public void initialize() {
        accederButton.setOnAction(event -> {
            StringBuilder errorMessage = new StringBuilder();

            if (userTextField.getText().trim().isEmpty()) {
                errorMessage.append("El campo de usuario está vacío.\n");
            }
            if (passwordTextField.getText().trim().isEmpty()) {
                errorMessage.append("El campo de contraseña está vacío.\n");
            }
            if (cursoComboBox.getValue() == null) {
                errorMessage.append("Debes seleccionar un curso.\n");
            }


            if (errorMessage.length() > 0) {
                mostrarError(errorMessage.toString());
                return; // No seguimos si hay errores
            }

            // Validación OK: abrimos la ruleta
            abrirRuleta();
        });
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación");
        alert.setHeaderText("Campos Requeridos");
        alert.setContentText(mensaje);

        // Aplica SOLO aquí el CSS de error
        DialogPane dp = alert.getDialogPane();
        URL cssError = getClass().getResource("/org/example/interfazfx/error.css");
        if (cssError != null) {
            dp.getStylesheets().add(cssError.toExternalForm());
        }
        alert.showAndWait();
    }

    private void abrirRuleta() {
        try {
            // Cargamos el FXML de la ruleta
            URL fxml = getClass().getResource("ruleta-view.fxml");
            if (fxml == null) throw new IllegalStateException("No se encontró ruleta-view.fxml");

            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            //traer datos del login
            RuletaController controller = loader.getController();
            String nombre = userTextField.getText().trim();
            String curso = cursoComboBox.getValue();
            controller.initializeData(nombre, curso);
            // Creamos la escena
            Scene scene = new Scene(root);

            // Creamos el nuevo Stage (ventana)
            Stage stage = new Stage();
            stage.setTitle("Ruleta de Halloween");
            stage.setScene(scene);

            // Pantalla completa (sin barra ni bordes)
            stage.setFullScreen(true);

            // (Opcional) aplica el CSS si existe
            URL css = getClass().getResource("/org/example/interfazfx/ruleta-style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            // Mostramos la nueva ventana
            stage.show();

            // (Opcional) cerrar la ventana actual (login)
            Stage currentStage = (Stage) accederButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("No se pudo abrir la ruleta.\n" + ex.getMessage());
        }
    }

}
