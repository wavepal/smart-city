# Assignment 3: Smart City Infrastructure and Route Planning

Java application for modeling city infrastructure networks using graph algorithms. It covers minimum spanning trees, connectivity analysis, and shortest-path route planning for municipal infrastructure networks.

---

## 1. Overview

The idea is to represent a smart city network as a graph where vertices represent city districts, utility substations, or transit hubs, and edges represent roads, power lines, or water pipelines.

The graph $G = (V, E)$ consists of:
* **Vertices ($V$):** City districts and locations
* **Edges ($E$):** Roads and infrastructure connections
* **Weights ($w$):** Road construction costs or travel distances (non-negative)

### Core Tasks

1. **Infrastructure Planning (MST):** Connect all districts at the minimum total cost without cycles using Kruskal's and Prim's algorithms.
2. **Connectivity Analysis:** Check if the network is connected, detect cycles, find topological ordering for service dependencies, and identify strongly connected components using Kosaraju's algorithm.
3. **Route Planning (Dijkstra):** Find shortest paths and distances from a municipal hub to all reachable districts.

---

## 2. Implementation Details

### Project Structure

```text
smart-city/
├── pom.xml
├── README.md
├── src/
│   ├── graph/
│   │   ├── Edge.java
│   │   ├── Graph.java
│   │   └── GraphGenerator.java
│   ├── dsu/
│   │   └── DisjointSetUnion.java
│   ├── mst/
│   │   ├── KruskalMST.java
│   │   └── PrimMST.java
│   ├── connectivity/
│   │   ├── TopologicalSort.java
│   │   └── KosarajuSCC.java
│   ├── shortestpath/
│   │   └── Dijkstra.java
│   ├── benchmark/
│   │   └── BenchmarkRunner.java
│   └── Main.java
├── tests/
│   └── TestSuite.java
├── results/
│   ├── tables/
│   │   ├── mst_benchmark.csv
│   │   ├── dsu_benchmark.csv
│   │   ├── topo_benchmark.csv
│   │   ├── scc_benchmark.csv
│   │   └── dijkstra_benchmark.csv
│   └── plots/
│       ├── execution_time_vs_vertices.png
│       ├── execution_time_vs_edges.png
│       ├── kruskal_vs_prim_comparison.png
│       ├── dsu_performance_comparison.png
│       └── dijkstra_operations.png
└── scripts/
    └── generate_plots.py
```

### Main Design Decisions

#### Graph Representation (`graph`)
* **Adjacency Lists:** Stored as `List<List<Edge>>`. For city networks which are sparse ($E \ll V^2$), adjacency lists use $O(V + E)$ space, avoiding the $O(V^2)$ overhead of an adjacency matrix.
* **Undirected Edge Handling:** For undirected graphs, each edge is added to the adjacency list of both endpoints for Prim's algorithm, but the global edge list stores only one instance of each edge so Kruskal's algorithm doesn't sort duplicates.
* **Graph Generator:** Uses a fixed random seed (42) for reproducible tests:
  * *Connected graphs:* First builds a random spanning tree over a shuffled vertex array to guarantee connectivity, then adds remaining edges without self-loops or duplicate edges.
  * *DAGs:* Generates edges $(u, v)$ with $u < v$ to guarantee acyclicity.
  * *Cyclic graphs:* Adds arbitrary directed edges and explicit cycles for connectivity testing.

#### Disjoint Set Union (`dsu`)
* **Path Compression:** During `find(u)`, points each visited node directly to the root.
* **Union by Rank:** Connects the root of the smaller tree to the root of the larger tree, keeping tree height bounded by $O(\log V)$ before compression.
* Combined, find and union run in nearly $O(1)$ amortized time ($O(\alpha(V))$, where $\alpha$ is the inverse Ackermann function).
* The class also supports running without optimizations to measure the performance penalty of naive tree structures.

