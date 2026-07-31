package storage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import storage.DBConnection;

public class SavedLocationDAO {

    // ── INSERT a New Saved Location ──────────────────────
    public void saveLocation(String name, String nodeId) {
        // check if name already exists first
        if (locationExists(name)) {
            System.out.println("⚠️ Location already saved!");
            return;
        }

        String sql = "INSERT INTO saved_locations " +
                     "(name, node_id) VALUES (?, ?)";
        try {
            Connection conn      = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, nodeId);

            ps.executeUpdate();
            ps.close();

            System.out.println("✅ Location saved: " + name);

        } catch (SQLException e) {
            System.out.println("❌ Failed to save location!");
            e.printStackTrace();
        }
    }

    // ── SELECT All Saved Locations ───────────────────────
    public List<String[]> getAllLocations() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM saved_locations " +
                     "ORDER BY name ASC";
        try {
            Connection conn = DBConnection.getConnection();
            Statement st    = conn.createStatement();
            ResultSet rs    = st.executeQuery(sql);

            while (rs.next()) {
                // each entry: [id, name, nodeId]
                list.add(new String[]{
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("node_id")
                });
            }

            rs.close();
            st.close();

        } catch (SQLException e) {
            System.out.println("❌ Failed to load locations!");
            e.printStackTrace();
        }
        return list;
    }

    // ── SELECT Node ID by Name ───────────────────────────
    public String getNodeIdByName(String name) {
        String sql = "SELECT node_id FROM saved_locations " +
                     "WHERE name = ?";
        try {
            Connection conn      = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("node_id");
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("❌ Failed to get node ID!");
            e.printStackTrace();
        }
        return null;
    }

    // ── CHECK if Location Name Already Exists ────────────
    public boolean locationExists(String name) {
        String sql = "SELECT COUNT(*) FROM saved_locations " +
                     "WHERE name = ?";
        try {
            Connection conn      = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("❌ Failed to check location!");
            e.printStackTrace();
        }
        return false;
    }

    // ── SELECT Total Saved Count ─────────────────────────
    public int getTotalSavedCount() {
        String sql = "SELECT COUNT(*) FROM saved_locations";
        try {
            Connection conn = DBConnection.getConnection();
            Statement st    = conn.createStatement();
            ResultSet rs    = st.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt(1);
            }

            rs.close();
            st.close();

        } catch (SQLException e) {
            System.out.println("❌ Failed to count locations!");
            e.printStackTrace();
        }
        return 0;
    }

    // ── DELETE a Location by Name ────────────────────────
    public void deleteLocationByName(String name) {
        String sql = "DELETE FROM saved_locations " +
                     "WHERE name = ?";
        try {
            Connection conn      = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();

            System.out.println("🗑️ Location deleted: " + name);

        } catch (SQLException e) {
            System.out.println("❌ Failed to delete location!");
            e.printStackTrace();
        }
    }

    // ── DELETE All Saved Locations ───────────────────────
    public void clearAllLocations() {
        String sql = "DELETE FROM saved_locations";
        try {
            Connection conn = DBConnection.getConnection();
            Statement st    = conn.createStatement();

            st.executeUpdate(sql);
            st.close();

            System.out.println("🗑️ All locations cleared!");

        } catch (SQLException e) {
            System.out.println("❌ Failed to clear locations!");
            e.printStackTrace();
        }
    }

    // ── UPDATE a Location Name ───────────────────────────
    public void updateLocationName(String oldName, String newName) {
        String sql = "UPDATE saved_locations " +
                     "SET name = ? WHERE name = ?";
        try {
            Connection conn      = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
            ps.close();

            System.out.println("✅ Location updated: " +
                                oldName + " → " + newName);

        } catch (SQLException e) {
            System.out.println("❌ Failed to update location!");
            e.printStackTrace();
        }
    }
}