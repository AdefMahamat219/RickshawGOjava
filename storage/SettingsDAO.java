package storage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import storage.DBConnection;

public class SettingsDAO {

    // ── SELECT All Settings ──────────────────────────────
    public double[] getSettings() {

        // default values if DB fails
        double[] settings = {
            15.0,  // [0] base rate
            10.0,  // [1] per km rate
            1.5,   // [2] night multiplier
            1.2,   // [3] peak multiplier
            21.0,  // [4] night start hour
            6.0    // [5] night end hour
        };

        String sql = "SELECT * FROM settings LIMIT 1";
        try {
            Connection conn = DBConnection.getConnection();
            Statement st    = conn.createStatement();
            ResultSet rs    = st.executeQuery(sql);

            if (rs.next()) {
                settings[0] = rs.getDouble("base_rate");
                settings[1] = rs.getDouble("per_km_rate");
                settings[2] = rs.getDouble("night_multiplier");
                settings[3] = rs.getDouble("peak_multiplier");
                settings[4] = rs.getDouble("night_start_hour");
                settings[5] = rs.getDouble("night_end_hour");
            }

            rs.close();
            st.close();

            System.out.println("✅ Settings loaded!");

        } catch (SQLException e) {
            System.out.println("❌ Failed to load settings!" +
                               " Using defaults.");
            e.printStackTrace();
        }
        return settings;
    }

    // ── SELECT Single Setting by Key ─────────────────────
    public double getSetting(String columnName) {
        String sql = "SELECT " + columnName +
                     " FROM settings LIMIT 1";
        try {
            Connection conn = DBConnection.getConnection();
            Statement st    = conn.createStatement();
            ResultSet rs    = st.executeQuery(sql);

            if (rs.next()) {
                return rs.getDouble(columnName);
            }

            rs.close();
            st.close();

        } catch (SQLException e) {
            System.out.println("❌ Failed to get setting: "
                               + columnName);
            e.printStackTrace();
        }
        return 0.0;
    }

    // ── UPDATE All Settings ──────────────────────────────
    public void saveSettings(double baseRate,
                             double perKmRate,
                             double nightMultiplier,
                             double peakMultiplier,
                             int nightStartHour,
                             int nightEndHour) {
        String sql = "UPDATE settings SET " +
                     "base_rate         = ?, " +
                     "per_km_rate       = ?, " +
                     "night_multiplier  = ?, " +
                     "peak_multiplier   = ?, " +
                     "night_start_hour  = ?, " +
                     "night_end_hour    = ?";
        try {
            Connection conn      = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setDouble(1, baseRate);
            ps.setDouble(2, perKmRate);
            ps.setDouble(3, nightMultiplier);
            ps.setDouble(4, peakMultiplier);
            ps.setInt   (5, nightStartHour);
            ps.setInt   (6, nightEndHour);

            ps.executeUpdate();
            ps.close();

            System.out.println("✅ Settings saved!");

            // update FareCalculator with new rates
            applySettingsToCalculator(
                baseRate, perKmRate,
                nightMultiplier, peakMultiplier
            );

        } catch (SQLException e) {
            System.out.println("❌ Failed to save settings!");
            e.printStackTrace();
        }
    }

    // ── RESET Settings to Default ────────────────────────
    public void resetToDefault() {
        saveSettings(15.0, 10.0, 1.5, 1.2, 21, 6);
        System.out.println("🔄 Settings reset to default!");
    }

    // ── CHECK if Settings Row Exists ─────────────────────
    public boolean settingsExist() {
        String sql = "SELECT COUNT(*) FROM settings";
        try {
            Connection conn = DBConnection.getConnection();
            Statement st    = conn.createStatement();
            ResultSet rs    = st.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            rs.close();
            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── INSERT Default Settings if None Exist ────────────
    public void initializeSettings() {
        if (!settingsExist()) {
            String sql = "INSERT INTO settings " +
                         "(base_rate, per_km_rate, " +
                         "night_multiplier, peak_multiplier, " +
                         "night_start_hour, night_end_hour) " +
                         "VALUES (15.0, 10.0, 1.5, 1.2, 21, 6)";
            try {
                Connection conn = DBConnection.getConnection();
                Statement st    = conn.createStatement();

                st.executeUpdate(sql);
                st.close();

                System.out.println("✅ Default settings created!");

            } catch (SQLException e) {
                System.out.println("❌ Failed to init settings!");
                e.printStackTrace();
            }
        }
    }

    // ── Apply Settings to FareCalculator ─────────────────
    private void applySettingsToCalculator(double baseRate,
                                           double perKmRate,
                                           double nightMult,
                                           double peakMult) {
        model.FareCalculator.updateRates(
            baseRate, perKmRate, nightMult, peakMult
        );
        System.out.println("✅ FareCalculator updated!");
    }
}