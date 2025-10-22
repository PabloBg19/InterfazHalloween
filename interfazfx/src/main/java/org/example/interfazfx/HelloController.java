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

/**
 * Controlador para la vista de login/acceso de la aplicación.
 * Gestiona la validación de campos y la navegación hacia la ruleta.
 */
public class HelloController {
    
    // ==================== COMPONENTES FXML ====================
    
    @FXML private Button accederButton;           // Botón para acceder a la aplicación
    @FXML private TextField userTextField;        // Campo de texto para el nombre de usuario
    @FXML private TextField passwordTextField;    // Campo de texto para la contraseña
    @FXML private ComboBox<String> cursoComboBox; // Selector desplegable para elegir curso
    
    // ==================== INICIALIZACIÓN ====================
    
    /**
     * Método que se ejecuta automáticamente tras cargar el FXML.
     * Configura el evento del botón de acceso.
     */
    @FXML
    public void initialize() {
        // Asigna el manejador de evento al botón "Acceder"
        accederButton.setOnAction(event -> {
            StringBuilder errorMessage = new StringBuilder();
            
            // Validación 1: Verifica que el campo de usuario no esté vacío
            if (userTextField.getText().trim().isEmpty()) {
                errorMessage.append("El campo de usuario está vacío.\n");
            }
            
            // Validación 2: Verifica que el campo de contraseña no esté vacío
            if (passwordTextField.getText().trim().isEmpty()) {
                errorMessage.append("El campo de contraseña está vacío.\n");
            }
            
            // Validación 3: Verifica que se haya seleccionado un curso
            if (cursoComboBox.getValue() == null) {
                errorMessage.append("Debes seleccionar un curso.\n");
            }
            
            // Si hay errores, muestra el mensaje y detiene el proceso
            if (errorMessage.length() > 0) {
                mostrarError(errorMessage.toString());
                return; // No continúa si hay errores de validación
            }
            
            // Si todas las validaciones son correctas, abre la ventana de la ruleta
            abrirRuleta();
        });
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * Muestra una ventana de diálogo con un mensaje de error.
     * Aplica un estilo CSS personalizado al diálogo.
     * 
     * @param mensaje Texto del error a mostrar al usuario
     */
    private void mostrarError(String mensaje) {
        // Crea una alerta de tipo ERROR
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación");
        alert.setHeaderText("Campos Requeridos");
        alert.setContentText(mensaje);
        
        // Obtiene el panel del diálogo para aplicarle estilos personalizados
        DialogPane dp = alert.getDialogPane();
        
        // Busca el archivo CSS de error y lo aplica SOLO a este diálogo
        URL cssError = getClass().getResource("/org/example/interfazfx/error.css");
        if (cssError != null) {
            dp.getStylesheets().add(cssError.toExternalForm());
        }
        
        // Muestra el diálogo y espera a que el usuario lo cierre
        alert.showAndWait();
    }
    
    /**
     * Abre la ventana de la ruleta en pantalla completa.
     * Cierra la ventana actual de login tras abrir la nueva ventana.
     */
    private void abrirRuleta() {
        try {
            // Carga el archivo FXML de la ruleta
            URL fxml = getClass().getResource("ruleta-view.fxml");
            if (fxml == null) throw new IllegalStateException("No se encontró ruleta-view.fxml");
            
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            
            // Obtiene el controlador de la ruleta para pasarle los datos del login
            RuletaController controller = loader.getController();
            String nombre = userTextField.getText().trim();  // Obtiene el nombre ingresado
            String curso = cursoComboBox.getValue();         // Obtiene el curso seleccionado
            controller.initializeData(nombre, curso);        // Pasa los datos al controlador
            
            // Crea la nueva escena con el contenido de la ruleta
            Scene scene = new Scene(root);
            
            // Crea una nueva ventana (Stage) para la ruleta
            Stage stage = new Stage();
            stage.setTitle("Ruleta de Halloween");
            stage.setScene(scene);
            
            // Configura la ventana para que se muestre en pantalla completa
            stage.setFullScreen(true);
            
            // Aplica el CSS personalizado si existe
            URL css = getClass().getResource("/org/example/interfazfx/ruleta-style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            
            // Muestra la ventana de la ruleta
            stage.show();
            
            // Cierra la ventana actual (login) para evitar tener múltiples ventanas abiertas
            Stage currentStage = (Stage) accederButton.getScene().getWindow();
            currentStage.close();
            
        } catch (Exception ex) {
            // Captura cualquier error durante la carga de la ruleta
            ex.printStackTrace();
            mostrarError("No se pudo abrir la ruleta.\n" + ex.getMessage());
        }
    }
}
