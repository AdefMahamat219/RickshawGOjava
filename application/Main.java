package application;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import storage.DBConnection;


public class Main extends Application {

    // ── Window Settings ──────────────────────────────────
    private static final String TITLE  = "🛺 RickshawGo";
    private static final double WIDTH  = 900;
    private static final double HEIGHT = 650;

    // ── Start Method ─────────────────────────────────────
    @Override
    public void start(Stage primaryStage) {
        try {
            // Step 1 — Test database connection
            if (!DBConnection.testConnection()) {
                showDBError();
                return;
            }

            // Step 2 — Load main screen FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/MapScreen.fxml")
            );
            Parent root = loader.load();

            // Step 3 — Create scene
            Scene scene = new Scene(root, WIDTH, HEIGHT);

            // Step 4 — Load CSS styling
            scene.getStylesheets().add(
                getClass()
                    .getResource("/ui/style.css")
                    .toExternalForm()
            );

            // Step 5 — Configure stage
            primaryStage.setTitle(TITLE);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // Step 6 — Handle window close
            primaryStage.setOnCloseRequest(event -> {
                DBConnection.closeConnection();
                System.out.println("👋 App closed. Goodbye!");
            });

            // Step 7 — Show window
            primaryStage.show();

            System.out.println("✅ RickshawGo started successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showLoadError();
        }
    }

    // ── Init Method (runs before start) ──────────────────
    @Override
    public void init() {
        System.out.println("🔄 Initializing RickshawGo...");
        System.out.println("🔌 Connecting to database...");
    }

    // ── Stop Method (runs when app closes) ───────────────
    @Override
    public void stop() {
        System.out.println("🔒 Closing database connection...");
        DBConnection.closeConnection();
        System.out.println("👋 RickshawGo closed. Goodbye!");
    }

    // ── Show Database Error ──────────────────────────────
    private void showDBError() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText("Cannot connect to MySQL!");
        alert.setContentText(
            "Please make sure:\n" +
            "1. MySQL server is running\n" +
            "2. Database 'rickshawgo' exists\n" +
            "3. Username & password are correct\n" +
            "4. MySQL Connector JAR is added"
        );
        alert.showAndWait();
    }

    // ── Show Load Error ──────────────────────────────────
    private void showLoadError() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Load Error");
        alert.setHeaderText("Cannot load application!");
        alert.setContentText(
            "Please make sure:\n" +
            "1. MapScreen.fxml exists in ui package\n" +
            "2. style.css exists in ui package\n" +
            "3. JavaFX is configured correctly"
        );
        alert.showAndWait();
    }

    // ── Main Entry Point ─────────────────────────────────
    public static void main(String[] args) {
        launch(args);
    }
}