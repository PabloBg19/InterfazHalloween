package org.example.interfazfx;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RuletaController {
    private static final double CANVAS_SIZE = 320.0;
    private static final double WHEEL_RADIUS = CANVAS_SIZE / 2 - 16;
    private static final double POINTER_WIDTH = 52.0;
    private static final double POINTER_HEIGHT = 74.0;

    private static final List<String> SECTIONS = List.of("TRUCO", "TRATO");

    private static final List<Color> SECTION_COLORS = List.of(
            Color.web("#A31B00"),
            Color.web("#5E2400")
    );

    @FXML private Pane wheelContainer;
    @FXML private Button spinButton;
    @FXML private Label resultLabel;
    @FXML private Label userInfoLabel;

    private Canvas wheelCanvas;
    private Polygon pointer;
    private Circle hub;
    private double currentRotation;
    private final Random random = new Random();
    private String nombre;
    private String curso;

    // Capa de efectos y overlays
    private Pane effectLayer;
    private Rectangle darkOverlay;

    // Fantasma (para TRATO)
    private ImageView ghostView;

    // ===== Assets/Efectos TRUCO (jumpscare) =====
    private ImageView jumpscareView;   // GIF jumpscare
    private Rectangle vignette;        // Viñeta oscura
    private Rectangle strobe;          // Flash blanco
    private GaussianBlur blurFx;       // Glitch blur
    private ColorAdjust colorShift;    // Glitch color

    // Sonidos
    private MediaPlayer thunderPlayer;     // trueno/impacto (opcional)
    private MediaPlayer screamPlayer;      // grito (solo TRUCO)
    private MediaPlayer rumblePlayer;      // grave/rumble opcional
    private MediaPlayer hymnPlayer;        // HIMNO (TRATO)  <<<<<<

    @FXML
    public void initialize() {
        configureWheelContainer();
        loadOptionalMedia();
        drawWheel();
        spinButton.setOnAction(event -> spinWheel());
        setupHoverAnimations();
    }

    public void initializeData(String nombre, String curso) {
        this.nombre = nombre;
        this.curso = curso;
        if (userInfoLabel != null) {
            userInfoLabel.setText("Bienvenido " + nombre + " del curso: " + curso);
        }
    }

    private void configureWheelContainer() {
        double containerSize = CANVAS_SIZE + 120;
        wheelContainer.setPrefSize(containerSize, containerSize);
        wheelContainer.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        wheelContainer.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        double offset = (containerSize - CANVAS_SIZE) / 2;

        // Canvas ruleta
        wheelCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        wheelCanvas.setLayoutX(offset);
        wheelCanvas.setLayoutY(offset);
        DropShadow canvasShadow = new DropShadow(35, Color.color(0, 0, 0, 0.85));
        canvasShadow.setSpread(0.25);
        wheelCanvas.setEffect(canvasShadow);
        wheelCanvas.setCache(true);
        wheelCanvas.setCacheHint(CacheHint.SPEED);

        // Puntero
        pointer = new Polygon();
        pointer.getStyleClass().add("wheel-pointer");
        pointer.setStrokeLineJoin(StrokeLineJoin.ROUND);
        pointer.setMouseTransparent(true);
        updatePointerShape(offset);
        pointer.setEffect(new DropShadow(20, Color.color(0, 0, 0, 0.9)));

        // Hub
        hub = new Circle();
        hub.getStyleClass().add("wheel-hub");
        hub.setRadius(28);
        hub.setMouseTransparent(true);
        hub.setLayoutX(offset + CANVAS_SIZE / 2);
        hub.setLayoutY(offset + CANVAS_SIZE / 2);
        hub.setFill(new RadialGradient(0, 0, hub.getLayoutX(), hub.getLayoutY(), 28, false,
                CycleMethod.NO_CYCLE, new Stop(0, Color.web("#ffe8a3")), new Stop(1, Color.web("#c79532"))));
        hub.setEffect(new InnerShadow(12, Color.rgb(0,0,0,0.6)));

        // Capa de efectos
        effectLayer = new Pane();
        effectLayer.setPickOnBounds(false);
        effectLayer.setPrefSize(containerSize, containerSize);

        darkOverlay = new Rectangle(containerSize, containerSize);
        darkOverlay.setFill(Color.color(0,0,0,0.0));
        darkOverlay.setVisible(false);

        // Viñeta (para TRUCO)
        vignette = new Rectangle(containerSize, containerSize);
        vignette.setMouseTransparent(true);
        vignette.setVisible(false);
        vignette.setFill(new RadialGradient(
                0, 0,
                containerSize/2, containerSize/2,
                containerSize/2,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.60, Color.color(0,0,0,0.0)),
                new Stop(0.90, Color.color(0,0,0,0.65)),
                new Stop(1.00, Color.color(0,0,0,0.9))
        ));

        // Flash (strobe)
        strobe = new Rectangle(containerSize, containerSize);
        strobe.setVisible(false);
        strobe.setOpacity(0.0);
        strobe.setFill(Color.WHITE);
        strobe.setMouseTransparent(true);

        // Fantasma (TRATO)
        ghostView = new ImageView();
        ghostView.setVisible(false);
        ghostView.setManaged(false);

        // Jumpscare (TRUCO)
        jumpscareView = new ImageView();
        jumpscareView.setVisible(false);
        jumpscareView.setManaged(false);
        jumpscareView.setPreserveRatio(true);

        effectLayer.getChildren().addAll(darkOverlay, vignette, strobe, ghostView, jumpscareView);

        // Orden de apilado
        wheelContainer.getChildren().setAll(wheelCanvas, pointer, hub, effectLayer);

        // Efectos glitch (TRUCO)
        blurFx = new GaussianBlur(0);
        colorShift = new ColorAdjust(0, 0, 0, 0);
        wheelContainer.setEffect(new Blend(BlendMode.MULTIPLY, blurFx, colorShift));
    }

    private void updatePointerShape(double offset) {
        double center = offset + CANVAS_SIZE / 2;
        double tipY = offset - 18;
        double baseY = tipY + POINTER_HEIGHT;
        pointer.getPoints().setAll(
                center, tipY,
                center - POINTER_WIDTH / 2, baseY,
                center + POINTER_WIDTH / 2, baseY
        );
        pointer.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffd68a")), new Stop(1, Color.web("#b37400"))));
        pointer.setStroke(Color.web("#663b00"));
        pointer.setStrokeWidth(2.2);
    }

    private void drawWheel() {
        GraphicsContext gc = wheelCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        double center = CANVAS_SIZE / 2;
        double radius = WHEEL_RADIUS;
        double anglePerSection = 360.0 / SECTIONS.size();
        double startAngle = 90 - anglePerSection / 2;

        // Anillo exterior
        gc.setFill(Color.web("#1a0c22"));
        gc.fillOval(center - radius - 22, center - radius - 22, (radius + 22) * 2, (radius + 22) * 2);

        // Secciones
        for (int i = 0; i < SECTIONS.size(); i++) {
            double currentStart = startAngle + i * anglePerSection;
            Color color = SECTION_COLORS.get(i % SECTION_COLORS.size());

            gc.setFill(color);
            gc.fillArc(center - radius, center - radius, radius * 2, radius * 2,
                    currentStart, anglePerSection, ArcType.ROUND);

            gc.setStroke(Color.web("#1a0c22"));
            gc.setLineWidth(3);
            gc.strokeArc(center - radius, center - radius, radius * 2, radius * 2,
                    currentStart, anglePerSection, ArcType.ROUND);

            drawSectionText(gc, SECTIONS.get(i), currentStart + anglePerSection / 2,
                    radius * 0.64, center);
        }

        // Centro decorativo
        gc.setFill(Color.web("#260f2c"));
        gc.fillOval(center - radius * 0.4, center - radius * 0.4, radius * 0.8, radius * 0.8);
    }

    private void drawSectionText(GraphicsContext gc, String textValue, double angle, double textRadius, double center) {
        double radians = Math.toRadians(angle);
        double textX = center + textRadius * Math.cos(radians);
        double textY = center - textRadius * Math.sin(radians);

        gc.save();
        gc.translate(textX, textY);
        gc.rotate(-angle);

        Font font = Font.font("Cinzel", FontWeight.SEMI_BOLD, 18);
        gc.setFont(font);
        gc.setFill(Color.web("#fbf5ff"));
        Text helper = new Text(textValue);
        helper.setFont(font);
        double textWidth = helper.getLayoutBounds().getWidth();
        gc.fillText(textValue, -textWidth / 2, 6);

        gc.restore();
    }

    private void setupHoverAnimations() {
        spinButton.hoverProperty().addListener((obs, old, isHover) -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), hub);
            st.setToX(isHover ? 1.1 : 1.0);
            st.setToY(isHover ? 1.1 : 1.0);
            st.play();
        });
    }

    private void spinWheel() {
        spinButton.setDisable(true);
        resultLabel.getStyleClass().remove("celebration");
        resultLabel.setText("Los espíritus preparan el destino...");

        stopAllSoundsSmooth();

        // Glow leve durante el giro
        Glow glow = new Glow(0.0);
        hub.setEffect(new Blend(BlendMode.MULTIPLY, hub.getEffect(), glow));
        Timeline glowTl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.0)),
                new KeyFrame(Duration.seconds(1.2), new KeyValue(glow.levelProperty(), 0.65))
        );
        glowTl.setAutoReverse(true);
        glowTl.setCycleCount(Animation.INDEFINITE);
        glowTl.play();

        // Ángulo aleatorio
        double spinAngle = 720 + random.nextDouble() * 1080;
        Duration duration = Duration.seconds(4.2 + random.nextDouble());

        // Ticks simulados
        AnimationTimer ticks = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (last == 0) last = now;
                if (now - last > 30_000_000) { // ~33fps
                    pointer.setRotate((pointer.getRotate() + 6) % 12 - 6);
                    last = now;
                }
            }
            @Override public void stop() {
                super.stop();
                pointer.setRotate(0);
            }
        };
        ticks.start();

        RotateTransition rotateTransition = new RotateTransition(duration, wheelCanvas);
        rotateTransition.setByAngle(spinAngle);
        rotateTransition.setInterpolator(Interpolator.SPLINE(0.1, 0.9, 0.2, 1.0));
        rotateTransition.setOnFinished(event -> {
            ticks.stop();
            glowTl.stop();
            hub.setEffect(new InnerShadow(12, Color.rgb(0,0,0,0.6)));
            currentRotation = (wheelCanvas.getRotate() % 360 + 360) % 360;
            revealResult();
            spinButton.setDisable(false);
        });
        rotateTransition.play();
    }

    private void fadeOutAndStop(MediaPlayer mp, Duration d) {
        if (mp == null) return;
        try {
            double from = mp.getVolume();
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(mp.volumeProperty(), from)),
                    new KeyFrame(d, e -> { mp.stop(); mp.setVolume(from); }, new KeyValue(mp.volumeProperty(), 0.0))
            );
            tl.play();
        } catch (Exception ignored) {}
    }

    private void stopAllSoundsSmooth() {
        fadeOutAndStop(thunderPlayer, Duration.millis(150));
        fadeOutAndStop(screamPlayer, Duration.millis(120));
        fadeOutAndStop(rumblePlayer, Duration.millis(120));
        fadeOutAndStop(hymnPlayer, Duration.millis(250)); // parar himno con fade suave
    }

    private void revealResult() {
        double anglePerSection = 360.0 / SECTIONS.size();
        double normalized = (360 - currentRotation) % 360;
        if (normalized < 0) normalized += 360;
        double adjusted = (normalized + anglePerSection / 2) % 360;
        int index = (int) (adjusted / anglePerSection) % SECTIONS.size();

        String selection = SECTIONS.get(index);
        if ("TRUCO".equals(selection)) {
            resultLabel.setText("¡TRUCO! 😱");
            runTrucoJumpscare(); // jumpscare + scream
        } else {
            resultLabel.setText("¡TRATO! 🎵👻");
            runTratoFantasmaConHimno();  // fantasma + HIMNO (se escucha)
        }
    }

    // ===== TRUCO: Jumpscare + scream =====
    private void runTrucoJumpscare() {
        stopAllSoundsSmooth();

        darkOverlay.setVisible(true);
        vignette.setVisible(true);
        strobe.setVisible(false);
        ghostView.setVisible(false);
        jumpscareView.setVisible(false);

        // 1) fundido oscuro
        Timeline dim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(darkOverlay.fillProperty(), Color.color(0,0,0,0.0))),
                new KeyFrame(Duration.millis(250), new KeyValue(darkOverlay.fillProperty(), Color.color(0,0,0,0.55)))
        );

        // 2) sonidos base
        playSafe(thunderPlayer);
        playSafe(rumblePlayer);

        try {
            URL gifUrl = getClass().getResource("jumpscare.gif");
            if (gifUrl != null) {
                Image gif = new Image(gifUrl.toExternalForm());
                jumpscareView.setImage(gif);
                jumpscareView.setOpacity(0);
                jumpscareView.setVisible(true);

                double iw = 520;
                jumpscareView.setFitWidth(iw);
                jumpscareView.setLayoutX((wheelContainer.getWidth() - iw)/2);
                jumpscareView.setLayoutY((wheelContainer.getHeight() - iw)/2);

                // zoom "cámara"
                ScaleTransition camZoom = new ScaleTransition(Duration.millis(240), wheelContainer);
                camZoom.setToX(1.06);
                camZoom.setToY(1.06);
                camZoom.setAutoReverse(true);
                camZoom.setCycleCount(2);

                // aparición violenta del gif
                ScaleTransition jZoom = new ScaleTransition(Duration.millis(160), jumpscareView);
                jZoom.setFromX(0.85); jZoom.setFromY(0.85);
                jZoom.setToX(1.18);   jZoom.setToY(1.18);

                FadeTransition jFade = new FadeTransition(Duration.millis(120), jumpscareView);
                jFade.setFromValue(0); jFade.setToValue(1);

                ParallelTransition jumpscareAppear = new ParallelTransition(jZoom, jFade);

                // grito (ligero retardo)
                PauseTransition screamDelay = new PauseTransition(Duration.millis(60));
                screamDelay.setOnFinished(e -> playSafe(screamPlayer));

                // flashes + glitch + temblores
                TranslateTransition shake1 = makeShake(wheelContainer, 16, 6, 5);
                TranslateTransition shake2 = makeShake(wheelContainer, 9, 4, 5);
                Runnable flashes = () -> flashStrobe(5, 35, 55);
                Runnable glitch = () -> microGlitch(Duration.millis(300));

                // vibración del puntero
                RotateTransition pointerTwitch = new RotateTransition(Duration.millis(200), pointer);
                pointerTwitch.setFromAngle(-12);
                pointerTwitch.setToAngle(12);
                pointerTwitch.setAutoReverse(true);
                pointerTwitch.setCycleCount(3);

                // limpieza
                PauseTransition hold = new PauseTransition(Duration.millis(600));
                Timeline undim = new Timeline(
                        new KeyFrame(Duration.millis(240), ev -> {
                            darkOverlay.setVisible(false);
                            vignette.setVisible(false);
                            strobe.setVisible(false);
                            jumpscareView.setVisible(false);
                            jumpscareView.setOpacity(0);
                            wheelContainer.setScaleX(1.0);
                            wheelContainer.setScaleY(1.0);
                            blurFx.setRadius(0);
                            colorShift.setHue(0);
                            colorShift.setSaturation(0);
                            pointer.setRotate(0);
                        })
                );

                SequentialTransition seq = new SequentialTransition(
                        dim,
                        new ParallelTransition(camZoom, jumpscareAppear, screamDelay),
                        new PauseTransition(Duration.millis(50)),
                        new Transition() {
                            @Override
                            protected void interpolate(double v) {

                            }

                            { setCycleDuration(Duration.millis(1)); setOnFinished(ev -> { flashes.run(); glitch.run(); }); }},
                        pointerTwitch,
                        shake1, shake2, hold, undim
                );
                seq.setOnFinished(e -> fadeOutAndStop(rumblePlayer, Duration.millis(180)));
                seq.play();

            } else {
                // Fallback sin GIF
                flashStrobe(3, 40, 70);
                microGlitch(Duration.millis(260));
                TranslateTransition shake = new TranslateTransition(Duration.millis(40 * 6), wheelContainer);
                shake.setFromX(0); shake.setByX(14); shake.setAutoReverse(true); shake.setCycleCount(6);
                SequentialTransition seq = new SequentialTransition(
                        dim, shake, new PauseTransition(Duration.millis(280))
                );
                seq.setOnFinished(e -> {
                    darkOverlay.setVisible(false);
                    vignette.setVisible(false);
                    fadeOutAndStop(rumblePlayer, Duration.millis(180));
                });
                seq.play();
            }
        } catch (Exception ex) {
            darkOverlay.setVisible(false);
            vignette.setVisible(false);
            fadeOutAndStop(rumblePlayer, Duration.millis(180));
        }
    }

    // ===== TRATO: fantasma + HIMNO (se escucha, sin scream) =====
    private void runTratoFantasmaConHimno() {
        // NO paramos el himno aquí; lo reproducimos y se seguirá oyendo hasta el próximo giro.
        // Pero sí paramos el resto para evitar solapes.

        fadeOutAndStop(screamPlayer, Duration.millis(100));
        fadeOutAndStop(rumblePlayer, Duration.millis(100));

        // Luz tenue para ambiente
        darkOverlay.setVisible(true);
        Timeline dim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(darkOverlay.fillProperty(), Color.color(0,0,0,0.0))),
                new KeyFrame(Duration.millis(220), new KeyValue(darkOverlay.fillProperty(), Color.color(0,0,0,0.25)))
        );

        // Reproducir HIMNO (audible). No loop por defecto; si quieres loop, descomenta:
        // if (hymnPlayer != null) hymnPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        if (hymnPlayer != null) {
            try { hymnPlayer.setVolume(0.85); } catch (Exception ignored) {}
            playSafe(hymnPlayer);
        }

        try {
            URL ghostUrl = getClass().getResource("jumpscare2.gif");
            if (ghostUrl != null) {
                Image ghost = new Image(ghostUrl.toExternalForm(), 380, 380, true, true);
                ghostView.setImage(ghost);
                ghostView.setOpacity(0);
                ghostView.setVisible(true);
                ghostView.setLayoutX((wheelContainer.getWidth() - 380) / 2);
                ghostView.setLayoutY(-420);

                TranslateTransition drop = new TranslateTransition(Duration.millis(500), ghostView);
                drop.setFromY(0);
                drop.setToY(520);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(320), ghostView);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                ScaleTransition zoom = new ScaleTransition(Duration.millis(320), ghostView);
                ghostView.setScaleX(1.0);
                ghostView.setScaleY(1.0);
                zoom.setToX(1.06);
                zoom.setToY(1.06);

                ParallelTransition ghostAppear = new ParallelTransition(drop, fadeIn, zoom);

                // Temblor leve simpático
                TranslateTransition shake1 = makeShake(wheelContainer, 10, 3, 6);
                TranslateTransition shake2 = makeShake(wheelContainer, 6, 3, 6);

                PauseTransition hold = new PauseTransition(Duration.millis(900));
                Timeline undim = new Timeline(
                        new KeyFrame(Duration.millis(400), e -> {
                            darkOverlay.setVisible(false);
                            ghostView.setVisible(false);
                            ghostView.setTranslateY(0);
                        })
                );

                SequentialTransition seq = new SequentialTransition(
                        dim, ghostAppear, shake1, shake2, hold, undim
                );
                seq.setOnFinished(e -> showResultMessage("¡TRATO con himno! 🎵👻"));
                seq.play();

            } else {
                // Sin imagen: dim + mensaje
                SequentialTransition seq = new SequentialTransition(
                        dim, new PauseTransition(Duration.millis(400))
                );
                seq.setOnFinished(e -> {
                    darkOverlay.setVisible(false);
                    showResultMessage("¡TRATO con himno! 🎵");
                });
                seq.play();
            }
        } catch (Exception ex) {
            darkOverlay.setVisible(false);
            showResultMessage("¡TRATO con himno! 🎵");
        }
    }

    // ===== Utilidades =====
    private TranslateTransition makeShake(Pane node, double amplitude, int cycles, int steps) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(40 * steps), node);
        shake.setFromX(0);
        shake.setByX(amplitude);
        shake.setAutoReverse(true);
        shake.setCycleCount(cycles);
        return shake;
    }

    private void flashStrobe(int times, double onMs, double offMs) {
        strobe.setVisible(true);
        List<KeyFrame> frames = new ArrayList<>();
        double t = 0;
        for (int i = 0; i < times; i++) {
            frames.add(new KeyFrame(Duration.millis(t), new KeyValue(strobe.opacityProperty(), 0.0)));
            t += 1;
            frames.add(new KeyFrame(Duration.millis(t), new KeyValue(strobe.opacityProperty(), 1.0)));
            t += onMs;
            frames.add(new KeyFrame(Duration.millis(t), new KeyValue(strobe.opacityProperty(), 0.0)));
            t += offMs;
        }
        Timeline tl = new Timeline(frames.toArray(new KeyFrame[0]));
        tl.setOnFinished(e -> { strobe.setVisible(false); strobe.setOpacity(0.0); });
        tl.play();
    }

    private void microGlitch(Duration total) {
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(blurFx.radiusProperty(), 0),
                        new KeyValue(colorShift.hueProperty(), 0),
                        new KeyValue(colorShift.saturationProperty(), 0)
                ),
                new KeyFrame(total.multiply(0.25),
                        new KeyValue(blurFx.radiusProperty(), 8),
                        new KeyValue(colorShift.hueProperty(), 0.2),
                        new KeyValue(colorShift.saturationProperty(), 0.3)
                ),
                new KeyFrame(total.multiply(0.5),
                        new KeyValue(blurFx.radiusProperty(), 2),
                        new KeyValue(colorShift.hueProperty(), -0.15),
                        new KeyValue(colorShift.saturationProperty(), 0.15)
                ),
                new KeyFrame(total,
                        new KeyValue(blurFx.radiusProperty(), 0),
                        new KeyValue(colorShift.hueProperty(), 0),
                        new KeyValue(colorShift.saturationProperty(), 0)
                )
        );
        tl.play();
    }

    private void showResultMessage(String message) {
        resultLabel.setText(message);
        if (!resultLabel.getStyleClass().contains("celebration")) {
            resultLabel.getStyleClass().add("celebration");
        }
    }

    private void loadOptionalMedia() {
        try {
            URL thunder = getClass().getResource("sfx/scream2.mp3");
            if (thunder != null) thunderPlayer = new MediaPlayer(new Media(thunder.toExternalForm()));
        } catch (Exception ignored) {}
        try {
            URL scream = getClass().getResource("sfx/scream.mp3");
            if (scream != null) {
                screamPlayer = new MediaPlayer(new Media(scream.toExternalForm()));
                screamPlayer.setVolume(0.9);
            }
        } catch (Exception ignored) {}
        try {
            URL rumble = getClass().getResource("sfx/rumble.mp3");
            if (rumble != null) {
                rumblePlayer = new MediaPlayer(new Media(rumble.toExternalForm()));
                rumblePlayer.setVolume(0.4);
            }
        } catch (Exception ignored) {}
        try {
            URL hymn = getClass().getResource("sfx/hymn.mp3");   // << recurso del HIMNO
            if (hymn != null) {
                hymnPlayer = new MediaPlayer(new Media(hymn.toExternalForm()));
                hymnPlayer.setVolume(0.85);
                // hymnPlayer.setCycleCount(MediaPlayer.INDEFINITE); // descomenta si quieres loop
            }
        } catch (Exception ignored) {}
    }

    private void playSafe(MediaPlayer player) {
        if (player == null) return;
        try {
            player.stop();
            player.seek(Duration.ZERO);
            player.play();
        } catch (Exception ignored) {}
    }
}
