package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class TopologicalSort {

    /**
     * Finds a topological ordering using Kahn's algorithm.
     */
    public static TopologicalSortResult sort(Graph graph) {
        int V = graph.getNumVertices();
        if (V == 0) {
            return new TopologicalSortResult(false, Collections.emptyList(), 0, 0, 0);
        }

        long startTime = System.nanoTime();

        int[] inDegree = new int[V];
        long edgeExaminations = 0;
        long vertexVisits = 0;

        // Compute indegrees of all vertices
        for (int u = 0; u < V; u++) {
            for (Edge edge : graph.getIncidentEdges(u)) {
                edgeExaminations++;
                inDegree[edge.getTo()]++;
            }
        }

        // Add all vertices with zero indegree to queue
        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < V; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> order = new ArrayList<>(V);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            vertexVisits++;
            order.add(u);

            // Reduce indegree for each neighbor
            for (Edge edge : graph.getIncidentEdges(u)) {
                edgeExaminations++;
                int v = edge.getTo();
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.add(v);
                }
            }
        }

        long endTime = System.nanoTime();

        // If processed vertices count is less than V, there is a cycle
        boolean hasCycle = (order.size() < V);

        return new TopologicalSortResult(
                hasCycle,
                hasCycle ? Collections.emptyList() : order,
                endTime - startTime,
                vertexVisits,
                edgeExaminations
        );
    }

    /**
     * Checks if a given ordering is a valid topological ordering of the graph.
     */
    public static boolean isValidOrder(Graph graph, List<Integer> order) {
        if (order.size() != graph.getNumVertices()) {
            return false;
        }

        int[] position = new int[graph.getNumVertices()];
        for (int i = 0; i < order.size(); i++) {
            position[order.get(i)] = i;
        }

        for (int u = 0; u < graph.getNumVertices(); u++) {
            for (Edge edge : graph.getIncidentEdges(u)) {
                int v = edge.getTo();
                // In valid topological order, u must appear before v
                if (position[u] >= position[v]) {
                    return false;
                }
            }
        }

        return true;
    }
}
