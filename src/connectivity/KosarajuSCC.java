package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class KosarajuSCC {
    private final List<List<Integer>> components = new ArrayList<>();
    private final int[] componentIds;
    private long dfsVisits = 0;
    private long edgeExaminations = 0;
    private long executionTimeNs = 0;

    public KosarajuSCC(Graph graph) {
        int V = graph.getNumVertices();
        this.componentIds = new int[V];
        if (V == 0) return;

        long startTime = System.nanoTime();

        boolean[] visited = new boolean[V];
        Deque<Integer> finishOrder = new ArrayDeque<>();

        // First pass: fill stack with vertices by finish time
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                dfsOrder(i, graph, visited, finishOrder);
            }
        }

        // Get transposed graph with reversed edges
        Graph transposed = graph.getTransposed();
        Arrays.fill(visited, false);

        // Second pass: process vertices from stack on transposed graph
        while (!finishOrder.isEmpty()) {
            int u = finishOrder.pop();
            if (!visited[u]) {
                List<Integer> component = new ArrayList<>();
                int sccId = components.size();
                dfsCollect(u, transposed, visited, component, sccId);
                components.add(component);
            }
        }

        long endTime = System.nanoTime();
        this.executionTimeNs = endTime - startTime;
    }

    private void dfsOrder(int u, Graph graph, boolean[] visited, Deque<Integer> stack) {
        visited[u] = true;
        dfsVisits++;

        for (Edge edge : graph.getIncidentEdges(u)) {
            edgeExaminations++;
            int v = edge.getTo();
            if (!visited[v]) {
                dfsOrder(v, graph, visited, stack);
            }
        }
        stack.push(u);
    }

    private void dfsCollect(int u, Graph transposed, boolean[] visited, List<Integer> component, int sccId) {
        visited[u] = true;
        dfsVisits++;
        component.add(u);
        componentIds[u] = sccId;

        for (Edge edge : transposed.getIncidentEdges(u)) {
            edgeExaminations++;
            int v = edge.getTo();
            if (!visited[v]) {
                dfsCollect(v, transposed, visited, component, sccId);
            }
        }
    }

    public List<List<Integer>> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public int getComponentCount() {
        return components.size();
    }

    public int getComponentId(int vertex) {
        return componentIds[vertex];
    }

    public long getDfsVisits() {
        return dfsVisits;
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getTotalOperations() {
        return dfsVisits + edgeExaminations;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }
}
