package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class TarjanSCC {

    public static SCCResult findSCC(Graph graph) {
        int V = graph.getNumVertices();
        if (V == 0) {
            return new SCCResult(Collections.emptyList(), new int[0], 0, 0, 0);
        }

        long startTime = System.nanoTime();

        int[] ids = new int[V];
        int[] low = new int[V];
        boolean[] onStack = new boolean[V];
        Arrays.fill(ids, -1);

        Deque<Integer> stack = new ArrayDeque<>();
        List<List<Integer>> components = new ArrayList<>();
        int[] componentIds = new int[V];

        long[] counters = new long[3]; // [0] = time, [1] = dfsVisits, [2] = edgeExaminations

        // Run DFS from all unvisited vertices
        for (int i = 0; i < V; i++) {
            if (ids[i] == -1) {
                dfs(i, graph, ids, low, onStack, stack, components, componentIds, counters);
            }
        }

        long endTime = System.nanoTime();

        return new SCCResult(
                components,
                componentIds,
                endTime - startTime,
                counters[1],
                counters[2]
        );
    }

    private static void dfs(int u, Graph graph, int[] ids, int[] low, boolean[] onStack,
                            Deque<Integer> stack, List<List<Integer>> components, int[] componentIds,
                            long[] counters) {
        ids[u] = low[u] = (int) (counters[0]++);
        stack.push(u);
        onStack[u] = true;
        counters[1]++; // dfs visits

        for (Edge edge : graph.getIncidentEdges(u)) {
            counters[2]++; // edge examinations
            int v = edge.getTo();

            if (ids[v] == -1) {
                // Visit unvisited neighbor
                dfs(v, graph, ids, low, onStack, stack, components, componentIds, counters);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // Back-edge to an ancestor on stack
                low[u] = Math.min(low[u], ids[v]);
            }
        }

        // When u is the root of an SCC, pop all vertices in this component
        if (ids[u] == low[u]) {
            List<Integer> comp = new ArrayList<>();
            int sccIndex = components.size();
            while (true) {
                int node = stack.pop();
                onStack[node] = false;
                comp.add(node);
                componentIds[node] = sccIndex;
                if (node == u) break;
            }
            components.add(comp);
        }
    }
}
