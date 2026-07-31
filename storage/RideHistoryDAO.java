package storage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.RideHistory;
import storage.DBConnection;

public class RideHistoryDAO {

    // ── INSERT a New Ride ────────────────────────────────
    public void saveRide(RideHistory ride) {
        String sql = "INSERT INTO ride_history " +
                     "(from_loc, to_loc, distance_km, " +
                     "fare_bdt, is_night, is_peak) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn        = DBConnection.getConnection();
            PreparedStatement ps   = conn.prepareStatement(sql);

            ps.setString (1, ride.getFrom());
            ps.setString (2, ride.getTo());
            ps.setDouble (3, ride.getDistance());
            ps.setDouble (4, ride.getFare());
            ps.setBoolean(5, ride.isNight());
            ps.setBoolean(6, ride.isPeak());

            ps.executeUpdate();
            ps.close();

            System.out.println("✅ Ride saved successfully!");

        } catch (SQLException e) {
            System.out.println("❌ Failed to save ride!");
            e.printStackTrace();
        }
    }

    // ── SELECT All Rides ─────────────────────────────────
    public List<RideHistory> getAllRides() {
        List<RideHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM ride_history " +
                     "ORDER BY ride_time DESC";
        try {
            Connection conn  = DBConnection.getConnection();
            Statement st     = conn.createStatement();
            ResultSet rs     = st.executeQuery(sql);

            while (rs.next()) {
                RideHistory ride = new RideHistory(
                    rs.getString ("from_loc"),
                    rs.getString ("to_loc"),
                    rs.getDouble ("distance_km"),
                    rs.getDouble ("fare_bdt"),
                    rs.getBoolean("is_night"),
                    rs.getBoolean("is_peak"),
                    rs.getTimestamp("ride_time").toString()
                );
                list.add(ride);
            }

            rs.close();
            st.close();

        } catch (SQLException e) {
            System.out.println("❌ Failed to load history!");
            e.printStackTrace();
        }
        return list;
    }

    // ── SELECT Last N Rides ──────────────────────────────
    public List<RideHistory> getLastRides(int limit) {
        List<RideHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM ride_history " +
                     "ORDER BY ride_time DESC " +
                     "LIMIT ?";
        try {
            Connection conn      = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RideHistory ride = new RideHistory(
                    rs.getString ("from_loc"),
                    rs.getString ("to_loc"),
                    rs.getDouble ("distance_km"),
                    rs.getDouble ("fare_bdt"),
                    rs.getBoolean("is_night"),
                    rs.getBoolean("is_peak"),
                    rs.getTimestamp("ride_time").toString()
                );
                list.add(ride);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("❌ Failed to load last rides!");
            e.printStackTrace();
        }
        return list;
    }

    // ── SELECT Total Ride Count ──────────────────────────
    public int getTotalRideCount() {
        String sql = "SELECT COUNT(*) FROM ride_history";
        try {
            Connection conn  = DBConnection.getConnection();
            Statement st     = conn.createStatement();
            ResultSet rs     = st.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt(1);
            }

            rs.close();
            st.close();

        } catch (SQLException e) {
            System.out.println("❌ Failed to count rides!");
            e.printStackTrace();
        }
        return 0;
    }

    // ── DELETE All Rides ─────────────────────────────────
    public void clearAllRides() {
        String sql = "DELETE FROM ride_history";
        try {
            Connection conn  = DBConnection.getConnection();
            Statement st     = conn.createStatement();

            st.executeUpdate(sql);
            st.close();

            System.out.println("🗑️ Ride history cleared!");

        } catch (SQLException e) {
            System.out.println("❌ Failed to clear history!");
            e.printStackTrace();
        }
    }

    // ── DELETE a Single Ride by ID ───────────────────────
    public void deleteRideById(int id) {
        String sql = "DELETE FROM ride_history WHERE id = ?";
        try {
            Connection conn      = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            System.out.println("🗑️ Ride deleted!");

        } catch (SQLException e) {
            System.out.println("❌ Failed to delete ride!");
            e.printStackTrace();
        }
    }
}