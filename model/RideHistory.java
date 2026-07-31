package model;

public class RideHistory {

    // ── Fields ──────────────────────────────────────────
    private String  from;        // start location name
    private String  to;          // end location name
    private double  distance;    // distance in km
    private double  fare;        // fare in BDT
    private boolean isNight;     // was it a night ride?
    private boolean isPeak;      // was it peak hour?
    private String  rideTime;    // timestamp of ride

    // ── Constructor ──────────────────────────────────────
    public RideHistory(String from, String to,
                       double distance, double fare,
                       boolean isNight, boolean isPeak,
                       String rideTime) {
        this.from     = from;
        this.to       = to;
        this.distance = distance;
        this.fare     = fare;
        this.isNight  = isNight;
        this.isPeak   = isPeak;
        this.rideTime = rideTime;
    }

    // ── Getters ──────────────────────────────────────────
    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getDistance() {
        return distance;
    }

    public double getFare() {
        return fare;
    }

    public boolean isNight() {
        return isNight;
    }

    public boolean isPeak() {
        return isPeak;
    }

    public String getRideTime() {
        return rideTime;
    }

    // ── Setters ──────────────────────────────────────────
    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public void setNight(boolean isNight) {
        this.isNight = isNight;
    }

    public void setPeak(boolean isPeak) {
        this.isPeak = isPeak;
    }

    public void setRideTime(String rideTime) {
        this.rideTime = rideTime;
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return from + " → " + to +
               " | " + distance + " km" +
               " | " + fare + " BDT" +
               " | " + (isNight ? "🌙 Night" : "☀️ Day") +
               " | " + rideTime;
    }
}