#### Minimum Spanning Tree (`mst`)
* **Kruskal's Algorithm:** Sorts all unique edges by weight and adds edges if endpoints are in different DSU sets. It stops early once $V - 1$ edges are added. If fewer edges are added, it detects that the graph is disconnected.
* **Prim's Algorithm:** Uses Java's `PriorityQueue<Edge>` (binary min-heap). It starts from vertex 0, greedily picks the minimum weight edge to an unvisited vertex, and adds outgoing edges to the queue. If visited count is less than $V$, it flags the graph as disconnected.

#### Connectivity & SCC (`connectivity`)
* **Topological Sort:** Implements Kahn's algorithm using an in-degree array and a queue. If the number of processed vertices is less than $V$, the remaining vertices form a cycle (`hasCycle() == true`).
* **Strongly Connected Components:** I chose **Kosaraju's algorithm** for SCC because it is clean and intuitive:
  1. Run DFS on the original graph, recording vertices on a stack by finish time.
  2. Transpose the graph (reverse all directed edges).
  3. Pop vertices from the stack and run DFS on the transposed graph to collect each component.

#### Shortest Paths (`shortestpath`)
* **Dijkstra's Algorithm:** Uses a binary min-heap (`PriorityQueue`) of `(vertex, distance)` pairs and an array `dist[]` initialized to `Double.POSITIVE_INFINITY`.
* **Lazy Deletion:** When polling from the queue, `if (curr.distance > dist[u]) continue;` skips older, suboptimal queue entries. This prevents redundant relaxations and preserves $O((V + E) \log V)$ time.
* **Reconstruction:** Reconstructs the path from source to target using a `parent[]` array. Unreachable vertices have infinite distance and return an empty path list.
* **Negative Weight Check:** If any edge has $w < 0$, it throws an `IllegalArgumentException`.

---

## 3. Time and Space Complexity

