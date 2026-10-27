package mst;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class PrimMST {
    private final List<Edge> mstEdges = new ArrayList<>();
    private double totalWeight = 0.0;
    private boolean connected = true;

    // Operation counters
    private long edgeExaminations = 0;
    private long comparisons = 0;
    private long pqOperations = 0;
    private long executionTimeNs = 0;

    public PrimMST(Graph graph) {
        int V = graph.getNumVertices();
        if (V <= 1) {
            this.connected = true;
            this.totalWeight = 0.0;
            return;
        }

        long startTime = System.nanoTime();

        boolean[] inMST = new boolean[V];
        PriorityQueue<Edge> pq = new PriorityQueue<>((e1, e2) -> {
            comparisons++;
            return Double.compare(e1.getWeight(), e2.getWeight());
        });

        // Start from first vertex
        inMST[0] = true;
        int visitedVertices = 1;

        for (Edge edge : graph.getIncidentEdges(0)) {
            edgeExaminations++;
            pq.add(edge);
            pqOperations++;
        }

        while (!pq.isEmpty() && mstEdges.size() < V - 1) {
            Edge minEdge = pq.poll();
            pqOperations++;

            int u = minEdge.getFrom();
            int v = minEdge.getTo();

            // Skip if both endpoints are already in the tree
            if (inMST[u] && inMST[v]) {
                continue;
            }

            int nextVertex = inMST[u] ? v : u;
            inMST[nextVertex] = true;
            visitedVertices++;

            mstEdges.add(minEdge);
            totalWeight += minEdge.getWeight();

            // Add new incident edges to priority queue
            for (Edge edge : graph.getIncidentEdges(nextVertex)) {
                edgeExaminations++;
                int other = (edge.getFrom() == nextVertex) ? edge.getTo() : edge.getFrom();
                if (!inMST[other]) {
                    pq.add(edge);
                    pqOperations++;
                }
            }
        }

        long endTime = System.nanoTime();
        this.executionTimeNs = endTime - startTime;
        this.connected = (visitedVertices == V && mstEdges.size() == V - 1);
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

    public long getPqOperations() {
        return pqOperations;
    }

    public long getTotalOperations() {
        return edgeExaminations + comparisons + pqOperations;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }
}
