package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShortestPathResult {
    private final int source;
    private final double[] distances;
    private final int[] parent;
    private final long executionTimeNs;
    private final long edgeRelaxations;
    private final long pqOperations;
    private final long comparisons;

    public ShortestPathResult(int source, double[] distances, int[] parent,
                              long executionTimeNs, long edgeRelaxations, long pqOperations, long comparisons) {
        this.source = source;
        this.distances = distances.clone();
        this.parent = parent.clone();
        this.executionTimeNs = executionTimeNs;
        this.edgeRelaxations = edgeRelaxations;
        this.pqOperations = pqOperations;
        this.comparisons = comparisons;
    }

    public int getSource() {
        return source;
    }

    public double getDistance(int target) {
        return distances[target];
    }

    public boolean hasPathTo(int target) {
        return distances[target] != Double.POSITIVE_INFINITY;
    }

    /**
     * Reconstructs the shortest path from source to target.
     */
    public List<Integer> getPathTo(int target) {
        if (!hasPathTo(target)) {
            return Collections.emptyList();
        }

        List<Integer> path = new ArrayList<>();
        for (int at = target; at != -1; at = parent[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    public String getFormattedPath(int target) {
        if (!hasPathTo(target)) {
            return "Unreachable";
        }
        List<Integer> path = getPathTo(target);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) sb.append(" -> ");
        }
        sb.append(String.format(" (distance: %.2f)", distances[target]));
        return sb.toString();
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }

    public long getEdgeRelaxations() {
        return edgeRelaxations;
    }

    public long getPqOperations() {
        return pqOperations;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getTotalOperations() {
        return edgeRelaxations + pqOperations + comparisons;
    }
}
