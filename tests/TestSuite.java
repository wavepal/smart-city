package tests;

import connectivity.KosarajuSCC;
import connectivity.TopologicalSort;
import dsu.DisjointSetUnion;
import graph.Graph;
import graph.GraphGenerator;
import mst.KruskalMST;
import mst.PrimMST;
import shortestpath.Dijkstra;

import java.util.*;

public class TestSuite {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  Smart City Infrastructure Algorithm Test Suite  ");
        System.out.println("==================================================");

        runAllTests();

        System.out.println("\n--------------------------------------------------");
        System.out.printf("Test Summary: %d / %d tests passed.%n", testsPassed, totalTests);
        if (testsPassed == totalTests) {
            System.out.println("All tests passed.");
        } else {
            System.err.println("Some tests failed.");
            System.exit(1);
        }
        System.out.println("--------------------------------------------------");
    }

    public static void runAllTests() {
        testSingleVertexGraph();
        testSmallGraphCorrectness();
        testDisconnectedGraphMSTAndDijkstra();
        testGraphWithCyclesTopologicalSort();
        testValidDAGTopologicalSort();
        testMultipleValidMSTsAndDuplicateWeights();
        testUnreachableVerticesDijkstra();
        testNegativeWeightDijkstraThrows();
        testKruskalAndPrimEquivalenceAcrossRandomGraphs();
        testSCCEveryVertexInExactlyOneComponent();
        testDSUCorrectness();
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        totalTests++;
        if (condition) {
            testsPassed++;
            System.out.printf("[PASS] %s%n", testName);
        } else {
            System.err.printf("[FAIL] %s: %s%n", testName, message);
        }
    }

    private static void assertEquals(String testName, double expected, double actual, double eps) {
        totalTests++;
        if (Math.abs(expected - actual) <= eps) {
            testsPassed++;
            System.out.printf("[PASS] %s (expected=%.2f, actual=%.2f)%n", testName, expected, actual);
        } else {
            System.err.printf("[FAIL] %s: expected %.4f but got %.4f%n", testName, expected, actual);
        }
    }

    private static void testSingleVertexGraph() {
        Graph g = new Graph(1, false);
        KruskalMST kRes = new KruskalMST(g);
        PrimMST pRes = new PrimMST(g);
        assertTrue("Single vertex Kruskal connected", kRes.isConnected(), "Should be connected");
        assertTrue("Single vertex Prim connected", pRes.isConnected(), "Should be connected");
        assertEquals("Single vertex MST weight", 0.0, kRes.getTotalWeight(), 1e-6);
        assertEquals("Single vertex Prim weight", 0.0, pRes.getTotalWeight(), 1e-6);

        Graph directed = new Graph(1, true);
        TopologicalSort topo = new TopologicalSort(directed);
        assertTrue("Single vertex topological sort is DAG", topo.isDAG(), "Should be DAG");
        assertTrue("Single vertex topo order size is 1", topo.getOrder().size() == 1, "Order size should be 1");

        KosarajuSCC scc = new KosarajuSCC(directed);
        assertTrue("Single vertex has 1 SCC", scc.getComponentCount() == 1, "Should have 1 component");

        Dijkstra sp = new Dijkstra(directed, 0);
        assertEquals("Single vertex shortest path distance to self", 0.0, sp.getDistance(0), 1e-6);
    }

    private static void testSmallGraphCorrectness() {
        // Triangle with tail: 0-1 (1), 1-2 (2), 0-2 (4), 2-3 (3)
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1.0);
        g.addEdge(1, 2, 2.0);
        g.addEdge(0, 2, 4.0);
        g.addEdge(2, 3, 3.0);

        KruskalMST kRes = new KruskalMST(g);
        PrimMST pRes = new PrimMST(g);

        assertEquals("Small graph Kruskal weight", 6.0, kRes.getTotalWeight(), 1e-6);
        assertEquals("Small graph Prim weight", 6.0, pRes.getTotalWeight(), 1e-6);
        assertTrue("Small graph Kruskal connected", kRes.isConnected(), "Must be connected");
        assertTrue("Small graph Prim connected", pRes.isConnected(), "Must be connected");

        // Directed version for Dijkstra
        Graph dg = new Graph(4, true);
        dg.addEdge(0, 1, 1.0);
        dg.addEdge(1, 2, 2.0);
        dg.addEdge(0, 2, 4.0);
        dg.addEdge(2, 3, 3.0);

        Dijkstra sp = new Dijkstra(dg, 0);
        assertEquals("Dijkstra dist 0->2 (via 1)", 3.0, sp.getDistance(2), 1e-6);
        assertEquals("Dijkstra dist 0->3", 6.0, sp.getDistance(3), 1e-6);
        List<Integer> path = sp.getPath(3);
        assertTrue("Dijkstra path 0->3", path.equals(Arrays.asList(0, 1, 2, 3)), "Path should be 0->1->2->3");
    }

    private static void testDisconnectedGraphMSTAndDijkstra() {
        // Component 1: 0-1-2. Component 2: 3-4.
        Graph g = new Graph(5, false);
        g.addEdge(0, 1, 10.0);
        g.addEdge(1, 2, 20.0);
        g.addEdge(3, 4, 30.0);

        KruskalMST kRes = new KruskalMST(g);
        PrimMST pRes = new PrimMST(g);

        assertTrue("Disconnected graph Kruskal detects disconnected", !kRes.isConnected(), "Should detect disconnected");
        assertTrue("Disconnected graph Prim detects disconnected", !pRes.isConnected(), "Should detect disconnected");

        Dijkstra sp = new Dijkstra(g, 0);
        assertTrue("Disconnected vertex 3 is unreachable", !sp.hasPath(3), "Vertex 3 should be unreachable");
        assertTrue("Disconnected vertex 4 is unreachable", !sp.hasPath(4), "Vertex 4 should be unreachable");
        assertTrue("Path to unreachable vertex is empty", sp.getPath(3).isEmpty(), "Path should be empty");
    }

    private static void testGraphWithCyclesTopologicalSort() {
        // 0 -> 1 -> 2 -> 0 (cycle)
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1.0);
        g.addEdge(1, 2, 1.0);
        g.addEdge(2, 0, 1.0);

        TopologicalSort topo = new TopologicalSort(g);
        assertTrue("Cyclic graph hasCycle is true", topo.hasCycle(), "Cycle must be detected");
        assertTrue("Cyclic graph order is empty", topo.getOrder().isEmpty(), "Order must be empty on cyclic graph");

        KosarajuSCC scc = new KosarajuSCC(g);
        assertEquals("Cycle graph has 1 SCC", 1.0, scc.getComponentCount(), 1e-6);
        assertTrue("All vertices in same SCC", scc.getComponents().get(0).size() == 3, "SCC must have 3 vertices");
    }

    private static void testValidDAGTopologicalSort() {
        // Diamond DAG: 0->1, 0->2, 1->3, 2->3
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 2.0);
        g.addEdge(0, 2, 4.0);
        g.addEdge(1, 3, 3.0);
        g.addEdge(2, 3, 1.0);

        TopologicalSort topo = new TopologicalSort(g);
        assertTrue("Valid DAG hasCycle is false", !topo.hasCycle(), "Should not have cycle");
        assertTrue("Valid DAG order is valid", TopologicalSort.isValidOrder(g, topo.getOrder()), "Order must satisfy dependencies");
    }

    private static void testMultipleValidMSTsAndDuplicateWeights() {
        // Square: 0-1, 1-2, 2-3, 3-0, all weights = 5.0
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 5.0);
        g.addEdge(1, 2, 5.0);
        g.addEdge(2, 3, 5.0);
        g.addEdge(3, 0, 5.0);

        KruskalMST kRes = new KruskalMST(g);
        PrimMST pRes = new PrimMST(g);

        assertTrue("Square Kruskal is connected", kRes.isConnected(), "Should be connected");
        assertTrue("Square Prim is connected", pRes.isConnected(), "Should be connected");
        assertEquals("Kruskal MST weight on duplicate weights", 15.0, kRes.getTotalWeight(), 1e-6);
        assertEquals("Prim MST weight on duplicate weights", 15.0, pRes.getTotalWeight(), 1e-6);
        assertEquals("MST edge count is V - 1", 3.0, kRes.getEdges().size(), 1e-6);
    }

    private static void testUnreachableVerticesDijkstra() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 2.0);
        g.addEdge(1, 2, 3.0);
        // Vertex 3 has no incoming or outgoing edges

        Dijkstra sp = new Dijkstra(g, 0);
        assertEquals("Distance to vertex 2", 5.0, sp.getDistance(2), 1e-6);
        assertTrue("Vertex 3 is unreachable", !sp.hasPath(3), "Should be unreachable");
        assertTrue("Vertex 3 distance is positive infinity",
                Double.isInfinite(sp.getDistance(3)), "Distance must be infinity");
    }

    private static void testNegativeWeightDijkstraThrows() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 5.0);
        g.addEdge(1, 2, -2.0);

        boolean caught = false;
        try {
            new Dijkstra(g, 0);
        } catch (IllegalArgumentException ex) {
            caught = true;
        }
        assertTrue("Dijkstra rejects negative edge weights", caught, "Must throw IllegalArgumentException");
    }

    private static void testKruskalAndPrimEquivalenceAcrossRandomGraphs() {
        int[] vertexSizes = {10, 25, 50, 100};
        for (int V : vertexSizes) {
            int E = V * 3;
            for (long seed = 100; seed <= 103; seed++) {
                Graph g = GraphGenerator.generateConnectedUndirectedGraph(V, E, seed, 1.0, 100.0);
                KruskalMST k = new KruskalMST(g);
                PrimMST p = new PrimMST(g);

                assertTrue("Random graph " + V + " Kruskal connected", k.isConnected(), "Must be connected");
                assertTrue("Random graph " + V + " Prim connected", p.isConnected(), "Must be connected");
                assertEquals("MST weight equality (V=" + V + ", seed=" + seed + ")",
                        k.getTotalWeight(), p.getTotalWeight(), 1e-4);
            }
        }
    }

    private static void testSCCEveryVertexInExactlyOneComponent() {
        int V = 50;
        int E = 150;
        Graph g = GraphGenerator.generateDirectedGraphWithCycles(V, E, 42L, 1.0, 10.0);

        KosarajuSCC scc = new KosarajuSCC(g);
        int totalVerticesInComponents = 0;
        boolean[] seen = new boolean[V];

        for (List<Integer> comp : scc.getComponents()) {
            totalVerticesInComponents += comp.size();
            for (int v : comp) {
                assertTrue("Vertex " + v + " belongs to only one SCC", !seen[v], "Must not appear twice");
                seen[v] = true;
            }
        }

        assertTrue("All V vertices accounted for in SCCs", totalVerticesInComponents == V,
                "Total vertices in SCCs must equal V");
    }

    private static void testDSUCorrectness() {
        DisjointSetUnion dsu = new DisjointSetUnion(10);
        assertEquals("Initial set count", 10.0, dsu.getCount(), 1e-6);

        dsu.union(0, 1);
        dsu.union(1, 2);
        assertTrue("0 and 2 are connected", dsu.connected(0, 2), "Should be connected");
        assertTrue("0 and 3 are not connected", !dsu.connected(0, 3), "Should not be connected");
        assertEquals("Set count after 2 unions", 8.0, dsu.getCount(), 1e-6);

        dsu.union(3, 4);
        dsu.union(2, 4);
        assertTrue("0 and 4 are now connected", dsu.connected(0, 4), "Should be connected");
        assertEquals("Size of component containing 0", 5.0, dsu.getSize(0), 1e-6);
    }
}
