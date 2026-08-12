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
    private static final double CANVAS_W = 860;
    private static final double CANVAS_H = 660;

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

        currentIsNight = FareCalculator.isNightTime();
        currentIsPeak  = FareCalculator.isPeakHour();

        currentFare = FareCalculator.calculate(
            currentDistance,
            currentIsNight,
            currentIsPeak
        );

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
        drawMap(currentPath);
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

        // clear
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);

        // ── Background ───────────────────────────────
        gc.setFill(Color.web("#E8F0E8"));
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        // ── Grid lines ───────────────────────────────
        drawGridLines(gc);

        // ── Area blocks ──────────────────────────────
        drawAreaBlocks(gc);

        // ── Main highway ─────────────────────────────
        drawHighway(gc);

        // ── All roads ────────────────────────────────
        drawAllRoads(gc);

        // ── Highlight path ───────────────────────────
        if (highlightPath != null &&
                highlightPath.size() > 1) {
            drawHighlightedPath(gc, highlightPath);
        }

        // ── All nodes ────────────────────────────────
        drawAllNodes(gc, highlightPath);

        // ── Distance labels ──────────────────────────
        drawDistanceLabels(gc);

        // ── Compass ──────────────────────────────────
        drawCompass(gc);

        // ── Title ────────────────────────────────────
        drawTitle(gc);

        // ── Legend ───────────────────────────────────
        drawLegend(gc);

        // ── Scale bar ────────────────────────────────
        drawScaleBar(gc);
    }

    // ── Draw Grid Lines ──────────────────────────────────
    private void drawGridLines(GraphicsContext gc) {
        gc.setStroke(Color.web("#D0DAD0", 0.5));
        gc.setLineWidth(0.5);
        for (int x = 0; x < CANVAS_W; x += 50) {
            gc.strokeLine(x, 0, x, CANVAS_H);
        }
        for (int y = 0; y < CANVAS_H; y += 50) {
            gc.strokeLine(0, y, CANVAS_W, y);
        }
    }

    // ── Draw Area Blocks ─────────────────────────────────
    private void drawAreaBlocks(GraphicsContext gc) {
        // IUT campus
        gc.setFill(Color.web("#C8E6C9", 0.6));
        gc.fillRoundRect(100, 230, 160, 140, 10, 10);
        gc.setStroke(Color.web("#81C784", 0.8));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(100, 230, 160, 140, 10, 10);
        gc.setFill(Color.web("#388E3C", 0.8));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 9));
        gc.fillText("IUT Campus", 140, 295);

        // National University area
        gc.setFill(Color.web("#C8E6C9", 0.6));
        gc.fillRoundRect(290, 30, 180, 90, 10, 10);
        gc.setStroke(Color.web("#81C784", 0.8));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(290, 30, 180, 90, 10, 10);
        gc.setFill(Color.web("#388E3C", 0.8));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 9));
        gc.fillText("Nat. University", 318, 75);

        // Board Bazar main area
        gc.setFill(Color.web("#FFF9C4", 0.5));
        gc.fillRoundRect(320, 350, 200, 200, 10, 10);
        gc.setStroke(Color.web("#F9A825", 0.6));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(320, 350, 200, 200, 10, 10);
        gc.setFill(Color.web("#F57F17", 0.8));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 9));
        gc.fillText("Board Bazar Area", 345, 375);

        // water body
        gc.setFill(Color.web("#B3E5FC", 0.7));
        gc.fillOval(620, 420, 80, 50);
        gc.setStroke(Color.web("#0288D1", 0.5));
        gc.setLineWidth(1);
        gc.strokeOval(620, 420, 80, 50);
        gc.setFill(Color.web("#0288D1", 0.7));
        gc.setFont(Font.font("Arial", 8));
        gc.fillText("Pond", 648, 450);
    }

    // ── Draw Main Highway ────────────────────────────────
    private void drawHighway(GraphicsContext gc) {
        // shadow
        gc.setStroke(Color.web("#9E9E9E"));
        gc.setLineWidth(14);
        gc.strokeLine(400, 40, 400, 580);

        // road surface
        gc.setStroke(Color.web("#EEEEEE"));
        gc.setLineWidth(10);
        gc.strokeLine(400, 40, 400, 580);

        // center dashed line
        gc.setStroke(Color.web("#FDD835"));
        gc.setLineWidth(1.5);
        gc.setLineDashes(12, 8);
        gc.strokeLine(400, 40, 400, 580);
        gc.setLineDashes(0);

        // highway label
        gc.save();
        gc.translate(418, 180);
        gc.rotate(90);
        gc.setFill(Color.web("#757575"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 9));
        gc.fillText(
            "Dhaka-Mymensingh Highway", 0, 0);
        gc.restore();
    }

    // ── Draw All Roads ───────────────────────────────────
    private void drawAllRoads(GraphicsContext gc) {
        // road shadow layer
        gc.setStroke(Color.web("#BDBDBD"));
        gc.setLineWidth(5);

        drawRoad(gc, "NAT_UNIV",    "BIG_BAZAR");
        drawRoad(gc, "BIG_BAZAR",   "BOARD_BAZAR");
        drawRoad(gc, "BOARD_BAZAR", "UNI_GEARS");
        drawRoad(gc, "BOARD_BAZAR", "HOSPITAL");
        drawRoad(gc, "IUT",         "BIG_BAZAR");
        drawRoad(gc, "IUT",         "RAJBARI");
        drawRoad(gc, "IUT",         "ROAD_30FEET");
        drawRoad(gc, "BIG_BAZAR",   "BOTTOLA");
        drawRoad(gc, "BOTTOLA",     "CHANDNA");
        drawRoad(gc, "BOTTOLA",     "UNI_GEARS");
        drawRoad(gc, "NAT_UNIV",    "RAJBARI");
        drawRoad(gc, "NAT_UNIV",    "KONABARI");
        drawRoad(gc, "NAT_UNIV",    "CHANDNA");
        drawRoad(gc, "RAJBARI",     "KONABARI");
        drawRoad(gc, "RAJBARI",     "BIG_BAZAR");
        drawRoad(gc, "KONABARI",    "BSCIC");
        drawRoad(gc, "KONABARI",    "ROAD_30FEET");
        drawRoad(gc, "KONABARI",    "CHANDNA");
        drawRoad(gc, "CHANDNA",     "MAWNA");
        drawRoad(gc, "BSCIC",       "MAWNA");
        drawRoad(gc, "ROAD_30FEET", "BSCIC");
        drawRoad(gc, "ROAD_30FEET", "HOSPITAL");

        // road surface overlay
        gc.setStroke(Color.web("#E0E0E0"));
        gc.setLineWidth(3);

        drawRoad(gc, "NAT_UNIV",    "BIG_BAZAR");
        drawRoad(gc, "BIG_BAZAR",   "BOARD_BAZAR");
        drawRoad(gc, "BOARD_BAZAR", "UNI_GEARS");
        drawRoad(gc, "BOARD_BAZAR", "HOSPITAL");
        drawRoad(gc, "IUT",         "BIG_BAZAR");
        drawRoad(gc, "IUT",         "RAJBARI");
        drawRoad(gc, "IUT",         "ROAD_30FEET");
        drawRoad(gc, "BIG_BAZAR",   "BOTTOLA");
        drawRoad(gc, "BOTTOLA",     "CHANDNA");
        drawRoad(gc, "BOTTOLA",     "UNI_GEARS");
        drawRoad(gc, "NAT_UNIV",    "RAJBARI");
        drawRoad(gc, "NAT_UNIV",    "KONABARI");
        drawRoad(gc, "NAT_UNIV",    "CHANDNA");
        drawRoad(gc, "RAJBARI",     "KONABARI");
        drawRoad(gc, "RAJBARI",     "BIG_BAZAR");
        drawRoad(gc, "KONABARI",    "BSCIC");
        drawRoad(gc, "KONABARI",    "ROAD_30FEET");
        drawRoad(gc, "KONABARI",    "CHANDNA");
        drawRoad(gc, "CHANDNA",     "MAWNA");
        drawRoad(gc, "BSCIC",       "MAWNA");
        drawRoad(gc, "ROAD_30FEET", "BSCIC");
        drawRoad(gc, "ROAD_30FEET", "HOSPITAL");
    }

    // ── Draw Highlighted Path ────────────────────────────
    private void drawHighlightedPath(
            GraphicsContext gc,
            List<String> path) {
        // glow effect
        gc.setStroke(Color.web("#FF8A80", 0.5));
        gc.setLineWidth(12);
        for (int i = 0; i < path.size()-1; i++) {
            drawRoad(gc,
                path.get(i), path.get(i+1));
        }
        // main highlight
        gc.setStroke(Color.web("#E94560"));
        gc.setLineWidth(6);
        for (int i = 0; i < path.size()-1; i++) {
            drawRoad(gc,
                path.get(i), path.get(i+1));
        }
        // center dashed line
        gc.setStroke(Color.web("#FFFFFF", 0.6));
        gc.setLineWidth(1.5);
        gc.setLineDashes(8, 6);
        for (int i = 0; i < path.size()-1; i++) {
            drawRoad(gc,
                path.get(i), path.get(i+1));
        }
        gc.setLineDashes(0);
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

    // ── Draw Distance Labels ─────────────────────────────
    private void drawDistanceLabels(
            GraphicsContext gc) {
        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font("Arial", 9));

        drawDistLabel(gc, "NAT_UNIV",
            "BIG_BAZAR",    "1.2km");
        drawDistLabel(gc, "BIG_BAZAR",
            "BOARD_BAZAR",  "0.8km");
        drawDistLabel(gc, "BOARD_BAZAR",
            "UNI_GEARS",    "1.5km");
        drawDistLabel(gc, "BOARD_BAZAR",
            "HOSPITAL",     "0.5km");
        drawDistLabel(gc, "IUT",
            "BIG_BAZAR",    "0.8km");
        drawDistLabel(gc, "IUT",
            "RAJBARI",      "1.2km");
        drawDistLabel(gc, "IUT",
            "ROAD_30FEET",  "1.0km");
        drawDistLabel(gc, "BIG_BAZAR",
            "BOTTOLA",      "1.5km");
        drawDistLabel(gc, "BOTTOLA",
            "CHANDNA",      "2.0km");
        drawDistLabel(gc, "BOTTOLA",
            "UNI_GEARS",    "1.8km");
        drawDistLabel(gc, "NAT_UNIV",
            "RAJBARI",      "1.0km");
        drawDistLabel(gc, "NAT_UNIV",
            "KONABARI",     "1.5km");
        drawDistLabel(gc, "NAT_UNIV",
            "CHANDNA",      "2.5km");
        drawDistLabel(gc, "RAJBARI",
            "KONABARI",     "1.2km");
        drawDistLabel(gc, "RAJBARI",
            "BIG_BAZAR",    "1.0km");
        drawDistLabel(gc, "KONABARI",
            "BSCIC",        "1.0km");
        drawDistLabel(gc, "KONABARI",
            "ROAD_30FEET",  "1.5km");
        drawDistLabel(gc, "KONABARI",
            "CHANDNA",      "2.5km");
        drawDistLabel(gc, "CHANDNA",
            "MAWNA",        "2.2km");
        drawDistLabel(gc, "BSCIC",
            "MAWNA",        "3.5km");
        drawDistLabel(gc, "ROAD_30FEET",
            "BSCIC",        "1.2km");
        drawDistLabel(gc, "ROAD_30FEET",
            "HOSPITAL",     "2.0km");
    }

    // ── Draw Distance Label ──────────────────────────────
    private void drawDistLabel(GraphicsContext gc,
                                String fromId,
                                String toId,
                                String label) {
        Location from = locations.get(fromId);
        Location to   = locations.get(toId);
        if (from == null || to == null) return;

        double mx = (from.getX() + to.getX()) / 2;
        double my = (from.getY() + to.getY()) / 2;

        gc.setFill(Color.web("#FFFFFF", 0.75));
        gc.fillRoundRect(
            mx-14, my-9, 32, 13, 4, 4);
        gc.setFill(Color.web("#6B7280"));
        gc.fillText(label, mx-12, my+1);
    }

    // ── Draw All Nodes ───────────────────────────────────
    private void drawAllNodes(GraphicsContext gc,
                               List<String> path) {
        for (Map.Entry<String, Location> entry
                : locations.entrySet()) {
            String   id  = entry.getKey();
            Location loc = entry.getValue();

            Color nodeColor;
            Color borderColor;

            if (path != null && !path.isEmpty()) {
                if (id.equals(path.get(0))) {
                    nodeColor   =
                        Color.web("#2E7D32");
                    borderColor =
                        Color.web("#A5D6A7");
                } else if (id.equals(
                        path.get(path.size()-1))) {
                    nodeColor   =
                        Color.web("#E94560");
                    borderColor =
                        Color.web("#FFCDD2");
                } else if (path.contains(id)) {
                    nodeColor   =
                        Color.web("#F5A623");
                    borderColor =
                        Color.web("#FFE082");
                } else {
                    nodeColor   =
                        Color.web("#1565C0");
                    borderColor =
                        Color.web("#90CAF9");
                }
            } else {
                nodeColor   = Color.web("#1565C0");
                borderColor = Color.web("#90CAF9");
            }
            drawNode(gc, loc,
                nodeColor, borderColor);
        }
    }

    // ── Draw Single Node ─────────────────────────────────
    private void drawNode(GraphicsContext gc,
                           Location loc,
                           Color color,
                           Color borderColor) {
        double x = loc.getX();
        double y = loc.getY();

        // drop shadow
        gc.setFill(Color.web("#00000040"));
        gc.fillOval(x-10, y-8, 22, 22);

        // outer ring
        gc.setFill(borderColor);
        gc.fillOval(x-13, y-13, 26, 26);

        // main circle
        gc.setFill(color);
        gc.fillOval(x-10, y-10, 20, 20);

        // inner white dot
        gc.setFill(Color.web("#FFFFFF", 0.5));
        gc.fillOval(x-4, y-6, 7, 7);

        // name label background
        gc.setFill(Color.web("#FFFFFF", 0.92));
        gc.fillRoundRect(
            x+13, y-11, 140, 18, 5, 5);

        // name label border
        gc.setStroke(color);
        gc.setLineWidth(1);
        gc.strokeRoundRect(
            x+13, y-11, 140, 18, 5, 5);

        // name text
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 11));
        gc.fillText(loc.getName(), x+17, y+2);
    }

    // ── Draw Compass ─────────────────────────────────────
    private void drawCompass(GraphicsContext gc) {
        double cx = CANVAS_W - 45;
        double cy = 55;

        // circle
        gc.setFill(Color.web("#FFFFFF", 0.9));
        gc.fillOval(cx-25, cy-25, 50, 50);
        gc.setStroke(Color.web("#BDBDBD"));
        gc.setLineWidth(1);
        gc.strokeOval(cx-25, cy-25, 50, 50);

        // N arrow red
        gc.setFill(Color.web("#E94560"));
        double[] xN = {cx, cx-6, cx+6};
        double[] yN = {cy-20, cy, cy};
        gc.fillPolygon(xN, yN, 3);

        // S arrow gray
        gc.setFill(Color.web("#9E9E9E"));
        double[] xS = {cx, cx-6, cx+6};
        double[] yS = {cy+20, cy, cy};
        gc.fillPolygon(xS, yS, 3);

        // N label
        gc.setFill(Color.web("#E94560"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 11));
        gc.fillText("N", cx-4, cy-22);
    }

    // ── Draw Title ───────────────────────────────────────
    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.web("#1A1A2E", 0.85));
        gc.fillRoundRect(10, 8, 320, 36, 8, 8);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 16));
        gc.fillText(
            "📍 Board Bazar Area — Gazipur",
            18, 32);
    }

    // ── Draw Legend ──────────────────────────────────────
    private void drawLegend(GraphicsContext gc) {
        double lx = 12;
        double ly = CANVAS_H - 145;

        // background
        gc.setFill(Color.web("#FFFFFF", 0.92));
        gc.fillRoundRect(
            lx-8, ly-25, 165, 140, 10, 10);
        gc.setStroke(Color.web("#BDBDBD"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(
            lx-8, ly-25, 165, 140, 10, 10);

        // title
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 12));
        gc.fillText("🗺️ Legend", lx, ly-8);

        // items
        String[][] items = {
            {"#2E7D32", "Start Point"},
            {"#E94560", "End Point"},
            {"#F5A623", "On Route"},
            {"#1565C0", "Location"},
        };

        for (int i = 0; i < items.length; i++) {
            double iy = ly + 5 + i * 24;
            gc.setFill(Color.web(items[i][0]));
            gc.fillOval(lx, iy, 14, 14);
            gc.setFill(Color.web("#FFFFFF", 0.4));
            gc.fillOval(lx+3, iy+2, 6, 6);
            gc.setFill(Color.web("#1A1A2E"));
            gc.setFont(Font.font("Arial", 11));
            gc.fillText(items[i][1],
                lx+22, iy+11);
        }

        // road sample
        gc.setStroke(Color.web("#BDBDBD"));
        gc.setLineWidth(4);
        gc.strokeLine(lx, ly+105,
            lx+35, ly+105);
        gc.setStroke(Color.web("#E0E0E0"));
        gc.setLineWidth(2.5);
        gc.strokeLine(lx, ly+105,
            lx+35, ly+105);
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial", 11));
        gc.fillText("Road", lx+42, ly+109);

        // route sample
        gc.setStroke(Color.web("#E94560"));
        gc.setLineWidth(4);
        gc.strokeLine(lx+90, ly+105,
            lx+125, ly+105);
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillText("Route", lx+130, ly+109);
    }

    // ── Draw Scale Bar ───────────────────────────────────
    private void drawScaleBar(GraphicsContext gc) {
        double sx = CANVAS_W - 160;
        double sy = CANVAS_H - 25;

        gc.setFill(Color.web("#FFFFFF", 0.85));
        gc.fillRoundRect(
            sx-8, sy-14, 155, 22, 5, 5);

        // scale bar blocks
        gc.setFill(Color.web("#1A1A2E"));
        gc.fillRect(sx, sy-4, 60, 6);
        gc.setFill(Color.WHITE);
        gc.fillRect(sx+20, sy-4, 20, 6);

        // ticks
        gc.setStroke(Color.web("#1A1A2E"));
        gc.setLineWidth(1.5);
        gc.strokeLine(sx, sy-6, sx, sy+4);
        gc.strokeLine(
            sx+60, sy-6, sx+60, sy+4);

        // labels
        gc.setFill(Color.web("#1A1A2E"));
        gc.setFont(Font.font("Arial",
            FontWeight.BOLD, 9));
        gc.fillText("0", sx-3, sy+14);
        gc.fillText("1km", sx+50, sy+14);
        gc.fillText("Scale: ~1km",
            sx+70, sy+3);
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
