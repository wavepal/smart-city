package graph;

import java.util.*;

/**
 * Models the smart city infrastructure network as a graph of city districts (vertices)
 * connected by roads or service connections (edges).
 */
public class Graph {
    private final int numVertices;
    private final boolean directed;
    private final List<List<Edge>> adjacencyList;
    private final List<Edge> edges;
    private final String[] districtNames;

    public Graph(int numVertices, boolean directed) {
        if (numVertices < 0) {
            throw new IllegalArgumentException("Number of vertices cannot be negative.");
        }
        this.numVertices = numVertices;
        this.directed = directed;
        this.adjacencyList = new ArrayList<>(numVertices);
        this.edges = new ArrayList<>();
        this.districtNames = new String[numVertices];

        for (int i = 0; i < numVertices; i++) {
            this.adjacencyList.add(new ArrayList<>());
            this.districtNames[i] = "District-" + i;
        }
    }

    public Graph(int numVertices) {
        this(numVertices, false);
    }

    public void setDistrictName(int districtId, String name) {
        validateVertex(districtId);
        districtNames[districtId] = name;
    }

    public String getDistrictName(int districtId) {
        validateVertex(districtId);
        return districtNames[districtId];
    }

    public void addEdge(int from, int to, double weight) {
        addEdge(new Edge(from, to, weight, directed));
    }

    public void addEdge(int from, int to, double weight, String label) {
        addEdge(new Edge(from, to, weight, directed, label));
    }

    public void addEdge(Edge edge) {
        validateVertex(edge.getFrom());
        validateVertex(edge.getTo());

        adjacencyList.get(edge.getFrom()).add(edge);
        if (!directed) {
            adjacencyList.get(edge.getTo()).add(edge.reversed());
        }
        edges.add(edge);
    }

    public List<Edge> getIncidentEdges(int vertex) {
        validateVertex(vertex);
        return Collections.unmodifiableList(adjacencyList.get(vertex));
    }

    public List<Edge> getAllEdges() {
        return Collections.unmodifiableList(edges);
    }

    public int getNumVertices() {
        return numVertices;
    }

    public int getNumEdges() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    /**
     * Creates and returns the transposed (reversed) graph.
     * Useful for connectivity algorithms such as Kosaraju's SCC.
     */
    public Graph getTransposed() {
        Graph transposed = new Graph(numVertices, directed);
        for (int i = 0; i < numVertices; i++) {
            transposed.setDistrictName(i, districtNames[i]);
        }
        for (Edge edge : edges) {
            transposed.addEdge(edge.getTo(), edge.getFrom(), edge.getWeight(), edge.getLabel());
        }
        return transposed;
    }

    /**
     * Checks whether an undirected graph is fully connected using Breadth-First Search.
     */
    public boolean isConnectedUndirected() {
        if (numVertices == 0) return true;
        if (numVertices == 1) return true;
        if (directed) {
            throw new IllegalStateException("isConnectedUndirected is only applicable for undirected graphs.");
        }

        boolean[] visited = new boolean[numVertices];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        visited[0] = true;
        int visitedCount = 1;

        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (Edge edge : adjacencyList.get(current)) {
                int neighbor = edge.getTo();
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    visitedCount++;
                    queue.add(neighbor);
                }
            }
        }

        return visitedCount == numVertices;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= numVertices) {
            throw new IndexOutOfBoundsException("Vertex index " + v + " is out of bounds [0, " + (numVertices - 1) + "]");
        }
    }
}
