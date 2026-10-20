package graph;

import java.util.Objects;

/**
 * Represents a connection or road between two city districts in the smart city infrastructure network.
 */
public class Edge implements Comparable<Edge> {
    private final int from;
    private final int to;
    private final double weight;
    private final boolean directed;
    private final String label;

    public Edge(int from, int to, double weight) {
        this(from, to, weight, false, "");
    }

    public Edge(int from, int to, double weight, boolean directed) {
        this(from, to, weight, directed, "");
    }

    public Edge(int from, int to, double weight, boolean directed, String label) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.directed = directed;
        this.label = (label == null) ? "" : label;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isDirected() {
        return directed;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns an edge in the reverse direction with the same weight.
     */
    public Edge reversed() {
        return new Edge(to, from, weight, directed, label.isEmpty() ? "" : label + " (reversed)");
    }

    @Override
    public int compareTo(Edge other) {
        return Double.compare(this.weight, other.weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        if (directed) {
            return from == edge.from && to == edge.to && Double.compare(edge.weight, weight) == 0;
        } else {
            return ((from == edge.from && to == edge.to) || (from == edge.to && to == edge.from))
                    && Double.compare(edge.weight, weight) == 0;
        }
    }

    @Override
    public int hashCode() {
        if (directed) {
            return Objects.hash(from, to, weight, directed);
        } else {
            int minV = Math.min(from, to);
            int maxV = Math.max(from, to);
            return Objects.hash(minV, maxV, weight);
        }
    }

    @Override
    public String toString() {
        String arrow = directed ? " -> " : " <-> ";
        return label.isEmpty()
                ? String.format("(%d%s%d, weight=%.2f)", from, arrow, to, weight)
                : String.format("[%s: %d%s%d, weight=%.2f]", label, from, arrow, to, weight);
    }
}
