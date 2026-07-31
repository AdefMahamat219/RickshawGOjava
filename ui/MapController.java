package ui;

// ── JavaFX Imports ───────────────────────────────────
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;

// ── Java Imports ─────────────────────────────────────
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

// ── Project Imports ──────────────────────────────────
import algorithm.Dijkstra;
import algorithm.Graph;
import algorithm.MapData;
import algorithm.RoadData;
import model.FareCalculator;
import model.Location;
import model.RideHistory;
import storage.dao.RideHistoryDAO;
import storage.dao.SavedLocationDAO;

public class MapController {

    // ── FXML Elements ────────────────────────────────────
    @FXML private Canvas               mapCanvas;
    @FXML private ComboBox<String>     fromCombo;
    @FXML private ComboBox<String>     toCombo;
    @FXML private Button               findRouteBtn;
    @FXML private Button               clearBtn;
    @FXML private Button               saveBtn;
    @FXML private Button               historyBtn;
    @FXML private Button               savedBtn;
    @FXML private Button               settingsBtn;
    @FXML private Label                distanceLabel;
    @FXML private Label                fareLabel;
    @FXML private Label                timeLabel;

    // ── Data ─────────────────────────────────────────────
    private Map<String, Location> locations;
    private Graph                 graph;
    private List<String>          currentPath;
    private double                currentDistance;
    private double                currentFare;
    private boolean               currentIsNight;
    private boolean               currentIsPeak;

    // ── DAOs ─────────────────────────────────────────────
    private RideHistoryDAO   rideHistoryDAO;
    private SavedLocationDAO savedLocationDAO;

    // ── Initialize ───────────────────────────────────────
    @FXML
    public void initialize() {
        rideHistoryDAO   = new RideHistoryDAO();
        savedLocationDAO = new SavedLocationDAO();

        locations = MapData.getLocations();
        graph     = RoadData.buildGraph();

        // fill ComboBoxes
        for (String key : locations.keySet()) {
            fromCombo.getItems().add(key);
            toCombo.getItems().add(key);
        }

        // show names instead of IDs
        StringConverter<String> converter =
            new StringConverter<String>() {
                @Override
                public String toString(String id) {
                    if (id == null) return "";
                    Location loc = locations.get(id);
                    return loc != null ? loc.getName() : id;
                }
                @Override
                public String fromString(String s) {
                    return s;
                }
            };

        fromCombo.setConverter(converter);
        toCombo.setConverter(converter);

        // draw initial map
        drawMap(null);
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
        currentDistance = Dijkstra.findShortestDistance(
            graph, fromId, toId
        );
        currentPath = Dijkstra.findShortestPath(
            graph, fromId, toId
        );

        if (currentDistance == -1 ||
                currentPath.isEmpty()) {
            showAlert("❌ No Route",
                "No route found between " +
                "selected locations!");
            return;
        }

        // detect time
        currentIsNight = FareCalculator.isNightTime();
        currentIsPeak  = FareCalculator.isPeakHour();

        // calculate fare
        currentFare = FareCalculator.calculate(
            currentDistance,
            currentIsNight,
            currentIsPeak
        );

        // update labels
        distanceLabel.setText(
            String.format("%.1f km", currentDistance)
        );
        fareLabel.setText(
            String.format("%.0f BDT", currentFare)
        );

        if (currentIsNight) {
            timeLabel.setText("🌙 Night Ride (×1.5)");
        } else if (currentIsPeak) {
            timeLabel.setText("🚦 Peak Hour (×1.2)");
        } else {
            timeLabel.setText("☀️ Normal Rate");
        }

        // draw highlighted route
        drawMap(currentPath);

        // enable save button
        saveBtn.setDisable(false);
    }

    // ── Clear Button ─────────────────────────────────────
    @FXML
    public void onClear() {
        fromCombo.setValue(null);
        toCombo.setValue(null);
        distanceLabel.setText("-- km");
        fareLabel.setText("-- BDT");
        timeLabel.setText("--");
        saveBtn.setDisable(true);
        currentPath = null;
        drawMap(null);
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

        String rideTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm")
        );

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

    // ── Open History Screen ──────────────────────────────
    @FXML
    public void onOpenHistory() {
        openScreen("/ui/HistoryScreen.fxml",
            "📋 Ride History");
    }

    // ── Open Saved Screen ────────────────────────────────
    @FXML
    public void onOpenSaved() {
        openScreen("/ui/SavedScreen.fxml",
            "⭐ Saved Locations");
    }

    // ── Open Settings Screen ─────────────────────────────
    @FXML
    public void onOpenSettings() {
        openScreen("/ui/SettingsScreen.fxml",
            "⚙️ Settings");
    }

