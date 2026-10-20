package mst;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class PrimMST {

    public static MSTResult findMST(Graph graph) {
        int V = graph.getNumVertices();
        if (V == 0) {
            return new MSTResult("Prim", Collections.emptyList(), 0.0, true, 0, 0, 0, 0);
        }

        long startTime = System.nanoTime();

        boolean[] inMST = new boolean[V];
        List<Edge> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;

        long[] comparisons = new long[1];
        long edgeExaminations = 0;
        long pqOperations = 0;

        PriorityQueue<Edge> pq = new PriorityQueue<>((e1, e2) -> {
            comparisons[0]++;
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

        // Check if all vertices are connected
        boolean isConnected = (V <= 1) || (visitedVertices == V && mstEdges.size() == V - 1);

        return new MSTResult(
                "Prim",
                mstEdges,
                totalWeight,
                isConnected,
                endTime - startTime,
                edgeExaminations,
                comparisons[0],
                pqOperations
        );
    }
}
