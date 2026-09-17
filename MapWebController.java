package ui;

import algorithm.Dijkstra;
import algorithm.Graph;
import algorithm.MapData;
import algorithm.RoadData;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.FareCalculator;
import model.Location;
import model.RideHistory;
//✅ Correct
import netscape.javascript.JSObject;
//AND add this to VM arguments
import storage.dao.RideHistoryDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MapWebController {

    // ── FXML Elements ────────────────────────────────────
    @FXML private WebView          mapWebView;
    @FXML private ComboBox<String> fromCombo;
    @FXML private ComboBox<String> toCombo;
    @FXML private Button           findRouteBtn;
    @FXML private Button           clearBtn;
    @FXML private Button           saveBtn;
    @FXML private Button           historyBtn;
    @FXML private Button           savedBtn;
    @FXML private Button           settingsBtn;
    @FXML private Label            distanceLabel;
    @FXML private Label            fareLabel;
    @FXML private Label            timeLabel;
    @FXML private Label            routeLabel;
    @FXML private Label            fromSelectedLabel;
    @FXML private Label            toSelectedLabel;

    // ── Data ─────────────────────────────────────────────
    private Map<String, Location> locations;
    private Graph                 graph;
    private List<String>          currentPath;
    private double                currentDistance;
    private double                currentFare;
    private boolean               currentIsNight;
    private boolean               currentIsPeak;
    private WebEngine             webEngine;

    // ── DAO ──────────────────────────────────────────────
    private RideHistoryDAO rideHistoryDAO;

    // ── Initialize ───────────────────────────────────────
    @FXML
    public void initialize() {
        rideHistoryDAO = new RideHistoryDAO();
        locations      = MapData.getLocations();
        graph          = RoadData.buildGraph();

        // fill ComboBoxes
        for (String key : locations.keySet()) {
            fromCombo.getItems().add(key);
            toCombo.getItems().add(key);
        }

        // show names in ComboBox
        StringConverter<String> converter =
            new StringConverter<String>() {
                @Override
                public String toString(String id) {
                    if (id == null) return "";
                    Location loc = locations.get(id);
                    return loc != null ?
                        loc.getName() : id;
                }
                @Override
                public String fromString(String s) {
                    return s;
                }
            };
        fromCombo.setConverter(converter);
        toCombo.setConverter(converter);

        // load map
        loadMap();
    }

    // ── Load Real Map ────────────────────────────────────
    private void loadMap() {
        webEngine = mapWebView.getEngine();

        // enable JavaScript
        webEngine.setJavaScriptEnabled(true);

        // load map.html
        String mapUrl = getClass()
            .getResource("/ui/map.html")
            .toExternalForm();
        webEngine.load(mapUrl);

        // wait for map to load then inject
        // Java connector
        webEngine.getLoadWorker()
            .stateProperty()
            .addListener(
                (ChangeListener<Worker.State>)
                (obs, oldState, newState) -> {
                    if (newState ==
                            Worker.State.SUCCEEDED) {
                        injectJavaConnector();
                    }
                });
    }

    // ── Inject Java Connector into JavaScript ────────────
    private void injectJavaConnector() {
        // create bridge between JS and Java
        JSObject window = (JSObject)
            webEngine.executeScript("window");
        window.setMember(
            "javaConnector", new JavaConnector());

        System.out.println(
            "✅ Java connector injected!");
    }

    // ── Java Connector (JS → Java bridge) ────────────────
    public class JavaConnector {

        // called when user clicks a marker on map
        public void onLocationClicked(
                String id, String name) {
            javafx.application.Platform
                .runLater(() -> {
                    // set from first then to
                    if (fromCombo.getValue()
                            == null) {
                        fromCombo.setValue(id);
                        fromSelectedLabel.setText(
                            "📍 " + name);
                        // highlight green on map
                        webEngine.executeScript(
                            "window.mapApi" +
                            ".highlightLocation('" +
                            id + "', 'from')");
                    } else if (
                            toCombo.getValue()
                            == null) {
                        toCombo.setValue(id);
                        toSelectedLabel.setText(
                            "📍 " + name);
                        // highlight red on map
                        webEngine.executeScript(
                            "window.mapApi" +
                            ".highlightLocation('" +
                            id + "', 'to')");
                    }
                });
        }
    }

    // ── Find Route Button ────────────────────────────────
    @FXML
    public void onFindRoute() {
        String fromId = fromCombo.getValue();
        String toId   = toCombo.getValue();

        if (fromId == null || toId == null) {
            showAlert("⚠️ Warning",
                "Please select both locations!");
            return;
        }
        if (fromId.equals(toId)) {
            showAlert("⚠️ Warning",
                "Start and destination cannot " +
                "be the same!");
            return;
        }

        // run Dijkstra
        currentDistance = Dijkstra
            .findShortestDistance(
                graph, fromId, toId);
        currentPath = Dijkstra
            .findShortestPath(
                graph, fromId, toId);

        if (currentDistance == -1 ||
                currentPath.isEmpty()) {
            showAlert("❌ No Route",
                "No route found!");
            return;
        }

        // detect time
        currentIsNight = FareCalculator
            .isNightTime();
        currentIsPeak  = FareCalculator
            .isPeakHour();

        // calculate fare
        currentFare = FareCalculator.calculate(
            currentDistance,
            currentIsNight,
            currentIsPeak
        );

        // update labels
        distanceLabel.setText(String.format(
            "%.1f km", currentDistance));
        fareLabel.setText(String.format(
            "%.0f BDT", currentFare));

        if (currentIsNight) {
            timeLabel.setText("🌙 Night (×1.5)");
        } else if (currentIsPeak) {
            timeLabel.setText("🚦 Peak (×1.2)");
        } else {
            timeLabel.setText("☀️ Normal Rate");
        }

        routeLabel.setText(buildRouteText());

        // draw route on real map
        drawRouteOnMap(fromId, toId);

        saveBtn.setDisable(false);
    }

    // ── Draw Route on Real Map ───────────────────────────
    private void drawRouteOnMap(
            String fromId, String toId) {
        if (currentPath == null ||
                currentPath.isEmpty()) return;

        // build JS array of path IDs
        StringBuilder pathArray =
            new StringBuilder("[");
        for (int i = 0;
                i < currentPath.size(); i++) {
            pathArray.append("'")
                     .append(currentPath.get(i))
                     .append("'");
            if (i < currentPath.size() - 1) {
                pathArray.append(",");
            }
        }
        pathArray.append("]");

        // call JS drawRoute function
        String js = String.format(
            "window.mapApi.drawRoute('%s','%s',%s)",
            fromId, toId,
            pathArray.toString()
        );
        webEngine.executeScript(js);
    }

    // ── Build Route Text ─────────────────────────────────
    private String buildRouteText() {
        if (currentPath == null ||
                currentPath.isEmpty()) return "--";
        StringBuilder sb = new StringBuilder();
        for (int i = 0;
                i < currentPath.size(); i++) {
            String id  = currentPath.get(i);
            Location l = locations.get(id);
            sb.append(l != null ?
                l.getName() : id);
            if (i < currentPath.size() - 1) {
                sb.append(" → ");
            }
        }
        return sb.toString();
    }

    // ── Clear Button ─────────────────────────────────────
    @FXML
    public void onClear() {
        fromCombo.setValue(null);
        toCombo.setValue(null);
        fromSelectedLabel.setText("Not selected");
        toSelectedLabel.setText("Not selected");
        distanceLabel.setText("-- km");
        fareLabel.setText("-- BDT");
        timeLabel.setText("--");
        routeLabel.setText("--");
        saveBtn.setDisable(true);
        currentPath = null;

        // reset map
        webEngine.executeScript(
            "window.mapApi.resetMarkers();" +
            "window.mapApi.resetView();");
    }

    // ── Save Ride Button ─────────────────────────────────
    @FXML
    public void onSaveRide() {
        if (currentPath == null ||
                currentPath.isEmpty()) {
            showAlert("⚠️ Warning",
                "No route to save!");
            return;
        }

        String fromName = locations.get(
            fromCombo.getValue()).getName();
        String toName   = locations.get(
            toCombo.getValue()).getName();
        String rideTime = LocalDateTime.now()
            .format(DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm"));

        RideHistory ride = new RideHistory(
            fromName, toName,
            currentDistance, currentFare,
            currentIsNight, currentIsPeak,
            rideTime
        );

        rideHistoryDAO.saveRide(ride);
        showAlert("✅ Saved!",
            "Ride saved to history!");
        saveBtn.setDisable(true);
    }

    // ── Open Screens ─────────────────────────────────────
    @FXML
    public void onOpenHistory() {
        openScreen("/ui/HistoryScreen.fxml",
            "📋 Ride History");
    }

    @FXML
    public void onOpenSaved() {
        openScreen("/ui/SavedScreen.fxml",
            "⭐ Saved Locations");
    }

    @FXML
    public void onOpenSettings() {
        openScreen("/ui/SettingsScreen.fxml",
            "⚙️ Settings");
    }

    // ── Open Screen ──────────────────────────────────────
    private void openScreen(String fxmlPath,
                             String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Error",
                "Cannot open " + title);
        }
    }

    // ── Show Alert ───────────────────────────────────────
    private void showAlert(String title,
                           String message) {
        Alert alert = new Alert(
            Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}