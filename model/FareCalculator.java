package model;

import java.time.LocalTime;

public class FareCalculator {

    // ── Fare Constants ───────────────────────────────────
    private static double BASE_RATE        = 15.0;  // BDT for first km
    private static double PER_KM_RATE      = 10.0;  // BDT per additional km
    private static double NIGHT_MULTIPLIER = 1.5;   // 50% extra at night
    private static double PEAK_MULTIPLIER  = 1.2;   // 20% extra at peak hours

    // ── Night & Peak Hours ───────────────────────────────
    private static final int NIGHT_START   = 21;    // 9:00 PM
    private static final int NIGHT_END     = 6;     // 6:00 AM
    private static final int PEAK_START_AM = 8;     // 8:00 AM
    private static final int PEAK_END_AM   = 10;    // 10:00 AM
    private static final int PEAK_START_PM = 17;    // 5:00 PM
    private static final int PEAK_END_PM   = 19;    // 7:00 PM

    // ── Main Calculate Method ────────────────────────────
    public static double calculate(double distanceKm,
                                   boolean isNight,
                                   boolean isPeak) {
        // Step 1 — base fare calculation
        double fare;
        if (distanceKm <= 1.0) {
            fare = BASE_RATE;
        } else {
            fare = BASE_RATE + (distanceKm - 1.0) * PER_KM_RATE;
        }

        // Step 2 — apply night multiplier
        if (isNight) {
            fare = fare * NIGHT_MULTIPLIER;
        }
        // Step 3 — apply peak hour multiplier
        else if (isPeak) {
            fare = fare * PEAK_MULTIPLIER;
        }

        // Step 4 — round to nearest BDT
        return Math.round(fare);
    }

    // ── Auto Detect Night Time ───────────────────────────
    public static boolean isNightTime() {
        int hour = LocalTime.now().getHour();
        return hour >= NIGHT_START || hour < NIGHT_END;
    }

    // ── Auto Detect Peak Hour ────────────────────────────
    public static boolean isPeakHour() {
        int hour = LocalTime.now().getHour();
        return (hour >= PEAK_START_AM && hour < PEAK_END_AM) ||
               (hour >= PEAK_START_PM && hour < PEAK_END_PM);
    }

    // ── Update Rates from Settings ───────────────────────
    public static void updateRates(double baseRate,
                                   double perKmRate,
                                   double nightMultiplier,
                                   double peakMultiplier) {
        BASE_RATE        = baseRate;
        PER_KM_RATE      = perKmRate;
        NIGHT_MULTIPLIER = nightMultiplier;
        PEAK_MULTIPLIER  = peakMultiplier;
    }

    // ── Get Fare Breakdown ───────────────────────────────
    public static String getBreakdown(double distanceKm,
                                      boolean isNight,
                                      boolean isPeak) {
        double baseFare;
        if (distanceKm <= 1.0) {
            baseFare = BASE_RATE;
        } else {
            baseFare = BASE_RATE + (distanceKm - 1.0) * PER_KM_RATE;
        }

        double finalFare = calculate(distanceKm, isNight, isPeak);

        String multiplierInfo = "";
        if (isNight) {
            multiplierInfo = "🌙 Night Ride × " + NIGHT_MULTIPLIER;
        } else if (isPeak) {
            multiplierInfo = "🚦 Peak Hour × " + PEAK_MULTIPLIER;
        } else {
            multiplierInfo = "☀️ Normal Rate";
        }

        return "📏 Distance    : " + distanceKm + " km\n" +
               "💵 Base Fare   : " + baseFare + " BDT\n" +
               "⏰ Time        : " + multiplierInfo + "\n" +
               "💰 Final Fare  : " + finalFare + " BDT";
    }

    // ── Getters for current rates ────────────────────────
    public static double getBaseRate()        { return BASE_RATE; }
    public static double getPerKmRate()       { return PER_KM_RATE; }
    public static double getNightMultiplier() { return NIGHT_MULTIPLIER; }
    public static double getPeakMultiplier()  { return PEAK_MULTIPLIER; }
}