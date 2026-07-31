package model;

public class Location {

    // ── Fields ──────────────────────────────────────────
    private String id;      // unique identifier e.g. "IUT"
    private String name;    // display name e.g. "IUT Main Gate"
    private double x;       // X position on canvas (pixels)
    private double y;       // Y position on canvas (pixels)

    // ── Constructor ──────────────────────────────────────
    public Location(String id, String name, double x, double y) {
        this.id   = id;
        this.name = name;
        this.x    = x;
        this.y    = y;
    }

    // ── Getters ──────────────────────────────────────────
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // ── Setters ──────────────────────────────────────────
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return name; // shown in ComboBox dropdowns
    }
}