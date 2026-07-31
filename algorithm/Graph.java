package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    // ── Adjacency List ───────────────────────────────────
    // Each location ID maps to a list of its connected edges
    private Map<String, List<Edge>> adjacencyList;

    // ── Constructor ──────────────────────────────────────
    public Graph() {
        adjacencyList = new HashMap<>();
    }

    // ── Add a Location Node ──────────────────────────────
    public void addLocation(String locationId) {
        // only add if not already exists
        if (!adjacencyList.containsKey(locationId)) {
            adjacencyList.put(locationId, new ArrayList<>());
        }
    }

    // ── Add a Road (Edge) ────────────────────────────────
    public void addEdge(String from, String to, double distance) {
        // make sure both locations exist first
        addLocation(from);
        addLocation(to);

        // add road from → to
        adjacencyList.get(from).add(new Edge(to, distance));

        // add road to → from (bidirectional — rickshaw goes both ways)
        adjacencyList.get(to).add(new Edge(from, distance));
    }

    // ── Get All Neighbors of a Location ─────────────────
    public List<Edge> getNeighbors(String locationId) {
        // return empty list if location not found
        return adjacencyList.getOrDefault(locationId, new ArrayList<>());
    }

    // ── Get All Location IDs ─────────────────────────────
    public List<String> getAllLocationIds() {
        return new ArrayList<>(adjacencyList.keySet());
    }

    // ── Check if Location Exists ─────────────────────────
    public boolean hasLocation(String locationId) {
        return adjacencyList.containsKey(locationId);
    }

    // ── Get Total Number of Locations ───────────────────
    public int getLocationCount() {
        return adjacencyList.size();
    }

    // ── Get Total Number of Roads ────────────────────────
    public int getRoadCount() {
        int count = 0;
        for (List<Edge> edges : adjacencyList.values()) {
            count += edges.size();
        }
        return count / 2; // divide by 2 because bidirectional
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph:\n");
        for (Map.Entry<String, List<Edge>> entry
                : adjacencyList.entrySet()) {
            sb.append(entry.getKey()).append(":\n");
            for (Edge edge : entry.getValue()) {
                sb.append("    ").append(edge).append("\n");
            }
        }
        return sb.toString();
    }
}