package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import storage.DBConnection;
import storage.dao.SettingsDAO;
import model.FareCalculator;

public class Main extends Application {

    private static final String TITLE  = "🛺 RickshawGo";
    private static final double WIDTH  = 1000;
    private static final double HEIGHT = 650;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Step 1 — test DB connection
            if (!DBConnection.testConnection()) {
                showDBError();
                return;
            }

            // Step 2 — initialize settings
            SettingsDAO settingsDAO =
                new SettingsDAO();
            settingsDAO.initializeSettings();
            double[] settings =
                settingsDAO.getSettings();
            FareCalculator.updateRates(
                settings[0], settings[1],
                settings[2], settings[3],
                (int) settings[4], (int) settings[5]);

            // Step 3 — load WELCOME screen first
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/ui/WelcomeScreen.fxml"));
            Parent root = loader.load();

            // Step 4 — create scene
            Scene scene = new Scene(
                root, WIDTH, HEIGHT);

            // Step 5 — configure stage
            primaryStage.setTitle(TITLE);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Step 6 — handle close
            primaryStage.setOnCloseRequest(
                event -> {
                    DBConnection.closeConnection();
                    System.out.println(
                        "👋 Goodbye!");
                });

            // Step 7 — show
            primaryStage.show();

            System.out.println(
                "✅ RickshawGo started!");

        } catch (Exception e) {
            e.printStackTrace();
            showLoadError();
        }
    }

    @Override
    public void init() {
        System.out.println(
            "🔄 Initializing RickshawGo...");
        System.out.println(
            "🔌 Connecting to database...");
    }

    @Override
    public void stop() {
        System.out.println(
            "🔒 Closing connection...");
        DBConnection.closeConnection();
        System.out.println("👋 Goodbye!");
    }

    private void showDBError() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(
            "Cannot connect to MySQL!");
        alert.setContentText(
            "Please make sure:\n" +
            "1. MySQL server is running\n" +
            "2. Database 'rickshawgo' exists\n" +
            "3. Username & password are correct\n" +
            "4. MySQL Connector JAR is added"
        );
        alert.showAndWait();
    }

    private void showLoadError() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Load Error");
        alert.setHeaderText(
            "Cannot load application!");
        alert.setContentText(
            "Please make sure:\n" +
            "1. WelcomeScreen.fxml exists\n" +
            "2. style.css exists\n" +
            "3. JavaFX is configured correctly"
        );
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
