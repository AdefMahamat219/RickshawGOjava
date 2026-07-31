package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import algorithm.MapData;
import model.Location;
import storage.dao.SavedLocationDAO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SavedController {

    // ── FXML Elements ────────────────────────────────────
    @FXML private TextField            nameField;
    @FXML private ComboBox<String>     nodeCombo;
    @FXML private ListView<String>     savedListView;
    @FXML private Label                totalSavedLabel;
    @FXML private Label                statusLabel;
    @FXML private Button               addBtn;
    @FXML private Button               deleteBtn;
    @FXML private Button               clearAllBtn;

    // ── DAO ──────────────────────────────────────────────
    private SavedLocationDAO savedLocationDAO;

    // ── Locations Map ────────────────────────────────────
    private Map<String, Location> locations;

    // ── Initialize ───────────────────────────────────────
    @FXML
    public void initialize() {
        savedLocationDAO = new SavedLocationDAO();
        locations        = MapData.getLocations();

        // fill node ComboBox with location names
        for (Map.Entry<String, Location> entry
                : locations.entrySet()) {
            nodeCombo.getItems().add(entry.getKey());
        }

        // show names in ComboBox
        nodeCombo.setConverter(
            new javafx.util.StringConverter<String>() {
                @Override
                public String toString(String id) {
                    return id == null ? "" :
                        locations.get(id).getName();
                }
                @Override
                public String fromString(String s) {
                    return s;
                }
            }
        );

        // enable delete button when item selected
        savedListView.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> {
                deleteBtn.setDisable(newVal == null);
            });

        // load saved locations
        loadSavedLocations();
    }

    // ── Load Saved Locations from MySQL ──────────────────
    private void loadSavedLocations() {
        savedListView.getItems().clear();

        List<String[]> savedList =
            savedLocationDAO.getAllLocations();

        if (savedList.isEmpty()) {
            savedListView.getItems().add(
                "No saved locations yet."
            );
            clearAllBtn.setDisable(true);
            statusLabel.setText("No locations saved!");
        } else {
            for (String[] loc : savedList) {
                // loc[0] = id
                // loc[1] = name (label)
                // loc[2] = nodeId
                String locationName = locations
                    .containsKey(loc[2])
                    ? locations.get(loc[2]).getName()
                    : loc[2];

                savedListView.getItems().add(
                    String.format("⭐ %s  →  %s",
                        loc[1], locationName)
                );
            }
            clearAllBtn.setDisable(false);
            statusLabel.setText("Loaded successfully!");
        }

        // update total count
        totalSavedLabel.setText(
            String.valueOf(savedList.size())
        );
    }

    // ── Add Location Button ──────────────────────────────
    @FXML
    public void onAddLocation() {
        String name   = nameField.getText().trim();
        String nodeId = nodeCombo.getValue();

        // validate inputs
        if (name.isEmpty()) {
            showAlert("⚠️ Warning",
                "Please enter a label name!");
            return;
        }

        if (nodeId == null) {
            showAlert("⚠️ Warning",
                "Please select a location!");
            return;
        }

        // check if name already exists
        if (savedLocationDAO.locationExists(name)) {
            showAlert("⚠️ Warning",
                "A saved location with this " +
                "name already exists!");
            return;
        }

        // save to MySQL
        savedLocationDAO.saveLocation(name, nodeId);

        // clear inputs
        nameField.clear();
        nodeCombo.setValue(null);

        // reload list
        loadSavedLocations();
        statusLabel.setText(
            "✅ Location saved: " + name
        );
    }

    // ── Delete Selected Button ───────────────────────────
    @FXML
    public void onDeleteSelected() {
        String selected = savedListView
            .getSelectionModel()
            .getSelectedItem();

        if (selected == null ||
                selected.equals(
                    "No saved locations yet.")) {
            showAlert("⚠️ Warning",
                "Please select a location to delete!");
            return;
        }

        // confirm deletion
        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION
        );
        confirm.setTitle("Delete Location");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText(
            "Delete: " + selected + "?"
        );

        Optional<ButtonType> result =
            confirm.showAndWait();

        if (result.isPresent() &&
                result.get() == ButtonType.OK) {
            // extract label name from display string
            // format: "⭐ Home  →  IUT Main Gate"
            String labelName = selected
                .replace("⭐ ", "")
                .split("  →  ")[0]
                .trim();

            savedLocationDAO
                .deleteLocationByName(labelName);
            loadSavedLocations();
            statusLabel.setText(
                "🗑️ Deleted: " + labelName
            );
        }
    }

    // ── Clear All Button ─────────────────────────────────
    @FXML
    public void onClearAll() {
        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION
        );
        confirm.setTitle("Clear All");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText(
            "This will delete ALL saved locations!"
        );

        Optional<ButtonType> result =
            confirm.showAndWait();

        if (result.isPresent() &&
                result.get() == ButtonType.OK) {
            savedLocationDAO.clearAllLocations();
            loadSavedLocations();
            statusLabel.setText(
                "🗑️ All locations cleared!"
            );
        }
    }

    // ── Refresh Button ───────────────────────────────────
    @FXML
    public void onRefresh() {
        loadSavedLocations();
        statusLabel.setText("✅ Refreshed!");
    }

    // ── Back Button ──────────────────────────────────────
    @FXML
    public void onBack() {
        Stage stage = (Stage) savedListView
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