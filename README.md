# Assignment 3: Smart City Infrastructure and Route Planning

A comprehensive Java application modeling a smart city's infrastructure network, incorporating graph algorithms for infrastructure optimization, network connectivity analysis, and shortest-path route planning.

---

## 1. Overview

Urban infrastructure planning requires robust computational graph models to design cost-effective utility networks, analyze traffic flow reliability, and compute optimal transit routes. In this project, a smart city network is represented as a graph $G = (V, E)$:

* **Vertices ($V$):** City districts, utility substations, transit hubs, or sensor locations.
* **Edges ($E$):** Physical roads, electrical transmission lines, fiber cables, or water pipelines.
* **Weights ($w$):** Construction costs (in thousands of dollars) or travel distances/times (in kilometers or minutes), non-negative for physical planning.

### System Capabilities

1. **Infrastructure Planning (Minimum Spanning Tree):** Connecting all city districts at the minimum total infrastructure expenditure with zero redundancy or redundant cycles.
2. **Connectivity & Dependency Analysis (Topological Ordering & Strongly Connected Components):** Evaluating one-way traffic systems, identifying mutually reachable urban zones, and validating service dependency sequences for infrastructure rollout.
3. **Route Planning (Single-Source Shortest Paths):** Finding distance-optimal routes from municipal service hubs (e.g., central fire stations, emergency dispatchers) to all reachable city sectors.

---

## 2. Implementation & Design Decisions

### Project Structure

Following the requirements, the repository is organized modularly:

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
│   │   ├── MSTResult.java
│   │   ├── KruskalMST.java
│   │   └── PrimMST.java
│   ├── connectivity/
│   │   ├── TopologicalSortResult.java
│   │   ├── TopologicalSort.java
│   │   ├── SCCResult.java
│   │   ├── TarjanSCC.java
│   │   └── KosarajuSCC.java
│   ├── shortestpath/
│   │   ├── ShortestPathResult.java
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

### Key Design Decisions

#### Graph Representation (`graph`)
* **Adjacency Lists:** An adjacency list `List<List<Edge>>` stores incident edges per vertex. For sparse and moderately dense graphs ($E \ll V^2$), adjacency lists provide optimal $O(V + E)$ storage and iteration speed compared to an $O(V^2)$ adjacency matrix.
* **Graph Generator:** The `GraphGenerator` utility uses a deterministic random seed to build reproducible test topologies:
  * *Connected Undirected Graphs:* To guarantee connectivity for MST benchmarks, a random spanning tree is initialized first over a randomized vertex permutation, followed by random insertion of the remaining $E - (V - 1)$ edges with multi-edge and self-loop suppression.
  * *DAGs:* Edges $(u, v)$ are restricted to $u < v$, strictly preventing cycles.
  * *Cyclic Graphs:* Arbitrary directed edges with explicit forced cycle cycles are generated to test Tarjan's SCC and cycle detection.

#### Disjoint Set Union (`dsu`)
* **Path Compression:** Implemented during `find(u)` so that every visited node points directly to the set representative root.
* **Union by Rank / Size:** Attaches the shallower tree root to the deeper tree root, bounding maximum tree depth to $O(\log V)$ before path compression and achieving near-constant $O(\alpha(V))$ amortized time.
* **Configurable Benchmark Modes:** The DSU supports toggling both path compression and union by rank on or off to directly measure the dramatic real-world performance difference between $O(\alpha(V))$ and unoptimized $O(V)$ chain trees.

#### Minimum Spanning Tree (`mst`)
* **Kruskal's Algorithm:** Sorts the graph's edge list in ascending order of weight using Dual-Pivot Quicksort / TimSort ($O(E \log E)$). It traverses candidate edges and employs the DSU to join disjoint components without cycles. Traversal stops early once exactly $V - 1$ edges are added. If fewer than $V - 1$ edges are connected, disconnected components are flagged.
* **Prim's Algorithm:** Employs a Java standard binary heap `PriorityQueue<Edge>` ordered by edge weight. Starting from vertex 0, newly discovered cross-edges incident to visited vertices are pushed into the queue. A `visited` boolean array prevents cycles. If the number of reached vertices is less than $V$, disconnectedness is detected.

