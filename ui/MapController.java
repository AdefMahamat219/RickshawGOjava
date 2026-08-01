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
    @FXML private Canvas           mapCanvas;
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

    // ── Canvas Size Constants ────────────────────────────
    private static final double CANVAS_W = 780;
    private static final double CANVAS_H = 620;

    // ── Initialize ───────────────────────────────────────
    @FXML
    public void initialize() {
        rideHistoryDAO   = new RideHistoryDAO();
        savedLocationDAO = new SavedLocationDAO();

        locations = MapData.getLocations();
        graph     = RoadData.buildGraph();

        // fill ComboBoxes with location IDs
        for (String key : locations.keySet()) {
            fromCombo.getItems().add(key);
            toCombo.getItems().add(key);
        }

        // show location NAMES in ComboBox
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

        // draw initial map
        drawMap(null);
    }

    // ── Find Route Button ────────────────────────────────
    @FXML
    public void onFindRoute() {
        String fromId = fromCombo.getValue();
        String toId   = toCombo.getValue();

        // validate
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

        // update result labels
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

        // show route path as text
        routeLabel.setText(buildRouteText());

        // draw map with highlighted route
        drawMap(currentPath);

        // enable save button
        saveBtn.setDisable(false);
    }

    // ── Build Route Text ─────────────────────────────────
    private String buildRouteText() {
        if (currentPath == null ||
                currentPath.isEmpty()) return "--";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentPath.size(); i++) {
            String id  = currentPath.get(i);
            Location l = locations.get(id);
            String name = l != null ?
                l.getName() : id;

            // shorten name for display
            sb.append(name);
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
        distanceLabel.setText("-- km");
        fareLabel.setText("-- BDT");
        timeLabel.setText("--");
        routeLabel.setText("--");
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

    // ── Draw Full Map ────────────────────────────────────
    private void drawMap(List<String> highlightPath) {
        GraphicsContext gc =
            mapCanvas.getGraphicsContext2D();

        // clear canvas
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);

        // background gradient effect
        gc.setFill(Color.web("#E8F5E9"));
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        // border
        gc.setStroke(Color.web("#BDBDBD"));
        gc.setLineWidth(1);
        gc.strokeRect(1, 1,
            CANVAS_W - 2, CANVAS_H - 2);

        // map title
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 16));
        gc.fillText(
            "📍 Board Bazar Area — Gazipur",
            15, 30);

        // draw all roads first (under nodes)
        drawAllRoads(gc);

        // highlight shortest path
        if (highlightPath != null &&
                highlightPath.size() > 1) {
            drawHighlightedPath(gc, highlightPath);
        }

        // draw all nodes on top
        drawAllNodes(gc, highlightPath);

        // draw distance labels on roads
        drawDistanceLabels(gc);

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
            GraphicsContext gc,
            List<String> path) {
        gc.setStroke(Color.web("#E94560"));
        gc.setLineWidth(6);
        for (int i = 0; i < path.size() - 1; i++) {
            drawRoad(gc,
                path.get(i),
                path.get(i + 1));
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

    // ── Draw Distance Labels on Roads ────────────────────
    private void drawDistanceLabels(
            GraphicsContext gc) {
        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font("Arial", 9));

        drawDistLabel(gc,"IUT","BOARD_INT","0.5km");
        drawDistLabel(gc,"BOARD_INT","BOARD_BAZ","1.2km");
        drawDistLabel(gc,"BOARD_INT","RAJBARI","1.5km");
        drawDistLabel(gc,"BOARD_INT","HOSPITAL","0.8km");
        drawDistLabel(gc,"BOARD_BAZ","GAZIPUR","2.0km");
        drawDistLabel(gc,"BOARD_BAZ","HOSPITAL","0.6km");
        drawDistLabel(gc,"RAJBARI","KONABARI","2.5km");
        drawDistLabel(gc,"RAJBARI","GAZIPUR","1.8km");
        drawDistLabel(gc,"GAZIPUR","CHANDNA","3.0km");
        drawDistLabel(gc,"KONABARI","BSCIC","1.8km");
        drawDistLabel(gc,"KONABARI","CHANDNA","2.0km");
        drawDistLabel(gc,"CHANDNA","MAWNA","2.2km");
        drawDistLabel(gc,"BSCIC","MAWNA","3.5km");
    }

    // ── Draw Distance Label on Midpoint of Road ──────────
    private void drawDistLabel(GraphicsContext gc,
                                String fromId,
                                String toId,
                                String label) {
        Location from = locations.get(fromId);
        Location to   = locations.get(toId);
        if (from == null || to == null) return;

        double mx = (from.getX() + to.getX()) / 2;
        double my = (from.getY() + to.getY()) / 2;

        // small white background
        gc.setFill(Color.web("#FFFFFF", 0.7));
        gc.fillRoundRect(mx-12, my-9, 30, 13, 4, 4);

        gc.setFill(Color.web("#6B7280"));
        gc.fillText(label, mx - 10, my + 1);
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
                    // start = green
                    nodeColor = Color.web("#2E7D32");
                } else if (id.equals(
                        path.get(path.size()-1))) {
                    // end = red
                    nodeColor = Color.web("#E94560");
                } else if (path.contains(id)) {
                    // on path = orange
                    nodeColor = Color.web("#F5A623");
                } else {
                    // normal = blue
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

        // shadow
        gc.setFill(Color.web("#00000033"));
        gc.fillOval(x-11, y-9, 24, 24);

        // filled circle
        gc.setFill(color);
        gc.fillOval(x-12, y-12, 24, 24);

        // white border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeOval(x-12, y-12, 24, 24);

        // location name background
        gc.setFill(Color.web("#FFFFFF", 0.75));
        gc.fillRoundRect(x+14, y-10, 130, 16, 4, 4);

        // location name
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 11));
        gc.fillText(loc.getName(), x+16, y+2);
    }

    // ── Draw Legend ──────────────────────────────────────
    private void drawLegend(GraphicsContext gc) {
        double lx = 15;
        double ly = CANVAS_H - 110;

        // legend background
        gc.setFill(Color.web("#FFFFFF", 0.9));
        gc.fillRoundRect(
            lx-8, ly-22, 160, 105, 10, 10);
        gc.setStroke(Color.web("#E5E7EB"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(
            lx-8, ly-22, 160, 105, 10, 10);

        // legend title
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 11));
        gc.fillText("Legend", lx, ly-6);

        // start
        gc.setFill(Color.web("#2E7D32"));
        gc.fillOval(lx, ly+2, 12, 12);
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial", 11));
        gc.fillText("Start Point", lx+18, ly+12);

        // end
        gc.setFill(Color.web("#E94560"));
        gc.fillOval(lx, ly+22, 12, 12);
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillText("End Point", lx+18, ly+32);

        // on path
        gc.setFill(Color.web("#F5A623"));
        gc.fillOval(lx, ly+42, 12, 12);
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillText("On Route", lx+18, ly+52);

        // normal
        gc.setFill(Color.web("#1565C0"));
        gc.fillOval(lx, ly+62, 12, 12);
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillText("Location", lx+18, ly+72);
    }

    // ── Open New Screen ──────────────────────────────────
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
