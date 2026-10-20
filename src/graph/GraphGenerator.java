package graph;

import java.util.*;

public class GraphGenerator {

    /**
     * Generates a connected undirected graph with V vertices and E edges.
     */
    public static Graph generateConnectedUndirectedGraph(int V, int E, long seed, double minWeight, double maxWeight) {
        if (V <= 0) {
            throw new IllegalArgumentException("V must be positive");
        }
        long maxEdges = ((long) V * (V - 1)) / 2;
        if (E < V - 1 || E > maxEdges) {
            throw new IllegalArgumentException("E must be between V-1 and V*(V-1)/2");
        }

        Random rng = new Random(seed);
        Graph graph = new Graph(V, false);
        Set<Long> existingEdges = new HashSet<>();

        // Build a random spanning tree first to ensure the graph is connected
        List<Integer> order = new ArrayList<>(V);
        for (int i = 0; i < V; i++) {
            order.add(i);
        }
        Collections.shuffle(order, rng);

        for (int i = 1; i < V; i++) {
            int u = order.get(i);
            int v = order.get(rng.nextInt(i));
            double w = roundWeight(minWeight + (maxWeight - minWeight) * rng.nextDouble());
            graph.addEdge(u, v, w);
            existingEdges.add(edgeKey(u, v));
        }

        // Add remaining edges
        int remaining = E - (V - 1);
        while (remaining > 0) {
            int u = rng.nextInt(V);
            int v = rng.nextInt(V);
            if (u == v) continue;
            long key = edgeKey(u, v);
            if (existingEdges.add(key)) {
                double w = roundWeight(minWeight + (maxWeight - minWeight) * rng.nextDouble());
                graph.addEdge(u, v, w);
                remaining--;
            }
        }

        return graph;
    }

    /**
     * Generates a Directed Acyclic Graph (DAG) with edges only from smaller to larger IDs.
     */
    public static Graph generateDAG(int V, int E, long seed, double minWeight, double maxWeight) {
        if (V <= 0) throw new IllegalArgumentException("V must be positive");
        long maxEdges = ((long) V * (V - 1)) / 2;
        int targetEdges = (int) Math.min(E, maxEdges);

        Random rng = new Random(seed);
        Graph graph = new Graph(V, true);
        Set<Long> existingEdges = new HashSet<>();

        while (existingEdges.size() < targetEdges) {
            int u = rng.nextInt(V);
            int v = rng.nextInt(V);
            if (u >= v) continue; // guarantees no cycle
            long key = directedEdgeKey(u, v);
            if (existingEdges.add(key)) {
                double w = roundWeight(minWeight + (maxWeight - minWeight) * rng.nextDouble());
                graph.addEdge(u, v, w);
            }
        }

        return graph;
    }

    /**
     * Generates a general directed graph that can contain cycles.
     */
    public static Graph generateDirectedGraphWithCycles(int V, int E, long seed, double minWeight, double maxWeight) {
        if (V <= 0) throw new IllegalArgumentException("V must be positive");
        long maxEdges = (long) V * (V - 1);
        int targetEdges = (int) Math.min(E, maxEdges);

        Random rng = new Random(seed);
        Graph graph = new Graph(V, true);
        Set<Long> existingEdges = new HashSet<>();

        // Make at least one cycle if V >= 3
        if (V >= 3 && targetEdges >= 3) {
            graph.addEdge(0, 1, roundWeight(minWeight + rng.nextDouble() * 10));
            existingEdges.add(directedEdgeKey(0, 1));
            graph.addEdge(1, 2, roundWeight(minWeight + rng.nextDouble() * 10));
            existingEdges.add(directedEdgeKey(1, 2));
            graph.addEdge(2, 0, roundWeight(minWeight + rng.nextDouble() * 10));
            existingEdges.add(directedEdgeKey(2, 0));
        }

        while (existingEdges.size() < targetEdges) {
            int u = rng.nextInt(V);
            int v = rng.nextInt(V);
            if (u == v) continue;
            long key = directedEdgeKey(u, v);
            if (existingEdges.add(key)) {
                double w = roundWeight(minWeight + (maxWeight - minWeight) * rng.nextDouble());
                graph.addEdge(u, v, w);
            }
        }

        return graph;
    }

    private static long edgeKey(int u, int v) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        return (((long) a) << 32) | (b & 0xFFFFFFFFL);
    }

    private static long directedEdgeKey(int u, int v) {
        return (((long) u) << 32) | (v & 0xFFFFFFFFL);
    }

    private static double roundWeight(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
