package shortestpath;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class Dijkstra {
    private final int source;
    private final double[] dist;
    private final int[] parent;

    private long edgeRelaxations = 0;
    private long pqOperations = 0;
    private long comparisons = 0;
    private long executionTimeNs = 0;

    public static class NodeEntry implements Comparable<NodeEntry> {
        final int vertex;
        final double distance;

        public NodeEntry(int vertex, double distance) {
            this.vertex = vertex;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeEntry other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public Dijkstra(Graph graph, int source) {
        int V = graph.getNumVertices();
        if (source < 0 || source >= V) {
            throw new IndexOutOfBoundsException("Source vertex " + source + " is out of bounds");
        }

        this.source = source;
        this.dist = new double[V];
        this.parent = new int[V];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(parent, -1);

        long startTime = System.nanoTime();

        PriorityQueue<NodeEntry> pq = new PriorityQueue<>((a, b) -> {
            comparisons++;
            return Double.compare(a.distance, b.distance);
        });

        // Initialize source
        dist[source] = 0.0;
        pq.add(new NodeEntry(source, 0.0));
        pqOperations++;

        while (!pq.isEmpty()) {
            NodeEntry curr = pq.poll();
            pqOperations++;
            int u = curr.vertex;

            // Skip if we already found a shorter path to u
            if (curr.distance > dist[u]) {
                continue;
            }

            // Relax outgoing edges
            for (Edge edge : graph.getIncidentEdges(u)) {
                if (edge.getWeight() < 0) {
                    throw new IllegalArgumentException("Dijkstra does not support negative weights: " + edge.getWeight());
                }

                int v = edge.getTo();
                edgeRelaxations++;

                double newDist = dist[u] + edge.getWeight();
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    parent[v] = u;
                    pq.add(new NodeEntry(v, newDist));
                    pqOperations++;
                }
            }
        }

        long endTime = System.nanoTime();
        this.executionTimeNs = endTime - startTime;
    }

    public int getSource() {
        return source;
    }

    public double getDistance(int target) {
        return dist[target];
    }

    public boolean hasPath(int target) {
        return dist[target] != Double.POSITIVE_INFINITY;
    }

    public List<Integer> getPath(int target) {
        if (!hasPath(target)) {
            return Collections.emptyList();
        }

        List<Integer> path = new ArrayList<>();
        for (int at = target; at != -1; at = parent[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    public long getEdgeRelaxations() {
        return edgeRelaxations;
    }

    public long getPqOperations() {
        return pqOperations;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getTotalOperations() {
        return edgeRelaxations + pqOperations + comparisons;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }
}
