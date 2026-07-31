module RickshawGo {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // MySQL connector
    requires java.sql;

    // Export packages to JavaFX
    opens application to javafx.graphics, javafx.fxml;
    opens ui          to javafx.fxml;
    opens model       to javafx.fxml;
    opens algorithm   to javafx.fxml;
    opens storage     to javafx.fxml;
    opens storage.dao to javafx.fxml;
}