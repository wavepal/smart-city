package dsu;

public class DisjointSetUnion {
    private final int[] parent;
    private final int[] rank;
    private final int[] size;
    private int setCount;

    private final boolean usePathCompression;
    private final boolean useUnionByRank;

    // Operation counters for benchmarking
    private long findCalls;
    private long unionCalls;
    private long pathCompressions;
    private long rootLookups;

    public DisjointSetUnion(int n) {
        this(n, true, true);
    }

    public DisjointSetUnion(int n, boolean usePathCompression, boolean useUnionByRank) {
        if (n < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        this.parent = new int[n];
        this.rank = new int[n];
        this.size = new int[n];
        this.setCount = n;
        this.usePathCompression = usePathCompression;
        this.useUnionByRank = useUnionByRank;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
            size[i] = 1;
        }
    }

    public int find(int u) {
        findCalls++;
        if (u < 0 || u >= parent.length) {
            throw new IndexOutOfBoundsException("Element " + u + " is out of bounds");
        }

        if (usePathCompression) {
            // Find root with path compression
            if (parent[u] != u) {
                rootLookups++;
                int root = find(parent[u]);
                if (parent[u] != root) {
                    pathCompressions++;
                    parent[u] = root;
                }
            }
            return parent[u];
        } else {
            // Simple lookup without path compression
            int curr = u;
            while (curr != parent[curr]) {
                rootLookups++;
                curr = parent[curr];
            }
            return curr;
        }
    }

    public boolean union(int u, int v) {
        unionCalls++;
        int rootU = find(u);
        int rootV = find(v);

        // Already in the same set
        if (rootU == rootV) {
            return false;
        }

        if (useUnionByRank) {
            // Attach smaller tree under root of larger tree
            if (rank[rootU] < rank[rootV]) {
                parent[rootU] = rootV;
                size[rootV] += size[rootU];
            } else if (rank[rootU] > rank[rootV]) {
                parent[rootV] = rootU;
                size[rootU] += size[rootV];
            } else {
                parent[rootV] = rootU;
                rank[rootU]++;
                size[rootU] += size[rootV];
            }
        } else {
            // Naive union: always make rootV child of rootU
            parent[rootV] = rootU;
            size[rootU] += size[rootV];
        }

        setCount--;
        return true;
    }

    public boolean connected(int u, int v) {
        return find(u) == find(v);
    }

    public int getCount() {
        return setCount;
    }

    public int getSize(int u) {
        return size[find(u)];
    }

    public void resetMetrics() {
        findCalls = 0;
        unionCalls = 0;
        pathCompressions = 0;
        rootLookups = 0;
    }

    public long getFindCalls() {
        return findCalls;
    }

    public long getUnionCalls() {
        return unionCalls;
    }

    public long getPathCompressions() {
        return pathCompressions;
    }

    public long getRootLookups() {
        return rootLookups;
    }

    public long getTotalOperations() {
        return findCalls + unionCalls + rootLookups;
    }
}
