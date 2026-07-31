package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.RideHistory;
import storage.dao.RideHistoryDAO;

import java.util.List;
import java.util.Optional;

public class HistoryController {

    // ── FXML Elements ────────────────────────────────────
    @FXML private ListView<String> historyListView;
    @FXML private Label            totalRidesLabel;
    @FXML private Label            statusLabel;
    @FXML private Button           clearBtn;
    @FXML private Button           refreshBtn;

    // ── DAO ──────────────────────────────────────────────
    private RideHistoryDAO rideHistoryDAO;

    // ── Initialize ───────────────────────────────────────
    @FXML
    public void initialize() {
        rideHistoryDAO = new RideHistoryDAO();
        loadHistory();
    }

    // ── Load History from MySQL ──────────────────────────
    private void loadHistory() {
        historyListView.getItems().clear();

        List<RideHistory> history =
            rideHistoryDAO.getAllRides();

        if (history.isEmpty()) {
            historyListView.getItems().add(
                "No ride history found."
            );
            clearBtn.setDisable(true);
            statusLabel.setText("No rides yet!");
        } else {
            for (RideHistory ride : history) {
                historyListView.getItems().add(
                    formatRide(ride)
                );
            }
            clearBtn.setDisable(false);
            statusLabel.setText("Loaded successfully!");
        }

        // update total count
        totalRidesLabel.setText(
            String.valueOf(history.size())
        );
    }

    // ── Format Ride for Display ──────────────────────────
    private String formatRide(RideHistory ride) {
        String timeType = "";
        if (ride.isNight()) {
            timeType = "🌙 Night";
        } else if (ride.isPeak()) {
            timeType = "🚦 Peak";
        } else {
            timeType = "☀️ Normal";
        }

        return String.format(
            "📍 %s  →  %s%n" +
            "    📏 %.1f km  |  💰 %.0f BDT" +
            "  |  %s  |  🕐 %s",
            ride.getFrom(),
            ride.getTo(),
            ride.getDistance(),
            ride.getFare(),
            timeType,
            ride.getRideTime()
        );
    }

    // ── Refresh Button ───────────────────────────────────
    @FXML
    public void onRefresh() {
        loadHistory();
        statusLabel.setText("✅ Refreshed!");
    }

    // ── Clear All Button ─────────────────────────────────
    @FXML
    public void onClearAll() {
        // confirm before deleting
        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION
        );
        confirm.setTitle("Clear History");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText(
            "This will permanently delete " +
            "all ride history!"
        );

        Optional<ButtonType> result =
            confirm.showAndWait();

        if (result.isPresent() &&
                result.get() == ButtonType.OK) {
            rideHistoryDAO.clearAllRides();
            loadHistory();
            statusLabel.setText("🗑️ History cleared!");
        }
    }

    // ── Back Button ──────────────────────────────────────
    @FXML
    public void onBack() {
        Stage stage = (Stage) historyListView
            .getScene().getWindow();
        stage.close();
    }
}