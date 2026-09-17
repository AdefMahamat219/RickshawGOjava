package ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WelcomeController {

    @FXML private ImageView rickshawImage;
    @FXML private Button    startBtn;

    // ── Initialize ───────────────────────────────────────
    @FXML
    public void initialize() {
        // load rickshaw image
        loadRickshawImage();

        // play entrance animations
        playAnimations();
    }

    // ── Load Rickshaw Image ──────────────────────────────
    private void loadRickshawImage() {
        try {
            // load from images folder
            Image image = new Image(
                getClass().getResourceAsStream(
                    "/images/rickshaw.png")
            );
            rickshawImage.setImage(image);
        } catch (Exception e) {
            // if image not found show emoji fallback
            System.out.println(
                "⚠️ Rickshaw image not found!" +
                " Using emoji fallback.");
        }
    }

    // ── Play Entrance Animations ─────────────────────────
    private void playAnimations() {
        // fade in image from top
        TranslateTransition slideDown =
            new TranslateTransition(
                Duration.millis(800),
                rickshawImage);
        slideDown.setFromY(-50);
        slideDown.setToY(0);

        FadeTransition fadeIn =
            new FadeTransition(
                Duration.millis(800),
                rickshawImage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // scale button
        ScaleTransition scalePulse =
            new ScaleTransition(
                Duration.millis(1000),
                startBtn);
        scalePulse.setFromX(0.95);
        scalePulse.setFromY(0.95);
        scalePulse.setToX(1.0);
        scalePulse.setToY(1.0);
        scalePulse.setCycleCount(
            ScaleTransition.INDEFINITE);
        scalePulse.setAutoReverse(true);

        slideDown.play();
        fadeIn.play();

        // start pulse after delay
        javafx.animation.PauseTransition pause =
            new javafx.animation.PauseTransition(
                Duration.millis(800));
        pause.setOnFinished(e -> scalePulse.play());
        pause.play();
    }

 // ── Start Button ─────────────────────────────────────
    @FXML
    public void onStart() {
        try {
            // fade out welcome screen
            FadeTransition fadeOut =
                new FadeTransition(
                    Duration.millis(400),
                    startBtn.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                try {
                    // load MAP screen directly
                    FXMLLoader loader =
                        new FXMLLoader(
                            getClass().getResource(
                                "/ui/MapWebScreen.fxml"
                            ));
                    Parent root = loader.load();

                    Scene scene = new Scene(
                        root, 1100, 720);
                    scene.getStylesheets().add(
                        getClass().getResource(
                            "/ui/style.css")
                            .toExternalForm());

                    Stage stage = (Stage) startBtn
                        .getScene().getWindow();

                    stage.setResizable(true);
                    stage.setScene(scene);
                    stage.setWidth(1100);
                    stage.setHeight(720);
                    stage.centerOnScreen();

                    // fade in map screen
                    FadeTransition fadeIn =
                        new FadeTransition(
                            Duration.millis(400),
                            root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}