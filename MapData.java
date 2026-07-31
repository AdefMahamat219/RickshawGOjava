package algorithm;

import java.util.HashMap;
import java.util.Map;
import model.Location;

public class MapData {

    // ── Canvas Size ──────────────────────────────────────
    public static final double CANVAS_WIDTH  = 800.0;
    public static final double CANVAS_HEIGHT = 600.0;

    // ── All Board Bazar Locations ────────────────────────
    public static Map<String, Location> getLocations() {

        Map<String, Location> locations = new HashMap<>();

        // ID           Name                          X      Y
        locations.put("IUT",
            new Location("IUT",
                "IUT Main Gate",                     150,   450));

        locations.put("BOARD_INT",
            new Location("BOARD_INT",
                "Board Bazar Intersection",          300,   350));

        locations.put("BOARD_BAZ",
            new Location("BOARD_BAZ",
                "Board Bazar Bazar",                 420,   420));

        locations.put("HOSPITAL",
            new Location("HOSPITAL",
                "Board Bazar Hospital",              400,   510));

        locations.put("RAJBARI",
            new Location("RAJBARI",
                "Rajbari Bus Stop",                  250,   240));

        locations.put("GAZIPUR",
            new Location("GAZIPUR",
                "Gazipur Chowrasta",                 500,   280));

        locations.put("KONABARI",
            new Location("KONABARI",
                "Konabari",                          180,   160));

        locations.put("BSCIC",
            new Location("BSCIC",
                "BSCIC",                             80,    160));

        locations.put("CHANDNA",
            new Location("CHANDNA",
                "Chandna Chowk",                    580,   170));

        locations.put("MAWNA",
            new Location("MAWNA",
                "Mawna Chowk",                      680,   80));

        return locations;
    }

    // ── Get a Single Location by ID ──────────────────────
    public static Location getLocation(String id) {
        return getLocations().get(id);
    }

    // ── Get All Location Names for ComboBox ──────────────
    public static Map<String, String> getLocationNames() {
        Map<String, String> names = new HashMap<>();
        for (Map.Entry<String, Location> entry
                : getLocations().entrySet()) {
            names.put(entry.getKey(), entry.getValue().getName());
        }
        return names;
    }
}