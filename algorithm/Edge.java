package algorithm;

public class Edge {

    // ── Fields ──────────────────────────────────────────
    private String destination;   // ID of the connected location
    private double distance;      // distance in km

    // ── Constructor ──────────────────────────────────────
    public Edge(String destination, double distance) {
        this.destination = destination;
        this.distance    = distance;
    }

    // ── Getters ──────────────────────────────────────────
    public String getDestination() {
        return destination;
    }

    public double getDistance() {
        return distance;
    }

    // ── Setters ──────────────────────────────────────────
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "→ " + destination + " (" + distance + " km)";
    }
}