module com.quoridor {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires javafx.graphics;
    requires java.desktop;
    opens com.quoridor to javafx.fxml, javafx.graphics;
    exports com.quoridor;
}