    // ── Draw Map ─────────────────────────────────────────
    private void drawMap(List<String> highlightPath) {
        GraphicsContext gc =
            mapCanvas.getGraphicsContext2D();

        // clear
        gc.clearRect(0, 0,
            mapCanvas.getWidth(),
            mapCanvas.getHeight());

        // background
        gc.setFill(Color.web("#E8F5E9"));
        gc.fillRect(0, 0,
            mapCanvas.getWidth(),
            mapCanvas.getHeight());

        // title
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 16));
        gc.fillText(
            "📍 Board Bazar Area — Gazipur",
            15, 30);

        // draw roads
        drawAllRoads(gc);

        // highlight path
        if (highlightPath != null &&
                highlightPath.size() > 1) {
            drawHighlightedPath(gc, highlightPath);
        }

        // draw nodes
        drawAllNodes(gc, highlightPath);

        // draw legend
        drawLegend(gc);
    }

    // ── Draw All Roads ───────────────────────────────────
    private void drawAllRoads(GraphicsContext gc) {
        gc.setStroke(Color.web("#BDBDBD"));
        gc.setLineWidth(3);

        drawRoad(gc, "IUT",       "BOARD_INT");
        drawRoad(gc, "BOARD_INT", "BOARD_BAZ");
        drawRoad(gc, "BOARD_INT", "RAJBARI");
        drawRoad(gc, "BOARD_INT", "HOSPITAL");
        drawRoad(gc, "BOARD_BAZ", "GAZIPUR");
        drawRoad(gc, "BOARD_BAZ", "HOSPITAL");
        drawRoad(gc, "RAJBARI",   "KONABARI");
        drawRoad(gc, "RAJBARI",   "GAZIPUR");
        drawRoad(gc, "GAZIPUR",   "CHANDNA");
        drawRoad(gc, "KONABARI",  "BSCIC");
        drawRoad(gc, "KONABARI",  "CHANDNA");
        drawRoad(gc, "CHANDNA",   "MAWNA");
        drawRoad(gc, "BSCIC",     "MAWNA");
    }

    // ── Draw Highlighted Path ────────────────────────────
    private void drawHighlightedPath(
            GraphicsContext gc, List<String> path) {
        gc.setStroke(Color.web("#E94560"));
        gc.setLineWidth(6);
        for (int i = 0; i < path.size() - 1; i++) {
            drawRoad(gc,
                path.get(i), path.get(i + 1));
        }
    }

    // ── Draw Single Road ─────────────────────────────────
    private void drawRoad(GraphicsContext gc,
                           String fromId,
                           String toId) {
        Location from = locations.get(fromId);
        Location to   = locations.get(toId);
        if (from == null || to == null) return;
        gc.strokeLine(
            from.getX(), from.getY(),
            to.getX(),   to.getY());
    }

    // ── Draw All Nodes ───────────────────────────────────
    private void drawAllNodes(GraphicsContext gc,
                               List<String> path) {
        for (Map.Entry<String, Location> entry
                : locations.entrySet()) {
            String   id  = entry.getKey();
            Location loc = entry.getValue();

            Color nodeColor;
            if (path != null && !path.isEmpty()) {
                if (id.equals(path.get(0))) {
                    nodeColor = Color.web("#2E7D32");
                } else if (id.equals(
                        path.get(path.size()-1))) {
                    nodeColor = Color.web("#E94560");
                } else if (path.contains(id)) {
                    nodeColor = Color.web("#F5A623");
                } else {
                    nodeColor = Color.web("#1565C0");
                }
            } else {
                nodeColor = Color.web("#1565C0");
            }
            drawNode(gc, loc, nodeColor);
        }
    }

    // ── Draw Single Node ─────────────────────────────────
    private void drawNode(GraphicsContext gc,
                           Location loc,
                           Color color) {
        double x = loc.getX();
        double y = loc.getY();

        gc.setFill(color);
        gc.fillOval(x-12, y-12, 24, 24);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(x-12, y-12, 24, 24);

        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 11));
        gc.fillText(loc.getName(), x+15, y+4);
    }

    // ── Draw Legend ──────────────────────────────────────
    private void drawLegend(GraphicsContext gc) {
        double lx = 15;
        double ly = mapCanvas.getHeight() - 100;

        gc.setFill(Color.web("#FFFFFF", 0.8));
        gc.fillRoundRect(
            lx-5, ly-20, 200, 95, 10, 10);

        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 11));

        gc.setFill(Color.web("#2E7D32"));
        gc.fillOval(lx, ly, 12, 12);
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillText("Start Point", lx+18, ly+10);

        gc.setFill(Color.web("#E94560"));
        gc.fillOval(lx, ly+22, 12, 12);
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillText("End Point", lx+18, ly+32);

        gc.setFill(Color.web("#F5A623"));
        gc.fillOval(lx, ly+44, 12, 12);
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillText("On Route", lx+18, ly+54);

        gc.setFill(Color.web("#1565C0"));
        gc.fillOval(lx, ly+66, 12, 12);
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillText("Location", lx+18, ly+76);
    }

    // ── Open Screen ──────────────────────────────────────
    private void openScreen(String fxmlPath,
                             String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath)
            );
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