| Algorithm | Assumptions / Data Structures | Time Complexity | Space Complexity |
| :--- | :--- | :---: | :---: |
| **DSU (find/union)** | Path compression + union by rank | $O(\alpha(V))$ amortized | $O(V)$ |
| **Kruskal's MST** | Edge list sorting + DSU | $O(E \log E) = O(E \log V)$ | $O(V + E)$ |
| **Prim's MST** | Adjacency list + binary min-heap | $O((V + E) \log V)$ | $O(V + E)$ |
| **Kosaraju's SCC** | Adjacency list + transposed graph + stack | $O(V + E)$ | $O(V + E)$ |
| **Topological Sort** | In-degree array + queue (Kahn's) | $O(V + E)$ | $O(V)$ |
| **Dijkstra's Algorithm** | Adjacency list + binary min-heap | $O((V + E) \log V)$ | $O(V + E)$ |

### Notes on Complexity

* **DSU:** With both path compression and union by rank, any sequence of $m$ operations on $n$ elements takes $O(m \cdot \alpha(n))$ time. In practice $\alpha(n) \le 4$. Without optimizations, trees can degrade into linear chains where each operation takes $O(V)$ worst case.
* **Kruskal:** Sorting $|E|$ edges takes $O(E \log E)$. Because $E \le V^2$, $\log E \le 2 \log V$, so sorting takes $O(E \log V)$. DSU operations take $O(E \cdot \alpha(V))$, which is dominated by sorting.
* **Prim:** Extracting $V$ vertices from the priority queue takes $O(V \log V)$, and relaxing $E$ edges takes $O(E \log V)$, giving $O((V + E) \log V)$.
* **Kosaraju:** Both DFS passes visit each vertex once and each edge once, so the total time is strictly linear $O(V + E)$.
* **Dijkstra:** With a binary heap, vertex extractions take $O(V \log V)$ and edge insertions take $O(E \log V)$, totaling $O((V + E) \log V)$.

### Why Dijkstra Fails with Negative Weights

Dijkstra's algorithm relies on a greedy invariant: once a vertex is extracted from the priority queue, its shortest distance is finalized and will not change.

With negative edge weights:
1. A longer path with a negative edge discovered later could result in a lower total distance to an already-finalized vertex. Because Dijkstra never re-evaluates visited vertices, it fails to find the true shortest path.
2. If the graph contains a negative cycle, shortest paths become undefined ($-\infty$) because traversing the cycle repeatedly lowers the path weight indefinitely. For negative weights, algorithms like Bellman-Ford are required.

---

## 4. Experimental Setup

The benchmarks were run on OpenJDK 26 (64-Bit VM).

### Configurations

Three graph sizes from the assignment specification were tested:

| Size | Vertices ($V$) | Edges ($E$) | Avg Degree ($2E/V$) | Density ($2E / (V(V-1))$) |
| :--- | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | 6.0 | 0.667 |
| **Medium** | 100 | 500 | 10.0 | 0.101 |
| **Large** | 1,000 | 5,000 | 10.0 | 0.010 |

### Methodology

1. **Fixed Seed:** Seed `42L` (+ offset per repeat) ensures all tests are reproducible.
2. **5 Repeats:** Every configuration is repeated 5 times; reported values are arithmetic averages.
3. **Warmup:** 50 warmup iterations run before timing to let the JVM JIT compiler stabilize.
4. **Timing:** Measured with `System.nanoTime()` around algorithm calls.
5. **Operation Counters:** Internal counters track edge examinations, key comparisons, and DSU/heap operations.

---

## 5. Experimental Results

### Minimum Spanning Tree: Kruskal vs. Prim

| Size | $V$ | $E$ | Algorithm | Time (ms) | Edge Exams | Comparisons | Aux / PQ Ops | Total Ops | MST Weight |
| :--- | :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | Kruskal | **0.0133** | 13 | 111 | 98 | 223 | **227.83** |
| | 10 | 30 | Prim | 0.0229 | 60 | 140 | 43 | 244 | **227.83** |
| **Medium** | 100 | 500 | Kruskal | **0.1601** | 290 | 3,836 | 2,071 | 6,198 | **1,161.90** |
| | 100 | 500 | Prim | 0.2583 | 1,000 | 5,477 | 790 | 7,268 | **1,161.90** |
| **Large** | 1,000 | 5,000 | Kruskal | **2.0004** | 3,652 | 55,158 | 25,574 | 84,385 | **12,260.45** |
| | 1,000 | 5,000 | Prim | 2.2687 | 10,000 | 90,342 | 8,652 | 108,995 | **12,260.45** |

*Check:* Both algorithms return the exact same MST weight across all graph sizes.

---

### DSU: Optimized vs. Naive

| Size | $V$ | $E$ | Mode | Time (ms) | DSU Ops | Ratio |
| :--- | :---: | :---: | :--- | :---: | :---: | :---: |
| **Small** | 10 | 30 | Optimized (PathComp + Rank) | 0.0157 | 98 | 1.0x |
| | 10 | 30 | Naive (No PathComp, No Rank) | 0.0266 | 90 | - |
| **Medium** | 100 | 500 | Optimized (PathComp + Rank) | 0.1567 | 2,071 | 2.95x fewer ops |
| | 100 | 500 | Naive (No PathComp, No Rank) | 0.1349 | 6,111 | - |
| **Large** | 1,000 | 5,000 | Optimized (PathComp + Rank) | **1.4527** | **25,574** | **27.4x fewer ops (1.62x faster)** |
| | 1,000 | 5,000 | Naive (No PathComp, No Rank) | 2.3574 | 700,483 | Degenerate chains |

---

### Topological Ordering: DAG vs. Cyclic

| Size | $V$ | $E$ | Graph Type | Status | Time (ms) | Vertex Visits | Edge Exams | Total Ops |
| :--- | :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | DAG | Valid | 0.0098 | 10 | 60 | 70 |
| | 10 | 30 | Cyclic | Cycle Detected | 0.0038 | 0 | 31 | 31 |
| **Medium** | 100 | 500 | DAG | Valid | 0.1211 | 100 | 1,000 | 1,100 |
| | 100 | 500 | Cyclic | Cycle Detected | 0.0531 | 0 | 505 | 505 |
| **Large** | 1,000 | 5,000 | DAG | Valid | 0.9843 | 1,000 | 10,000 | 11,000 |
| | 1,000 | 5,000 | Cyclic | Cycle Detected | 0.2703 | 6 | 5,028 | 5,034 |

---

### Strongly Connected Components (Kosaraju)

| Size | $V$ | $E$ | Time (ms) | DFS Visits | Edge Exams | Total Ops | Avg SCCs |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | 0.0197 | 20 | 60 | 80 | 1.6 |
| **Medium** | 100 | 500 | 0.1127 | 200 | 1,000 | 1,200 | 2.2 |
| **Large** | 1,000 | 5,000 | 1.5590 | 2,000 | 10,000 | 12,000 | 13.6 |

Notice that the total operations equal $2(V + E)$ across both passes, matching the theoretical linear bound.

---

### Dijkstra's Shortest Paths

| Size | $V$ | $E$ | Time (ms) | Edge Relaxations | PQ Operations | Comparisons | Total Ops |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | 0.0167 | 60 | 27 | 53 | 140 |
| **Medium** | 100 | 500 | 0.1626 | 1,000 | 392 | 2,292 | 3,684 |
| **Large** | 1,000 | 5,000 | 1.9583 | 10,000 | 3,926 | 36,137 | 50,063 |

---

### Plots

#### Execution Time vs. Vertices
![Execution Time vs Vertices](results/plots/execution_time_vs_vertices.png)

#### Execution Time vs. Edges
![Execution Time vs Edges](results/plots/execution_time_vs_edges.png)

#### Kruskal vs. Prim Comparison
![Kruskal vs Prim](results/plots/kruskal_vs_prim_comparison.png)

#### DSU Optimization Impact
![DSU Performance](results/plots/dsu_performance_comparison.png)

#### Dijkstra Operation Breakdown
![Dijkstra Operations](results/plots/dijkstra_operations.png)

---

## 6. Discussion

### Kruskal vs. Prim
In our tests on sparse graphs ($E / V = 3$ to $5$), Kruskal's algorithm was faster than Prim's:
* On the Large graph ($V = 1000, E = 5000$), Kruskal finished in 2.00 ms (84,385 ops) compared to Prim's 2.27 ms (108,995 ops).
* **Early termination:** Kruskal stops as soon as it selects $V - 1 = 999$ edges. In our tests, Kruskal only had to examine 3,652 out of 5,000 edges (around 73%). Prim had to process all 10,000 incident edge endpoints across all visited vertices.
* **Queue overhead:** Java's standard `PriorityQueue` has noticeable overhead from object allocations and repeated heap rebalancing during `add` and `poll`. In contrast, sorting the edge list once in Kruskal is fast and cache-friendly.
* However, on very dense graphs ($E \approx V^2$), Kruskal would spend significant time sorting $O(V^2)$ edges, where Prim using an indexed heap would be faster.

### DSU Optimizations
The benchmark clearly shows the impact of path compression and union by rank:
* On small graphs ($V = 10$), naive DSU works fine because trees don't get deep enough for chain lookups to matter.
* On the Large graph ($V = 1000$), the naive version needed **700,483 operations**, while the optimized version needed only **25,574 operations** (a 27.4x reduction).
* Without path compression and rank balance, trees degenerate into long chains where each `find` call walks through many parent pointers. Path compression flattens the tree so subsequent lookups are nearly direct.

### Strongly Connected Components
Kosaraju's algorithm showed strictly linear scaling:
* Total operations were exactly $2 \times (V + E)$: 80 for Small, 1,200 for Medium, and 12,000 for Large.
* In smart cities, this is directly applicable to traffic flow. If an urban network breaks into multiple SCCs, it means some districts cannot reach others via one-way streets, highlighting where two-way roads or return routes are missing.

### Topological Ordering
* On valid DAGs, Kahn's algorithm processes all vertices and edges in linear time (11,000 operations for $V=1000, E=5000$).
* On cyclic networks, the queue empties early when all remaining vertices still have incoming edges. On the Large cyclic graph, it aborted after examining edges without processing vertices in 0.27 ms. This fast cycle detection is useful for validating dependency chains before rolling out infrastructure updates.

### Dijkstra Scalability
* The dominant cost in Dijkstra is heap comparisons (36,137 comparisons on the Large graph vs 10,000 relaxations).
* The lazy deletion check `if (curr.distance > dist[u]) continue;` is critical. Without it, outdated queue entries would be processed, triggering redundant relaxation loops and degrading the runtime.

---

## 7. Testing

The test suite in `tests/TestSuite.java` contains 140 assertions that verify correctness across edge cases:

1. **Single vertex graph ($V=1, E=0$):** MST weight is 0.0, graph is connected, topological sort returns `[0]`, Kosaraju finds 1 SCC, and Dijkstra distance to self is 0.0.
2. **Small graph with hand-calculated solution:** Verified that MST weight matches 6.0 and shortest path $0 \to 3$ is `[0, 1, 2, 3]` with distance 6.0.
3. **Disconnected graph:** Verified that both Kruskal and Prim detect disconnectedness (`isConnected() == false`), and Dijkstra marks unreachable nodes with infinite distance.
4. **Graphs with cycles:** Topological sort detects cycles and returns empty order with `hasCycle() == true`. Kosaraju groups cyclic nodes into one SCC.
5. **Multiple valid MSTs and duplicate weights:** Square graph with identical edge weights (5.0). Both Kruskal and Prim return 15.0 and select exactly 3 edges.
6. **Unreachable vertices:** Directed graph with isolated target. Dijkstra returns infinite distance and empty path.
7. **Negative weights:** Dijkstra throws `IllegalArgumentException`.
8. **Random graph equivalence:** 16 random connected graphs tested across sizes ($V=10, 25, 50, 100$). Kruskal and Prim always return identical MST weights.
9. **SCC partition check:** Verified that every vertex belongs to exactly one SCC and the sum of component sizes equals $V$.
10. **DSU operations:** Verified find, union, set counts, and component sizes.

---

## 8. Conclusion

* **MST:** Kruskal and Prim produce identical trees. For typical sparse city infrastructure networks, Kruskal is faster due to early stopping and lower heap overhead.
* **DSU:** Path compression and union by rank prevent tree degeneracy, reducing operations by over 96% on larger graphs.
* **Connectivity:** Kosaraju's algorithm and Kahn's topological sort run in linear time $O(V + E)$ and cleanly handle cycle detection and component partitioning.
* **Dijkstra:** Efficient for non-negative road networks with path reconstruction and lazy deletion.

---

## 9. How to Compile and Run

### Requirements
* OpenJDK 17 or newer (tested on OpenJDK 26)
* Python 3 with `matplotlib` for generating plots

### Compile
```powershell
& "C:\Users\mazhi\.jdks\openjdk-26.0.2.1\bin\javac.exe" -d out -sourcepath "src;tests" (Get-ChildItem -Recurse src/*.java, tests/*.java).FullName
```

### Run Benchmarks
```powershell
& "C:\Users\mazhi\.jdks\openjdk-26.0.2.1\bin\java.exe" -cp out Main
```

### Run Tests
```powershell
& "C:\Users\mazhi\.jdks\openjdk-26.0.2.1\bin\java.exe" -cp out tests.TestSuite
```

### Regenerate Plots
```powershell
python scripts/generate_plots.py
```
Plots are saved in `results/plots/`.
