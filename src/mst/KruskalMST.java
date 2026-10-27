package mst;

import dsu.DisjointSetUnion;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KruskalMST {
    private final List<Edge> mstEdges = new ArrayList<>();
    private double totalWeight = 0.0;
    private boolean connected = true;

    // Operation counters
    private long edgeExaminations = 0;
    private long comparisons = 0;
    private long dsuOperations = 0;
    private long executionTimeNs = 0;

    public KruskalMST(Graph graph) {
        this(graph, true, true);
    }

    public KruskalMST(Graph graph, boolean usePathCompression, boolean useUnionByRank) {
        int V = graph.getNumVertices();
        if (V <= 1) {
            this.connected = true;
            this.totalWeight = 0.0;
            return;
        }

        long startTime = System.nanoTime();

        List<Edge> allEdges = new ArrayList<>(graph.getAllEdges());

        // Sort all edges by weight
        allEdges.sort((e1, e2) -> {
            comparisons++;
            return Double.compare(e1.getWeight(), e2.getWeight());
        });

        DisjointSetUnion dsu = new DisjointSetUnion(V, usePathCompression, useUnionByRank);

        for (Edge edge : allEdges) {
            edgeExaminations++;
            int u = edge.getFrom();
            int v = edge.getTo();

            // Connect if vertices belong to different sets
            if (dsu.find(u) != dsu.find(v)) {
                dsu.union(u, v);
                mstEdges.add(edge);
                totalWeight += edge.getWeight();

                // Stop early when MST has V - 1 edges
                if (mstEdges.size() == V - 1) {
                    break;
                }
            }
        }

        long endTime = System.nanoTime();
        this.executionTimeNs = endTime - startTime;
        this.dsuOperations = dsu.getTotalOperations();
        this.connected = (mstEdges.size() == V - 1);
        this.totalWeight = Math.round(totalWeight * 100.0) / 100.0;
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(mstEdges);
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public boolean isConnected() {
        return connected;
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getDsuOperations() {
        return dsuOperations;
    }

    public long getTotalOperations() {
        return edgeExaminations + comparisons + dsuOperations;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }
}
