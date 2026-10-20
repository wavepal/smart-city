package shortestpath;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class Dijkstra {

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

    public static ShortestPathResult findShortestPaths(Graph graph, int source) {
        int V = graph.getNumVertices();
        if (source < 0 || source >= V) {
            throw new IndexOutOfBoundsException("Source vertex " + source + " is out of bounds [0, " + (V - 1) + "]");
        }

        long startTime = System.nanoTime();

        double[] dist = new double[V];
        int[] parent = new int[V];
        boolean[] visited = new boolean[V];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(parent, -1);

        long[] comparisons = new long[1];
        long edgeRelaxations = 0;
        long pqOperations = 0;

        PriorityQueue<NodeEntry> pq = new PriorityQueue<>((a, b) -> {
            comparisons[0]++;
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

            // Skip if vertex was already processed
            if (visited[u]) {
                continue;
            }
            visited[u] = true;

            // Relax outgoing edges
            for (Edge edge : graph.getIncidentEdges(u)) {
                if (edge.getWeight() < 0) {
                    throw new IllegalArgumentException(
                            "Negative edge weight (" + edge.getWeight() + ") found! Dijkstra requires non-negative weights."
                    );
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

        return new ShortestPathResult(
                source,
                dist,
                parent,
                endTime - startTime,
                edgeRelaxations,
                pqOperations,
                comparisons[0]
        );
    }
}
