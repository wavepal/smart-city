package mst;

import graph.Edge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MSTResult {
    private final String algorithm;
    private final List<Edge> edges;
    private final double totalWeight;
    private final boolean connected;
    private final long executionTimeNs;
    private final long edgeExaminations;
    private final long comparisons;
    private final long auxiliaryOperations; // DSU operations for Kruskal, PQ operations for Prim

    public MSTResult(String algorithm, List<Edge> edges, double totalWeight, boolean connected,
                     long executionTimeNs, long edgeExaminations, long comparisons, long auxiliaryOperations) {
        this.algorithm = algorithm;
        this.edges = new ArrayList<>(edges);
        this.totalWeight = Math.round(totalWeight * 100.0) / 100.0;
        this.connected = connected;
        this.executionTimeNs = executionTimeNs;
        this.edgeExaminations = edgeExaminations;
        this.comparisons = comparisons;
        this.auxiliaryOperations = auxiliaryOperations;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public boolean isConnected() {
        return connected;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getAuxiliaryOperations() {
        return auxiliaryOperations;
    }

    public long getTotalOperations() {
        return edgeExaminations + comparisons + auxiliaryOperations;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] Connected: %b, MST Edges: %d, Weight: %.2f, Time: %.3f ms, Ops: %d",
                algorithm, connected, edges.size(), totalWeight, getExecutionTimeMs(), getTotalOperations()
        );
    }
}
