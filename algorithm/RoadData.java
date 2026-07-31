package algorithm;

public class RoadData {

    // ── Build Complete Board Bazar Road Network ──────────
    public static Graph buildGraph() {

        Graph graph = new Graph();

        // ── Roads from IUT ───────────────────────────────
        graph.addEdge("IUT",        "BOARD_INT",  0.5);

        // ── Roads from Board Bazar Intersection ──────────
        graph.addEdge("BOARD_INT",  "BOARD_BAZ",  1.2);
        graph.addEdge("BOARD_INT",  "RAJBARI",    1.5);
        graph.addEdge("BOARD_INT",  "HOSPITAL",   0.8);

        // ── Roads from Board Bazar Bazar ─────────────────
        graph.addEdge("BOARD_BAZ",  "GAZIPUR",    2.0);
        graph.addEdge("BOARD_BAZ",  "HOSPITAL",   0.6);

        // ── Roads from Rajbari Bus Stop ──────────────────
        graph.addEdge("RAJBARI",    "KONABARI",   2.5);
        graph.addEdge("RAJBARI",    "GAZIPUR",    1.8);

        // ── Roads from Gazipur Chowrasta ─────────────────
        graph.addEdge("GAZIPUR",    "CHANDNA",    3.0);

        // ── Roads from Konabari ──────────────────────────
        graph.addEdge("KONABARI",   "BSCIC",      1.8);
        graph.addEdge("KONABARI",   "CHANDNA",    2.0);

        // ── Roads from Chandna Chowk ─────────────────────
        graph.addEdge("CHANDNA",    "MAWNA",      2.2);

        // ── Roads from BSCIC ─────────────────────────────
        graph.addEdge("BSCIC",      "MAWNA",      3.5);

        return graph;
    }
}