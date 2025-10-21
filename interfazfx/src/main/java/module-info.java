module org.example.interfazfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media; // necesario para Media y MediaPlayer

    opens org.example.interfazfx to javafx.graphics, javafx.fxml;
    exports org.example.interfazfx;
}
