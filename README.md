# 🎃 Proyecto "Túnel del Terror → Ruleta TRUCO/TRATO" (JavaFX)

¡Bienvenido/a al README oficial de esta práctica evaluable del módulo M5! Aquí encontrarás todo lo necesario para comprender, ejecutar y presentar la mini-app creada con **JavaFX + Scene Builder**. Se han cuidado todos los apartados de la rúbrica, añadiendo comentarios, emojis y referencias visuales para que puedas defender el trabajo con seguridad ante tu profesor/a. 👻

> ℹ️ **Sugerencia:** añade tus propias capturas de pantalla en la carpeta `docs/` (o la que prefieras) y actualiza las rutas para que se vean en GitHub. He dejado espacios reservados donde debes colocarlas.

---

## 🧭 Índice
1. [Objetivo general](#-objetivo-general)
2. [Tecnologías y estructura del proyecto](#-tecnologías-y-estructura-del-proyecto)
3. [Desglose detallado del código](#-desglose-detallado-del-código)
4. [Pantalla 1 · "Entrada al Túnel"](#-pantalla-1--entrada-al-túnel)
   - [Validaciones y flujo de navegación](#validaciones-y-flujo-de-navegación)
   - [Código destacado del controlador](#código-destacado-del-controlador)
5. [Pantalla 2 · "Ruleta TRUCO/TRATO"](#-pantalla-2--ruleta-trucotrato)
   - [Animación de la ruleta](#animación-de-la-ruleta)
   - [Cálculo del resultado](#cálculo-del-resultado)
   - [Código destacado del controlador](#código-destacado-del-controlador-1)
6. [Estilos y ambientación Halloween](#-estilos-y-ambientación-halloween)
7. [Checklist contra la rúbrica oficial](#-checklist-contra-la-rúbrica-oficial)
8. [Cómo ejecutar la app](#-cómo-ejecutar-la-app)
9. [Capturas sugeridas](#-capturas-sugeridas)
10. [Ideas de ampliación](#-ideas-de-ampliación)

---

## 🎯 Objetivo general
- Construir una mini-app con **dos escenas**: formulario de entrada y ruleta aleatoria.
- Validar datos de usuario (Nombre, Apellidos y Curso) antes de permitir el acceso.
- Transportar los datos a la segunda escena y mostrarlos de forma destacada.
- Ejecutar una animación suave de ruleta que determine aleatoriamente **TRUCO** o **TRATO** (probabilidad 50/50).
- Mantener una estética Halloween coherente con imágenes, tipografías y colores apropiados.

---

## 🧱 Tecnologías y estructura del proyecto

```
InterfazHalloween/
├── README.md  ← Documento que estás leyendo.
└── interfazfx/
    ├── pom.xml
    ├── src/
    │   ├── main/java/org/example/interfazfx/
    │   │   ├── Launcher.java
    │   │   ├── LoginHalloween.java
    │   │   ├── HelloController.java
    │   │   └── RuletaController.java
    │   └── main/resources/org/example/interfazfx/
    │       ├── hello-view.fxml
    │       ├── ruleta-view.fxml
    │       ├── halloween-style.css
    │       ├── ruleta-style.css
    │       ├── error.css
    │       └── fondo.gif · calabaza.png
    └── target/ …
```

- 🧩 **Launcher.java** sirve para lanzar JavaFX en entornos que requieren una clase separada del `Application` (habitual en Maven/IDE).
- 🚪 **LoginHalloween.java** (`Application`) carga la primera escena (`hello-view.fxml`) y aplica hojas de estilo.
- 🕸️ **HelloController.java** coordina el formulario de entrada, validaciones y paso de datos a la ruleta.
- 🎡 **RuletaController.java** dibuja la ruleta en un `Canvas`, anima el giro y muestra el resultado.
- 🪄 **FXML + CSS**: definidos con Scene Builder y personalizados mediante hojas de estilo para reforzar la ambientación.

---

## 🧬 Desglose detallado del código

### 📄 `Launcher.java`
- 👉 Clase puente que solo contiene el método `main` y delega la ejecución en `LoginHalloween`.
- 💡 Útil cuando el empaquetado o ciertos IDE necesitan un punto de entrada fuera de `Application`.

```java
public class Launcher {
    public static void main(String[] args) {
        LoginHalloween.main(args);
    }
}
```

### 🧙 `LoginHalloween.java`
- 🚀 Extiende `Application` e inicia JavaFX cargando `hello-view.fxml`.
- 🧾 Añade la hoja `halloween-style.css` a la escena inicial y bloquea el redimensionamiento para conservar el diseño.

```java
FXMLLoader fxmlLoader = new FXMLLoader(LoginHalloween.class.getResource("hello-view.fxml"));
Scene scene = new Scene(fxmlLoader.load(), 900, 600);
scene.getStylesheets().add(Objects.requireNonNull(
        LoginHalloween.class.getResource("halloween-style.css")).toExternalForm());
stage.setTitle("🎃 Túnel del Terror 🎃");
stage.setScene(scene);
stage.setResizable(false);
stage.show();
```

> 🖼️ **Captura recomendada**: pantalla inicial al ejecutarse la app, mostrando el formulario con la ambientación aplicada.

### 🧛‍♂️ `HelloController.java`
- 🧾 Declara los `@FXML` que enlazan con Scene Builder (`TextField`, `ComboBox`, `Button`, `ImageView`).
- 🧠 En `initialize()` prepara la lista de cursos, activa la validación y centra la imagen animada.
- 🔐 `mostrarError(String mensaje)` crea `Alert` con un `DialogPane` personalizado usando `error.css`.
- 🚪 `abrirRuleta()` carga `ruleta-view.fxml`, obtiene el nuevo controlador e invoca `initializeData(nombre, apellidos, curso)`.

```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("ruleta-view.fxml"));
Parent root = loader.load();
RuletaController ruletaController = loader.getController();
ruletaController.initializeData(nombre, apellidos, curso);
```

> 📷 **Captura recomendada**: formulario con mensaje de error emergente cuando falta algún dato, evidenciando la validación.

### 🧞 `ruleta-view.fxml`
- 🧱 Estructura la interfaz con un `AnchorPane` que contiene un `VBox` central y aplica IDs para el estilo (`root-pane`, `wheel-container`).
- 🎯 Utiliza un `Canvas` para la ruleta, un `StackPane` para el puntero y `Label` con clases CSS personalizadas.
- 🧩 Fácil de editar en Scene Builder: ajusta el tamaño del `Canvas` o el texto del botón directamente desde la vista de diseño.

```xml
<VBox fx:id="mainContainer" alignment="CENTER" spacing="25.0" stylesheets="@ruleta-style.css">
    <Label fx:id="userInfoLabel" text="Bienvenido viajero" />
    <StackPane fx:id="wheelContainer" prefHeight="360.0" prefWidth="360.0" />
    <Button fx:id="spinButton" text="Invocar el Destino" />
    <Label fx:id="resultLabel" text="Los espíritus aguardan..." />
</VBox>
```

### 🧙‍♀️ `RuletaController.java`
- 🎡 Construye la ruleta en tiempo de ejecución con `Canvas` y `GraphicsContext` para mantener flexibilidad de colores y textos.
- 🔁 `spinWheel()` calcula un ángulo aleatorio, anima con `RotateTransition` y gestiona el estado del botón.
- 🎯 `revealResult()` determina el índice ganador y actualiza el mensaje.
- ✨ Incluye `configureWheelContainer()` que suma efectos de sombras (`DropShadow`) y elementos decorativos (puntero, hub central).

```java
double anglePerSection = 360.0 / SECTIONS.size();
for (int i = 0; i < SECTIONS.size(); i++) {
    graphicsContext.setFill(SECTION_COLORS[i % SECTION_COLORS.length]);
    graphicsContext.fillArc(0, 0, size, size, startAngle, anglePerSection, ArcType.ROUND);
    drawSectionLabel(graphicsContext, SECTIONS.get(i), startAngle + anglePerSection / 2, size / 2);
    startAngle += anglePerSection;
}
```

> 🌀 **Captura recomendada**: ruleta girando (botón desactivado) para evidenciar el feedback visual y la animación suave.

### 🧵 Hojas de estilo (`*.css`)
- 🎃 `halloween-style.css` → define `@font-face`, gradientes oscuros, animaciones `glow` para títulos y hover del botón "ACCEDER".
- ⚠️ `error.css` → ajusta colores de fondo, texto y bordes del `Alert` para mantener la temática incluso en los mensajes de validación.
- 🕯️ `ruleta-style.css` → crea un ambiente nocturno, estiliza el botón de giro con efectos luminosos y resalta el resultado final mediante la clase `.celebration`.

```css
.celebration {
    -fx-text-fill: #ffd166;
    -fx-font-size: 26px;
    -fx-font-weight: bold;
    -fx-effect: dropshadow(gaussian, rgba(255, 209, 102, 0.8), 15, 0.6, 0, 0);
}
```

---

## 🚪 Pantalla 1 · "Entrada al Túnel"
![Captura de la pantalla de entrada](docs/captura-entrada.png) <!-- Sustituye la ruta por la de tu imagen real -->

### 🎨 Maquetación
- Contenedor principal `Pane` con **imagen animada** (`fondo.gif`) cubriendo toda la escena.
- `BorderPane` central que organiza título, formulario y botón.
- Labels, campos y botón comparten la hoja `halloween-style.css` para mantener contraste y legibilidad.

### 📝 Controles del formulario
- `TextField` → Nombre de usuario.
- `TextField` → Apellidos / contraseña (puedes renombrar en Scene Builder).
- `ComboBox` → Curso (valores iniciales: `dam`, `daw`, `asir`).
- `Button` → "ACCEDER" con id `accederButton`.

### ✅ Validaciones y flujo de navegación
- El método `initialize()` registra un **evento `OnAction`** sobre el botón.
- Se revisa que **ningún campo quede vacío** y que el usuario seleccione un curso.
- Ante un error, se construye un `Alert` con mensajes breves, claros y estilizados (RA4).
- Con validación correcta, se ejecuta `abrirRuleta()` que carga la segunda escena, envía datos y abre la ventana a pantalla completa.

### 🔍 Código destacado del controlador
```java
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
            return;
        }
        abrirRuleta();
    });
}
```
📌 Observaciones:
- Se usa un `StringBuilder` para acumular errores y mostrarlos de una vez.
- `mostrarError()` aplica un CSS exclusivo (`error.css`) para tematizar la ventana emergente.
- `abrirRuleta()` empaqueta los datos (`nombre`, `curso`) y los envía al segundo controlador mediante `initializeData()`.

---

## 🎡 Pantalla 2 · "Ruleta TRUCO/TRATO"
![Captura de la ruleta](docs/captura-ruleta.png) <!-- Sustituye la ruta por la de tu imagen real -->

### 📐 Layout general (`ruleta-view.fxml`)
- `AnchorPane` con hoja de estilo `ruleta-style.css`.
- `VBox` central alinea título, contenedor de la ruleta, botón y etiqueta de resultado.
- `Label` superior muestra "Bienvenido <Nombre> del curso <Curso>".

### 🌀 Animación de la ruleta
- La ruleta se dibuja en un `Canvas` dentro de `wheelContainer`.
- Al pulsar "Invocar el Destino", se lanza un `RotateTransition` que rota entre 720º y 1800º con suavizado `Interpolator.EASE_OUT`.
- Durante el giro, el botón queda deshabilitado para evitar interacciones repetidas.

### 🪄 Cálculo del resultado
- Tras la animación, se normaliza el ángulo (`currentRotation`) para saber qué segmento quedó en la parte superior.
- Se calcula el índice (`TRUCO` o `TRATO`) con división modular y se actualiza el `Label` con la clase `celebration`.
- El mensaje final muestra "El destino revela: TRUCO." o "El destino revela: TRATO." (probabilidad 50/50).

### 🔍 Código destacado del controlador
```java
private void spinWheel() {
    spinButton.setDisable(true);
    resultLabel.getStyleClass().remove("celebration");
    resultLabel.setText("Los espíritus preparan el destino...");

    double spinAngle = 720 + random.nextDouble() * 1080;
    Duration duration = Duration.seconds(4.2 + random.nextDouble());

    RotateTransition rotateTransition = new RotateTransition(duration, wheelCanvas);
    rotateTransition.setByAngle(spinAngle);
    rotateTransition.setInterpolator(Interpolator.EASE_OUT);
    rotateTransition.setOnFinished(event -> {
        currentRotation = (wheelCanvas.getRotate() % 360 + 360) % 360;
        revealResult();
        spinButton.setDisable(false);
    });
    rotateTransition.play();
}
```
```java
private void revealResult() {
    double anglePerSection = 360.0 / SECTIONS.size();
    double normalized = (360 - currentRotation) % 360;
    double adjusted = (normalized + anglePerSection / 2) % 360;
    int index = (int) (adjusted / anglePerSection) % SECTIONS.size();

    String selection = SECTIONS.get(index);
    resultLabel.setText("El destino revela: " + selection + ".");
    if (!resultLabel.getStyleClass().contains("celebration")) {
        resultLabel.getStyleClass().add("celebration");
    }
}
```
📌 Observaciones:
- `SECTIONS` controla las opciones disponibles (puedes añadir más trucos/tratos).
- `SECTION_COLORS` alterna tonalidades para mayor claridad visual.
- `configureWheelContainer()` añade sombras (`DropShadow`) y puntero superior para resaltar el resultado ganador.

---

## 🧛 Estilos y ambientación Halloween
- **`halloween-style.css`** → define tipografías góticas, degradados rojizos, sombras y animaciones de hover para botones.
- **`error.css`** → personaliza las ventanas `Alert` con fondo oscuro y borde naranja.
- **`ruleta-style.css`** → crea un escenario con gradiente radial, botón con efecto de iluminación y etiqueta celebratoria.
- **Recursos gráficos** → `fondo.gif` (ambientación en la pantalla de entrada) y `calabaza.png` (elemento decorativo en la ruleta).

> 🎨 Consejo: abre los FXML en Scene Builder para apreciar cómo se aplican los estilos y ajustar detalles de alineación o fuentes.

---

## ☑️ Checklist contra la rúbrica oficial
1. **Interfaz “Entrada al Túnel” (RA1, RA4)** ✅
   - Distribución limpia en `BorderPane`, estética Halloween reforzada con `halloween-style.css` y fondo animado.
2. **Validación y mensajes (RA4)** ✅
   - No permite avanzar con campos vacíos; `Alert` personalizado y mensajes concisos.
3. **Paso de datos a la Ruleta (RA1)** ✅
   - `initializeData(nombre, curso)` recibe la información y la pinta en `userInfoLabel`.
4. **Ruleta y feedback visual (RA3, RA4)** ✅
   - Giro suavizado con `RotateTransition`, botón deshabilitado durante la animación y mensajes claros de estado.
5. **Resultado TRUCO/TRATO (RA4)** ✅
   - Resultado destacado con estilo `celebration`, mensaje final expresivo.

---

## 🛠️ Cómo ejecutar la app
Asegúrate de tener **JDK 21** (o superior) y **Maven 3.8+**.

```bash
cd interfazfx
mvn clean javafx:run
```
- Maven compilará el proyecto, descargará JavaFX y lanzará `LoginHalloween`.
- Si prefieres un IDE (IntelliJ/NetBeans/Eclipse) basta con importar el `pom.xml` y ejecutar la clase `Launcher`.

---

## 📸 Capturas sugeridas
- `docs/captura-entrada.png` → Pantalla del formulario justo antes de pulsar "ACCEDER".
- `docs/captura-ruleta.png` → Ruleta a mitad de animación o mostrando el resultado final.

> 📷 Añade las capturas a tu repositorio y actualiza las rutas en este README para que se muestren en GitHub.

---

## 💡 Ideas de ampliación
- Ampliar el `ComboBox` a `DAM1`, `DAM2`, `DAW1`, `DAW2`, `SMR1`, `SMR2` como sugiere el enunciado.
- Mostrar Nombre + Apellidos en la etiqueta de bienvenida (solo habría que concatenar ambos campos).
- Añadir una lista de "trucos" y "tratos" temáticos y mostrarlos bajo el resultado.
- Incluir efectos de sonido al girar la ruleta o al obtener un resultado específico.

---

¡Listo! Con este README podrás justificar cada criterio, explicar el código en clase y lucir una presentación impecable en GitHub. 🦇 Si necesitas ajustes adicionales (traducciones, más capturas, etc.), modifícalo sin miedo. ¡Mucho éxito en tu entrega! 🕯️
