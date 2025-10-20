package org.example.interfazfx;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class RuletaController {
    private static final double CANVAS_SIZE = 320.0;
    private static final double WHEEL_RADIUS = CANVAS_SIZE / 2 - 16;
    private static final double POINTER_WIDTH = 52.0;
    private static final double POINTER_HEIGHT = 74.0;

    private static final List<String> SECTIONS = List.of(
            "TRUCO",
            "TRATO"
    );

    private static final List<Color> SECTION_COLORS = List.of(
            Color.web("#A31B00"),
            Color.web("#5E2400")
    );

    @FXML
    private Pane wheelContainer;
    @FXML
    private Button spinButton;
    @FXML
    private Label resultLabel;

    private Canvas wheelCanvas;
    private Polygon pointer;
    private Circle hub;
    private double currentRotation;
    private final Random random = new Random();

    @FXML
    public void initialize() {
        configureWheelContainer();
        drawWheel();
        spinButton.setOnAction(event -> spinWheel());
    }

    private void configureWheelContainer() {
        double containerSize = CANVAS_SIZE + 120;
        wheelContainer.setPrefSize(containerSize, containerSize);
        wheelContainer.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        wheelContainer.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        double offset = (containerSize - CANVAS_SIZE) / 2;

        wheelCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        wheelCanvas.setLayoutX(offset);
        wheelCanvas.setLayoutY(offset);
        DropShadow canvasShadow = new DropShadow(35, Color.color(0, 0, 0, 0.85));
        canvasShadow.setSpread(0.25);
        wheelCanvas.setEffect(canvasShadow);

        pointer = new Polygon();
        pointer.getStyleClass().add("wheel-pointer");
        pointer.setStrokeLineJoin(StrokeLineJoin.ROUND);
        pointer.setMouseTransparent(true);
        updatePointerShape(offset);

        hub = new Circle();
        hub.getStyleClass().add("wheel-hub");
        hub.setRadius(28);
        hub.setMouseTransparent(true);
        hub.setLayoutX(offset + CANVAS_SIZE / 2);
        hub.setLayoutY(offset + CANVAS_SIZE / 2);

        wheelContainer.getChildren().setAll(wheelCanvas, pointer, hub);
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
        pointer.setEffect(new DropShadow(20, Color.color(0, 0, 0, 0.9)));
    }

    private void drawWheel() {
        GraphicsContext gc = wheelCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        double center = CANVAS_SIZE / 2;
        double radius = WHEEL_RADIUS;
        double anglePerSection = 360.0 / SECTIONS.size();
        double startAngle = 90 - anglePerSection / 2;

        gc.setFill(Color.web("#1a0c22"));
        gc.fillOval(center - radius - 22, center - radius - 22, (radius + 22) * 2, (radius + 22) * 2);

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

        Font font = Font.font("Cinzel", FontWeight.SEMI_BOLD, 16);
        gc.setFont(font);
        gc.setFill(Color.web("#fbf5ff"));
        Text helper = new Text(textValue);
        helper.setFont(font);
        double textWidth = helper.getLayoutBounds().getWidth();
        gc.fillText(textValue, -textWidth / 2, 6);

        gc.restore();
    }

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

    private void revealResult() {
        double anglePerSection = 360.0 / SECTIONS.size();
        double normalized = (360 - currentRotation) % 360;
        if (normalized < 0) {
            normalized += 360;
        }
        double adjusted = (normalized + anglePerSection / 2) % 360;
        int index = (int) (adjusted / anglePerSection) % SECTIONS.size();

        String selection = SECTIONS.get(index);
        resultLabel.setText("El destino revela: " + selection + ".");
        if (!resultLabel.getStyleClass().contains("celebration")) {
            resultLabel.getStyleClass().add("celebration");
        }
    }
}
