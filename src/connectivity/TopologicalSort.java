package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class TopologicalSort {
    private final List<Integer> order = new ArrayList<>();
    private final boolean hasCycle;
    private long vertexVisits = 0;
    private long edgeExaminations = 0;
    private long executionTimeNs = 0;

    public TopologicalSort(Graph graph) {
        int V = graph.getNumVertices();
        if (V == 0) {
            this.hasCycle = false;
            return;
        }

        long startTime = System.nanoTime();

        int[] inDegree = new int[V];

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
        this.executionTimeNs = endTime - startTime;
        this.hasCycle = (order.size() < V);
        if (this.hasCycle) {
            order.clear();
        }
    }

    public boolean hasCycle() {
        return hasCycle;
    }

    public boolean isDAG() {
        return !hasCycle;
    }

    public List<Integer> getOrder() {
        return Collections.unmodifiableList(order);
    }

    public long getVertexVisits() {
        return vertexVisits;
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getTotalOperations() {
        return vertexVisits + edgeExaminations;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }

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
                if (position[u] >= position[v]) {
                    return false;
                }
            }
        }

        return true;
    }
}
