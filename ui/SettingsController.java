package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.FareCalculator;
import storage.dao.SettingsDAO;

public class SettingsController {

    // ── FXML Elements ────────────────────────────────────
    @FXML private TextField baseRateField;
    @FXML private TextField perKmRateField;
    @FXML private TextField nightMultField;
    @FXML private TextField peakMultField;
    @FXML private TextField nightStartField;
    @FXML private TextField nightEndField;
    @FXML private Label     statusLabel;

    // ── DAO ──────────────────────────────────────────────
    private SettingsDAO settingsDAO;

    // ── Initialize ───────────────────────────────────────
    @FXML
    public void initialize() {
        settingsDAO = new SettingsDAO();
        loadSettings();
    }

    // ── Load Settings from MySQL ─────────────────────────
    private void loadSettings() {
        double[] settings = settingsDAO.getSettings();

        baseRateField.setText(
            String.valueOf(settings[0]));
        perKmRateField.setText(
            String.valueOf(settings[1]));
        nightMultField.setText(
            String.valueOf(settings[2]));
        peakMultField.setText(
            String.valueOf(settings[3]));
        nightStartField.setText(
            String.valueOf((int) settings[4]));
        nightEndField.setText(
            String.valueOf((int) settings[5]));

        statusLabel.setText("✅ Settings loaded!");
    }

    // ── Save Settings Button ─────────────────────────────
    @FXML
    public void onSaveSettings() {
        // validate all fields
        if (!validateFields()) return;

        try {
            double baseRate   =
                Double.parseDouble(
                    baseRateField.getText().trim());
            double perKmRate  =
                Double.parseDouble(
                    perKmRateField.getText().trim());
            double nightMult  =
                Double.parseDouble(
                    nightMultField.getText().trim());
            double peakMult   =
                Double.parseDouble(
                    peakMultField.getText().trim());
            int nightStart    =
                Integer.parseInt(
                    nightStartField.getText().trim());
            int nightEnd      =
                Integer.parseInt(
                    nightEndField.getText().trim());

            // validate ranges
            if (!validateRanges(baseRate, perKmRate,
                    nightMult, peakMult,
                    nightStart, nightEnd)) return;

            // save to MySQL
            settingsDAO.saveSettings(
                baseRate, perKmRate,
                nightMult, peakMult,
                nightStart, nightEnd
            );

            // update FareCalculator
            FareCalculator.updateRates(
                baseRate, perKmRate,
                nightMult, peakMult
            );

            statusLabel.setText(
                "✅ Settings saved successfully!"
            );

        } catch (NumberFormatException e) {
            showAlert("❌ Error",
                "Please enter valid numbers only!");
        }
    }

    // ── Reset to Default Button ──────────────────────────
    @FXML
    public void onResetDefault() {
        settingsDAO.resetToDefault();
        loadSettings();
        statusLabel.setText(
            "🔄 Settings reset to default!"
        );
    }

    // ── Validate Fields Not Empty ────────────────────────
    private boolean validateFields() {
        if (baseRateField.getText().trim().isEmpty()   ||
            perKmRateField.getText().trim().isEmpty()  ||
            nightMultField.getText().trim().isEmpty()  ||
            peakMultField.getText().trim().isEmpty()   ||
            nightStartField.getText().trim().isEmpty() ||
            nightEndField.getText().trim().isEmpty()) {

            showAlert("⚠️ Warning",
                "Please fill in all fields!");
            return false;
        }
        return true;
    }

    // ── Validate Value Ranges ────────────────────────────
    private boolean validateRanges(double baseRate,
                                   double perKmRate,
                                   double nightMult,
                                   double peakMult,
                                   int nightStart,
                                   int nightEnd) {
        if (baseRate <= 0 || perKmRate <= 0) {
            showAlert("⚠️ Warning",
                "Rates must be greater than 0!");
            return false;
        }
        if (nightMult < 1.0 || peakMult < 1.0) {
            showAlert("⚠️ Warning",
                "Multipliers must be 1.0 or greater!");
            return false;
        }
        if (nightStart < 0 || nightStart > 23 ||
            nightEnd   < 0 || nightEnd   > 23) {
            showAlert("⚠️ Warning",
                "Hours must be between 0 and 23!");
            return false;
        }
        return true;
    }

    // ── Back Button ──────────────────────────────────────
    @FXML
    public void onBack() {
        Stage stage = (Stage) baseRateField
            .getScene().getWindow();
        stage.close();
    }

    // ── Show Alert ───────────────────────────────────────
    private void showAlert(String title,
                           String message) {
        Alert alert = new Alert(
            Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}