#### Connectivity & SCC (`connectivity`)
* **Topological Sort:** Implemented via Kahn's algorithm (BFS with in-degree tracking). Vertices with in-degree 0 are queued. As each vertex is dequeued, in-degrees of its downstream neighbors are decremented. If fewer than $V$ vertices are processed when the queue empties, the remaining vertices form a directed cycle.
* **Strongly Connected Components (Tarjan & Kosaraju):**
  * *Tarjan's Algorithm:* Uses a single DFS traversal maintaining discovery time (`ids`), lowest reachable time (`lowLink`), and an explicit DFS stack. Roots of SCCs are detected when `ids[u] == low[u]`.
  * *Kosaraju's Algorithm:* Included for verification, performing a forward DFS to order vertices by exit time, followed by a DFS over the transposed graph ($G^T$).

#### Shortest Paths (`shortestpath`)
* **Dijkstra's Algorithm:** Uses a binary min-heap Priority Queue of `(vertex, distance)` pairs and an array `dist[]` initialized to $\infty$. A `visited[]` array handles duplicate queue entries (lazy deletion). Shortest paths are reconstructed using an antecedent array `parent[]`.
* **Negative Edge Detection:** Strictly asserts $w \ge 0$. If any negative edge is encountered, an `IllegalArgumentException` is thrown immediately.

---

## 3. Complexity Analysis

