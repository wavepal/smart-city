package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class KosarajuSCC {

    public static SCCResult findSCC(Graph graph) {
        int V = graph.getNumVertices();
        if (V == 0) {
            return new SCCResult(Collections.emptyList(), new int[0], 0, 0, 0);
        }

        long startTime = System.nanoTime();

        boolean[] visited = new boolean[V];
        Deque<Integer> finishOrder = new ArrayDeque<>();
        long[] counters = new long[2]; // [0] = visits, [1] = edgeExaminations

        // First pass: fill stack with vertices by finish time
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                dfsOrder(i, graph, visited, finishOrder, counters);
            }
        }

        // Get transposed graph with reversed edges
        Graph transposed = graph.getTransposed();

        Arrays.fill(visited, false);
        List<List<Integer>> components = new ArrayList<>();
        int[] componentIds = new int[V];

        // Second pass: process vertices from stack on transposed graph
        while (!finishOrder.isEmpty()) {
            int u = finishOrder.pop();
            if (!visited[u]) {
                List<Integer> component = new ArrayList<>();
                int sccId = components.size();
                dfsCollect(u, transposed, visited, component, componentIds, sccId, counters);
                components.add(component);
            }
        }

        long endTime = System.nanoTime();

        return new SCCResult(
                components,
                componentIds,
                endTime - startTime,
                counters[0],
                counters[1]
        );
    }

    private static void dfsOrder(int u, Graph graph, boolean[] visited, Deque<Integer> stack, long[] counters) {
        visited[u] = true;
        counters[0]++;

        for (Edge edge : graph.getIncidentEdges(u)) {
            counters[1]++;
            int v = edge.getTo();
            if (!visited[v]) {
                dfsOrder(v, graph, visited, stack, counters);
            }
        }
        stack.push(u);
    }

    private static void dfsCollect(int u, Graph transposed, boolean[] visited,
                                   List<Integer> component, int[] componentIds, int sccId, long[] counters) {
        visited[u] = true;
        counters[0]++;
        component.add(u);
        componentIds[u] = sccId;

        for (Edge edge : transposed.getIncidentEdges(u)) {
            counters[1]++;
            int v = edge.getTo();
            if (!visited[v]) {
                dfsCollect(v, transposed, visited, component, componentIds, sccId, counters);
            }
        }
    }
}
