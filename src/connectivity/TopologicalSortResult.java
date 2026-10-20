package connectivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopologicalSortResult {
    private final boolean hasCycle;
    private final List<Integer> order;
    private final long executionTimeNs;
    private final long vertexVisits;
    private final long edgeExaminations;

    public TopologicalSortResult(boolean hasCycle, List<Integer> order,
                                 long executionTimeNs, long vertexVisits, long edgeExaminations) {
        this.hasCycle = hasCycle;
        this.order = new ArrayList<>(order);
        this.executionTimeNs = executionTimeNs;
        this.vertexVisits = vertexVisits;
        this.edgeExaminations = edgeExaminations;
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

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
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

    @Override
    public String toString() {
        if (hasCycle) {
            return String.format("[TopologicalSort] Graph contains cycle(s). Time: %.3f ms, Ops: %d",
                    getExecutionTimeMs(), getTotalOperations());
        }
        return String.format("[TopologicalSort] Valid DAG. Vertices: %d, Time: %.3f ms, Ops: %d",
                order.size(), getExecutionTimeMs(), getTotalOperations());
    }
}