| Algorithm | Assumptions / Data Structures | Time Complexity (Theoretical) | Space Complexity |
| :--- | :--- | :---: | :---: |
| **DSU Operations (`find` / `union`)** | Path compression + union by rank | $O(\alpha(V))$ amortized | $O(V)$ |
| **Kruskal's Algorithm** | Adjacency list, edge array sort, DSU | $O(E \log E) = O(E \log V)$ | $O(V + E)$ |
| **Prim's Algorithm** | Adjacency list, binary min-heap | $O((V + E) \log V)$ | $O(V + E)$ |
| **Tarjan's SCC** | Adjacency list, DFS stack | $O(V + E)$ | $O(V)$ |
| **Kosaraju's SCC** | Adjacency list, transposed graph, stack | $O(V + E)$ | $O(V + E)$ |
| **Topological Sort (Kahn's)** | Adjacency list, in-degree array, queue | $O(V + E)$ | $O(V)$ |
| **Dijkstra's Algorithm** | Adjacency list, binary min-heap | $O((V + E) \log V)$ | $O(V + E)$ |

### Detailed Algorithm Breakdown

#### 1. Disjoint Set Union (DSU)
* **Time Complexity:** With both path compression and union by rank, any sequence of $m$ operations on $n$ elements takes $O(m \cdot \alpha(n))$ time, where $\alpha(n)$ is the inverse Ackermann function. For all practical inputs ($n \le 10^{80}$), $\alpha(n) \le 4$, making operations effectively $O(1)$.
* **Without Optimizations:** Without union by rank, trees can degenerate into linear chains of depth $O(V)$, causing single `find` calls to take $O(V)$ time and $m$ operations to degrade to $O(m \cdot V)$.

#### 2. Kruskal's Algorithm
* **Time Complexity:** Sorting $|E|$ edges takes $O(E \log E)$. Since $E \le V^2$, $\log E \le 2 \log V$, which means sorting is $O(E \log V)$. The subsequent loop performs at most $E$ `find` operations and $V - 1$ `union` operations, totaling $O(E \cdot \alpha(V))$. Because $\alpha(V) \ll \log V$, edge sorting dominates:
  $$T(V, E) = O(E \log V)$$
* **Space Complexity:** $O(E)$ to store edge references, plus $O(V)$ for DSU parent, rank, and size arrays.

#### 3. Prim's Algorithm
* **Time Complexity:** With an adjacency list and standard binary min-heap, each vertex is extracted from the priority queue at most once ($V \log V$), and each incident edge is pushed at most once ($E \log V$):
  $$T(V, E) = O((V + E) \log V)$$
* **Comparison with Kruskal:** On sparse graphs ($E \approx V$), both take $O(V \log V)$. On dense graphs ($E \approx V^2$), Kruskal takes $O(V^2 \log V)$, whereas Prim using an indexed binary heap or Fibonacci heap can achieve $O(E + V \log V) = O(V^2)$.

#### 4. Strongly Connected Components (Tarjan's Algorithm)
* **Time Complexity:** Every vertex is visited by DFS exactly once, and each directed edge is examined exactly once. Stack pushes and pops occur at most once per vertex:
  $$T(V, E) = O(V + E)$$
* **Space Complexity:** $O(V)$ for recursion call stack, `ids`, `low`, `onStack`, and component ID arrays.

#### 5. Topological Ordering (Kahn's Algorithm)
* **Time Complexity:** Initializing in-degrees examines all $E$ edges in $O(V + E)$. Enqueueing vertices with zero in-degree takes $O(V)$. In the main loop, each vertex is dequeued once ($V$), and each outgoing edge is decremented once ($E$):
  $$T(V, E) = O(V + E)$$
* **Space Complexity:** $O(V)$ for the in-degree array and FIFO queue.

#### 6. Dijkstra's Algorithm
* **Time Complexity:** Initializing distances takes $O(V)$. Each vertex is popped from the min-heap at most once ($O(V \log V)$). When relaxation succeeds, an updated node is inserted into the min-heap in $O(\log V)$ time. Across all $E$ edges, this contributes $O(E \log V)$:
  $$T(V, E) = O((V + E) \log V)$$
* **Space Complexity:** $O(V + E)$ for adjacency lists and heap storage.

### Why Dijkstra's Algorithm Fails with Negative Edge Weights

Dijkstra's algorithm relies on a **greedy choice property**: once a vertex $u$ is extracted from the priority queue (having the minimal tentative distance among all unvisited vertices), its distance $dist[u]$ is finalized and guaranteed optimal.

When negative weights are present, this invariant breaks:
1. **Premature Finalization:** A longer path containing a negative-weight edge discovered later could produce a strictly smaller total distance to an already-finalized vertex $u$. Because Dijkstra never re-evaluates finalized nodes, it outputs incorrect, sub-optimal distances.
2. **Negative Cycles:** If the graph contains a directed cycle whose total edge weight is negative ($\sum w < 0$), shortest paths become ill-defined ($-\infty$), as traversing the cycle indefinitely reduces path cost without bound. Algorithms like Bellman-Ford or SPFA are required to detect negative cycles and compute shortest paths under negative weights.

---

## 4. Experimental Setup

Experiments were executed under reproducible conditions on OpenJDK 26 (64-Bit VM).

### Graph Configurations

Three standard graph configurations were tested as defined in the assignment specifications:

| Configuration | Vertices ($V$) | Edges ($E$) | Average Degree ($2E/V$) | Density ($2E / (V(V-1))$) |
| :--- | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | 6.0 | 0.667 |
| **Medium** | 100 | 500 | 10.0 | 0.101 |
| **Large** | 1,000 | 5,000 | 10.0 | 0.010 |

### Benchmark Methodology

1. **Fixed Random Seed:** All graphs were generated using `seed = 42L` (+ iteration offset) to guarantee exact repeatability.
2. **Connectivity Guarantee:** For MST benchmarks, graphs are guaranteed connected by generating a random spanning tree before populating remaining edges.
3. **Repeated Trials:** Each benchmark configuration was repeated **5 times**. Reported metrics represent the arithmetic mean across all 5 runs.
4. **JVM Warm-up:** 50 warm-up runs of all algorithms on intermediate graph sizes were conducted before measurement to trigger JIT compilation and stabilize garbage collection.
5. **High-Precision Timing:** Wall-clock time was captured via `System.nanoTime()` surrounding the algorithm execution.
6. **Detailed Metric Counting:** Internal operation counters recorded edge examinations, element comparisons, priority queue pushes/polls, and DSU operations.

---

## 5. Experimental Results

### Table 1: Minimum Spanning Tree — Kruskal vs. Prim

> **Metrics:** Time in milliseconds (ms), operation counts, and calculated MST weight.

| Graph Size | $V$ | $E$ | Algorithm | Execution Time (ms) | Edge Examinations | Comparisons | Aux / PQ Ops | Total Operations | MST Weight |
| :--- | :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | Kruskal | **0.0258** | 13 | 111 | 98 | 223 | **227.83** |
| | 10 | 30 | Prim | 0.0332 | 60 | 140 | 43 | 244 | **227.83** |
| **Medium** | 100 | 500 | Kruskal | **0.2151** | 290 | 3,836 | 2,071 | 6,198 | **1,161.90** |
| | 100 | 500 | Prim | 0.2826 | 1,000 | 5,477 | 790 | 7,268 | **1,161.90** |
| **Large** | 1,000 | 5,000 | Kruskal | **2.9957** | 3,652 | 55,158 | 25,574 | 84,385 | **12,260.45** |
| | 1,000 | 5,000 | Prim | 3.6373 | 10,000 | 90,342 | 8,652 | 108,995 | **12,260.45** |

*Verification:* For every graph configuration, Kruskal and Prim produce the exact same total MST weight.

---

### Table 2: Disjoint Set Union — Optimized vs. Naive

> **Comparison:** Optimized (Path Compression + Union by Rank) vs. Naive (No Path Compression, No Rank).

| Graph Size | $V$ | $E$ | DSU Mode | Execution Time (ms) | Total DSU Operations | Speedup / Efficiency |
| :--- | :---: | :---: | :--- | :---: | :---: | :---: |
| **Small** | 10 | 30 | Optimized (PathComp + Rank) | 0.0612 | 98 | Baseline |
| | 10 | 30 | Naive (No PathComp, No Rank) | 0.0519 | 90 | 0.85x |
| **Medium** | 100 | 500 | Optimized (PathComp + Rank) | 0.1998 | 2,071 | 2.95x fewer ops |
| | 100 | 500 | Naive (No PathComp, No Rank) | 0.1870 | 6,111 | Baseline |
| **Large** | 1,000 | 5,000 | Optimized (PathComp + Rank) | **1.5423** | **25,574** | **27.4x fewer ops (1.52x faster)** |
| | 1,000 | 5,000 | Naive (No PathComp, No Rank) | 2.3376 | 700,483 | Degenerate chain lookups |

---

### Table 3: Connectivity — Topological Ordering (DAG vs. Cyclic)

| Graph Size | $V$ | $E$ | Graph Type | Detected State | Execution Time (ms) | Vertex Visits | Edge Exams | Total Operations |
| :--- | :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | DAG | Valid DAG | 0.0160 | 10 | 60 | 70 |
| | 10 | 30 | Cyclic | Cycle Detected | 0.0063 | 0 | 31 | 31 |
| **Medium** | 100 | 500 | DAG | Valid DAG | 0.1774 | 100 | 1,000 | 1,100 |
| | 100 | 500 | Cyclic | Cycle Detected | 0.0734 | 0 | 505 | 505 |
| **Large** | 1,000 | 5,000 | DAG | Valid DAG | 1.7799 | 1,000 | 10,000 | 11,000 |
| | 1,000 | 5,000 | Cyclic | Cycle Detected | 0.2009 | 6 | 5,028 | 5,034 |

---

### Table 4: Strongly Connected Components — Tarjan's Algorithm

| Graph Size | $V$ | $E$ | Algorithm | Execution Time (ms) | DFS Visits | Edge Examinations | Total Operations | Average SCCs |
| :--- | :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | Tarjan SCC | 0.0138 | 10 | 30 | 40 | 1.6 |
| **Medium** | 100 | 500 | Tarjan SCC | 0.0273 | 100 | 500 | 600 | 2.2 |
| **Large** | 1,000 | 5,000 | Tarjan SCC | 0.4194 | 1,000 | 5,000 | 6,000 | 13.6 |

---

### Table 5: Route Planning — Dijkstra's Shortest Paths

| Graph Size | $V$ | $E$ | Execution Time (ms) | Edge Relaxations | PQ Operations | Comparisons | Total Operations |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Small** | 10 | 30 | 0.0154 | 60 | 27 | 53 | 140 |
| **Medium** | 100 | 500 | 0.1805 | 1,000 | 392 | 2,292 | 3,684 |
| **Large** | 1,000 | 5,000 | 2.3140 | 10,000 | 3,926 | 36,137 | 50,063 |

---

### Plots & Visualizations

#### Plot 1: Execution Time vs. Number of Vertices
![Execution Time vs Number of Vertices](results/plots/execution_time_vs_vertices.png)

#### Plot 2: Execution Time vs. Number of Edges
![Execution Time vs Number of Edges](results/plots/execution_time_vs_edges.png)

#### Plot 3: Kruskal vs. Prim Performance Comparison
![Kruskal vs Prim Comparison](results/plots/kruskal_vs_prim_comparison.png)

#### Plot 4: DSU Optimization Impact (Optimized vs. Naive)
![DSU Performance Comparison](results/plots/dsu_performance_comparison.png)

#### Plot 5: Dijkstra Operation Breakdown
![Dijkstra Operation Breakdown](results/plots/dijkstra_operations.png)

---

## 6. Comparison and Discussion

### 1. Kruskal vs. Prim: Performance and Trade-offs
* **Execution Time:** Kruskal's algorithm consistently achieved lower execution times across all tested graph sizes (e.g., 2.99 ms vs. 3.64 ms on the Large graph).
* **Early Stopping Advantage:** Kruskal terminates early as soon as $V - 1$ edges are added to the MST. On the Large graph ($V = 1000, E = 5000$), Kruskal examined only 3,652 out of 5,000 edges (73%). In contrast, Prim's algorithm examined all 10,000 directed edge endpoints (incident to each vertex) because edges are inserted into the priority queue as vertices are visited.
* **Operation Count:** Kruskal required 84,385 total operations on the Large graph compared to 108,995 for Prim. Prim's reliance on `PriorityQueue` operations introduces higher comparison overhead during heap reorganizations.
* **Density Trade-off:** For sparse networks ($E / V \approx 5$ to $10$, typical of road networks), Kruskal with DSU is simpler and faster. For very dense networks ($E \to V^2$), Prim implemented with an indexed priority queue or Fibonacci heap becomes advantageous because it avoids sorting $O(V^2)$ edges upfront.

### 2. DSU: Path Compression & Union by Rank/Size
* **The Degeneracy Problem:** Without optimizations, successive unions can create linear chain trees where each element is a child of the preceding element. On the Large graph ($V = 1000$), unoptimized DSU caused operation counts to jump from **25,574 to 700,483 operations**—an increase of over **27.4x**.
* **Path Compression:** Flattens the tree dynamically during `find` queries. After the first lookup, subsequent queries to any node along that path resolve in $O(1)$ pointer dereferences.
* **Union by Rank / Size:** Guarantees that tree height never exceeds $\lfloor \log_2 k \rfloor$ by always attaching the smaller subtree to the root of the larger subtree. Combined with path compression, the average cost per operation is bounded by $\alpha(V) \le 4$, eliminating tree degeneration even under worst-case union sequences.

### 3. Strongly Connected Components (SCC) Scaling
* **Linear Scaling ($O(V + E)$):** Tarjan's algorithm exhibits strictly linear scaling. As the graph expanded from Small ($V=10, E=30$) to Large ($V=1000, E=5000$), the operation count scaled exactly as $V + E$:
  * Small: $10 + 30 = 40$ ops (0.0138 ms)
  * Medium: $100 + 500 = 600$ ops (0.0273 ms)
  * Large: $1000 + 5000 = 6,000$ ops (0.4194 ms)
* **Smart City Applicability:** Tarjan's SCC is exceptionally efficient and scalable. In urban networks, an SCC represents a sector where any district can reach any other district via one-way roads. If an urban network breaks into multiple SCCs, traffic planners can immediately pinpoint isolated sectors or dead-end districts that lack reciprocal egress routes.

### 4. Topological Ordering: DAGs vs. Cyclic Networks
* **DAG Performance:** On acyclic dependency graphs, Kahn's algorithm successfully orders all vertices in linear time $O(V + E)$ (11,000 operations for $V=1000, E=5000$).
* **Cycle Detection:** On cyclic networks, Kahn's algorithm immediately halts vertex processing when remaining vertices all have in-degree $> 0$. For instance, on the Large cyclic graph, only 6 vertices were processed before identifying the cycle in 0.20 ms. This early termination makes Kahn's algorithm optimal for infrastructure dependency validation (e.g., verifying that power grid restoration sequences contain no circular dependencies).

### 5. Dijkstra: Growth and Scalability
* **Scaling Behavior:** Dijkstra scaled from 0.015 ms ($V=10$) to 2.31 ms ($V=1000$). The operation count grew from 140 to 50,063 operations, driven predominantly by min-heap key comparisons (36,137 comparisons) during heap bubble-up and bubble-down operations.
* **Unreachable Vertex Handling:** Disconnected target vertices remain at distance $\infty$, and path reconstruction cleanly returns an empty list in $O(1)$ time without throwing exceptions.

---

## 7. Testing & Verification

The test suite in `tests/TestSuite.java` was executed to validate all algorithmic requirements, edge cases, and invariants. A total of **142 test assertions** were evaluated with a **100% pass rate**.

### Verified Scenarios

1. **Single-Vertex Graph ($V = 1, E = 0$):**
   * Kruskal and Prim return MST weight $0.0$, report `isConnected = true`.
   * Topological sort returns `[0]`, `hasCycle = false`.
   * Tarjan SCC identifies exactly 1 component `[0]`.
   * Dijkstra returns distance $0.0$ to self.
2. **Small Graph with Known Ground Truth:**
   * Validated against hand-calculated MST cost ($6.0$) and shortest path distances ($0 \to 2$ is $3.0$ via node 1).
   * Confirmed path reconstruction yields `[0, 1, 2, 3]`.
3. **Disconnected Graphs:**
   * Both Kruskal and Prim detect disconnected components (`isConnected = false`).
   * Dijkstra flags unreachable vertices with distance $\infty$ and empty reconstructed path lists.
4. **Graphs Containing Cycles:**
   * Directed cycles ($0 \to 1 \to 2 \to 0$) correctly trigger `hasCycle = true` in Topological Sort.
   * Tarjan SCC correctly groups cyclic subgraphs into a single component of size 3.
5. **Multiple Valid MSTs & Duplicate Edge Weights:**
   * Symmetric 4-vertex cycle with uniform edge weights ($5.0$).
   * Both Kruskal and Prim return exactly $15.0$ total weight and select valid spanning trees of $V - 1 = 3$ edges.
6. **Unreachable Vertices:**
   * Verified that directed graph destinations with in-degree 0 remain unreachable from source vertex 0.
7. **Negative Edge Weight Handling:**
   * Graph with a negative edge ($-2.0$) causes Dijkstra to throw `IllegalArgumentException`, confirming validation integrity.
8. **Kruskal and Prim Equivalence Across Multiple Random Graphs:**
   * Evaluated across 16 random connected graphs ($V = 10, 25, 50, 100$, multiple seeds).
   * Verified that $|weight_{Kruskal} - weight_{Prim}| < 10^{-4}$ in every case.
9. **SCC Partition Invariants:**
   * Verified that every vertex belongs to **exactly one** strongly connected component:
     $$\sum_{C \in \mathcal{C}} |C| = V \quad \text{and} \quad C_i \cap C_j = \emptyset \quad \forall i \neq j$$
10. **Algorithm Equivalence:**
    * Verified that Tarjan's and Kosaraju's algorithms produce identical component counts and vertex partitions.

---

## 8. Conclusion

This project implemented and empirically analyzed key graph algorithms applied to smart city infrastructure planning:

1. **Infrastructure Planning (MST):** Both Kruskal's and Prim's algorithms solve the minimum-cost connectivity problem identically. Kruskal with an optimized DSU is preferred for sparse urban networks due to lower constant-factor overhead and early termination upon finding $V - 1$ edges.
2. **Disjoint Set Union (DSU):** Path compression and union by rank reduce operation counts by over **96%** (a 27x reduction in operations on $V=1000$), ensuring near-constant amortized time.
3. **Connectivity Analysis (SCC & Topological Sort):** Tarjan's SCC and Kahn's topological sort operate in strictly linear time $O(V + E)$, scaling effortlessly to large municipal networks for cycle detection and reachability partitioning.
4. **Route Planning (Dijkstra):** Binary min-heap Dijkstra delivers efficient $O((V + E) \log V)$ shortest-path routing with path reconstruction and strict non-negative weight validation.

---

## 9. Compilation & Reproduction Guide

### Prerequisites
* Java JDK 17+ (tested on OpenJDK 26)
* Python 3.8+ with `matplotlib` (for generating plots)

### Compilation
```bash
# Compile all source and test files
javac -d out -sourcepath "src;tests" src/**/*.java tests/**/*.java
```

### Run Tests and Benchmarks
```bash
# Runs correctness verification followed by experimental benchmarks
java -cp out Main
```

### Run Tests Only
```bash
java -cp out tests.TestSuite
```

### Regenerate Plots
```bash
python scripts/generate_plots.py
```
Generated charts are automatically placed in `results/plots/`.
