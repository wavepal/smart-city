package connectivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SCCResult {
    private final List<List<Integer>> components;
    private final int[] componentIds;
    private final long executionTimeNs;
    private final long dfsVisits;
    private final long edgeExaminations;

    public SCCResult(List<List<Integer>> components, int[] componentIds,
                     long executionTimeNs, long dfsVisits, long edgeExaminations) {
        this.components = new ArrayList<>();
        for (List<Integer> comp : components) {
            this.components.add(new ArrayList<>(comp));
        }
        this.componentIds = componentIds.clone();
        this.executionTimeNs = executionTimeNs;
        this.dfsVisits = dfsVisits;
        this.edgeExaminations = edgeExaminations;
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

    public int[] getComponentIds() {
        return componentIds.clone();
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
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

    @Override
    public String toString() {
        return String.format("[SCC] Components: %d, Time: %.3f ms, Ops: %d",
                components.size(), getExecutionTimeMs(), getTotalOperations());
    }
}
