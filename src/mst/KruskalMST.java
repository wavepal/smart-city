package mst;

import dsu.DisjointSetUnion;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KruskalMST {

    public static MSTResult findMST(Graph graph) {
        return findMST(graph, true, true);
    }

    public static MSTResult findMST(Graph graph, boolean usePathCompression, boolean useUnionByRank) {
        int V = graph.getNumVertices();
        if (V == 0) {
            return new MSTResult("Kruskal", Collections.emptyList(), 0.0, true, 0, 0, 0, 0);
        }

        long startTime = System.nanoTime();

        List<Edge> allEdges = new ArrayList<>(graph.getAllEdges());
        long[] comparisonCount = new long[1];

        // Sort all edges by weight
        allEdges.sort((e1, e2) -> {
            comparisonCount[0]++;
            return Double.compare(e1.getWeight(), e2.getWeight());
        });

        DisjointSetUnion dsu = new DisjointSetUnion(V, usePathCompression, useUnionByRank);
        List<Edge> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;
        long edgeExaminations = 0;

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

        // Graph is connected if MST has V - 1 edges
        boolean isConnected = (V <= 1) || (mstEdges.size() == V - 1);
        long dsuOps = dsu.getTotalOperations();

        return new MSTResult(
                "Kruskal",
                mstEdges,
                totalWeight,
                isConnected,
                endTime - startTime,
                edgeExaminations,
                comparisonCount[0],
                dsuOps
        );
    }
}
