package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Dijkstra {

    // ── Find Shortest Distance ───────────────────────────
    public static double findShortestDistance(Graph graph,
                                               String start,
                                               String end) {
        Map<String, Double> distances = initDistances(graph, start);
        PriorityQueue<String> pq      = initPriorityQueue(distances);

        while (!pq.isEmpty()) {
            // pick unvisited node with smallest distance
            String current = pq.poll();

            // stop early if we reached destination
            if (current.equals(end)) break;

            // check all neighbors
            for (Edge edge : graph.getNeighbors(current)) {
                double newDist = distances.get(current)
                                 + edge.getDistance();

                // if shorter path found — update
                if (newDist < distances.get(edge.getDestination())) {
                    distances.put(edge.getDestination(), newDist);
                    pq.add(edge.getDestination());
                }
            }
        }

        // return -1 if no path found
        double result = distances.get(end);
        return result == Double.MAX_VALUE ? -1 : result;
    }

    // ── Find Shortest Path (list of location IDs) ────────
    public static List<String> findShortestPath(Graph graph,
                                                  String start,
                                                  String end) {
        Map<String, Double> distances  = initDistances(graph, start);
        Map<String, String> previousNode = new HashMap<>();
        PriorityQueue<String> pq       = initPriorityQueue(distances);

        // initialize previous node map
        for (String id : graph.getAllLocationIds()) {
            previousNode.put(id, null);
        }

        while (!pq.isEmpty()) {
            String current = pq.poll();

            // stop early if destination reached
            if (current.equals(end)) break;

            for (Edge edge : graph.getNeighbors(current)) {
                double newDist = distances.get(current)
                                 + edge.getDistance();

                if (newDist < distances.get(edge.getDestination())) {
                    distances.put(edge.getDestination(), newDist);
                    previousNode.put(edge.getDestination(), current);
                    pq.add(edge.getDestination());
                }
            }
        }

        // build path by tracing back from end to start
        return buildPath(previousNode, start, end);
    }

    // ── Helper: Initialize Distances ────────────────────
    private static Map<String, Double> initDistances(Graph graph,
                                                      String start) {
        Map<String, Double> distances = new HashMap<>();

        // set all distances to infinity
        for (String id : graph.getAllLocationIds()) {
            distances.put(id, Double.MAX_VALUE);
        }

        // set start distance to 0
        distances.put(start, 0.0);

        return distances;
    }

    // ── Helper: Initialize Priority Queue ───────────────
    private static PriorityQueue<String> initPriorityQueue(
            Map<String, Double> distances) {

        PriorityQueue<String> pq = new PriorityQueue<>(
            (a, b) -> Double.compare(distances.get(a), distances.get(b))
        );

        pq.addAll(distances.keySet());
        return pq;
    }

    // ── Helper: Build Path from Previous Node Map ────────
    private static List<String> buildPath(
            Map<String, String> previousNode,
            String start,
            String end) {

        List<String> path = new ArrayList<>();
        String current    = end;

        // trace back from end to start
        while (current != null) {
            path.add(current);
            current = previousNode.get(current);
        }

        // reverse to get start → end order
        Collections.reverse(path);

        // return empty list if no valid path found
        if (path.isEmpty() || !path.get(0).equals(start)) {
            return new ArrayList<>();
        }

        return path;
    }
}