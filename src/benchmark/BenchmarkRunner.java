package benchmark;

import connectivity.KosarajuSCC;
import connectivity.TopologicalSort;
import graph.Graph;
import graph.GraphGenerator;
import mst.KruskalMST;
import mst.PrimMST;
import shortestpath.Dijkstra;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class BenchmarkRunner {

    public static final int REPEATS = 5;
    public static final long FIXED_SEED = 42L;

    public static class Config {
        public final String name;
        public final int V;
        public final int E;

        public Config(String name, int V, int E) {
            this.name = name;
            this.V = V;
            this.E = E;
        }
    }

    public static void runAllBenchmarks() {
        System.out.println("Starting Benchmark Suite (5 runs per config, fixed seed = " + FIXED_SEED + ")...");

        File resultsDir = new File("results/tables");
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }
        File plotsDir = new File("results/plots");
        if (!plotsDir.exists()) {
            plotsDir.mkdirs();
        }

        Config[] configs = {
                new Config("Small", 10, 30),
                new Config("Medium", 100, 500),
                new Config("Large", 1000, 5000)
        };

        // Warm up JVM to avoid JIT spikes
        warmup();

        runMSTBenchmark(configs);
        runDSUBenchmark(configs);
        runTopologicalSortBenchmark(configs);
        runSCCBenchmark(configs);
        runDijkstraBenchmark(configs);

        System.out.println("\nAll benchmarks finished successfully. CSV tables saved to results/tables/.");
    }

    private static void warmup() {
        System.out.print("Warming up JVM... ");
        for (int i = 0; i < 50; i++) {
            Graph g = GraphGenerator.generateConnectedUndirectedGraph(50, 150, 999L + i, 1.0, 100.0);
            new KruskalMST(g);
            new PrimMST(g);
            Graph dag = GraphGenerator.generateDAG(50, 150, 999L + i, 1.0, 100.0);
            new TopologicalSort(dag);
            Graph cyclic = GraphGenerator.generateDirectedGraphWithCycles(50, 150, 999L + i, 1.0, 100.0);
            new KosarajuSCC(cyclic);
            new Dijkstra(cyclic, 0);
        }
        System.out.println("Done.");
    }

    private static void runMSTBenchmark(Config[] configs) {
        System.out.println("\n=== MST Benchmark: Kruskal vs Prim ===");
        String csvPath = "results/tables/mst_benchmark.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {
            pw.println("GraphSize,V,E,Algorithm,AvgTimeMs,AvgEdgeExams,AvgComparisons,AvgAuxOps,AvgTotalOps,MSTWeight");

            for (Config cfg : configs) {
                double totalKruskalTime = 0, totalPrimTime = 0;
                long totalKruskalEdges = 0, totalPrimEdges = 0;
                long totalKruskalComp = 0, totalPrimComp = 0;
                long totalKruskalAux = 0, totalPrimAux = 0;
                long totalKruskalTotalOps = 0, totalPrimTotalOps = 0;
                double kWeight = 0, pWeight = 0;

                for (int r = 0; r < REPEATS; r++) {
                    Graph g = GraphGenerator.generateConnectedUndirectedGraph(cfg.V, cfg.E, FIXED_SEED + r, 1.0, 100.0);
                    KruskalMST k = new KruskalMST(g);
                    PrimMST p = new PrimMST(g);

                    totalKruskalTime += k.getExecutionTimeMs();
                    totalKruskalEdges += k.getEdgeExaminations();
                    totalKruskalComp += k.getComparisons();
                    totalKruskalAux += k.getDsuOperations();
                    totalKruskalTotalOps += k.getTotalOperations();
                    kWeight = k.getTotalWeight();

                    totalPrimTime += p.getExecutionTimeMs();
                    totalPrimEdges += p.getEdgeExaminations();
                    totalPrimComp += p.getComparisons();
                    totalPrimAux += p.getPqOperations();
                    totalPrimTotalOps += p.getTotalOperations();
                    pWeight = p.getTotalWeight();
                }

                double avgKTime = totalKruskalTime / REPEATS;
                long avgKEdges = totalKruskalEdges / REPEATS;
                long avgKComp = totalKruskalComp / REPEATS;
                long avgKAux = totalKruskalAux / REPEATS;
                long avgKTotalOps = totalKruskalTotalOps / REPEATS;

                double avgPTime = totalPrimTime / REPEATS;
                long avgPEdges = totalPrimEdges / REPEATS;
                long avgPComp = totalPrimComp / REPEATS;
                long avgPAux = totalPrimAux / REPEATS;
                long avgPTotalOps = totalPrimTotalOps / REPEATS;

                pw.printf(Locale.US, "%s,%d,%d,Kruskal,%.5f,%d,%d,%d,%d,%.2f%n",
                        cfg.name, cfg.V, cfg.E, avgKTime, avgKEdges, avgKComp, avgKAux, avgKTotalOps, kWeight);
                pw.printf(Locale.US, "%s,%d,%d,Prim,%.5f,%d,%d,%d,%d,%.2f%n",
                        cfg.name, cfg.V, cfg.E, avgPTime, avgPEdges, avgPComp, avgPAux, avgPTotalOps, pWeight);

                System.out.printf("[%s: V=%d, E=%d]%n", cfg.name, cfg.V, cfg.E);
                System.out.printf("  Kruskal: Time = %.4f ms | Operations = %d (weight = %.2f)%n", avgKTime, avgKTotalOps, kWeight);
                System.out.printf("  Prim:    Time = %.4f ms | Operations = %d (weight = %.2f)%n", avgPTime, avgPTotalOps, pWeight);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runDSUBenchmark(Config[] configs) {
        System.out.println("\n=== DSU Benchmark: Optimized vs Naive ===");
        String csvPath = "results/tables/dsu_benchmark.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {
            pw.println("GraphSize,V,E,DSU_Mode,AvgTimeMs,AvgTotalOps");

            for (Config cfg : configs) {
                double optTime = 0, naiveTime = 0;
                long optOps = 0, naiveOps = 0;

                for (int r = 0; r < REPEATS; r++) {
                    Graph g = GraphGenerator.generateConnectedUndirectedGraph(cfg.V, cfg.E, FIXED_SEED + r, 1.0, 100.0);

                    // Optimized: Path Compression + Union by Rank
                    KruskalMST optRes = new KruskalMST(g, true, true);
                    optTime += optRes.getExecutionTimeMs();
                    optOps += optRes.getDsuOperations();

                    // Naive: No Path Compression, No Union by Rank
                    KruskalMST naiveRes = new KruskalMST(g, false, false);
                    naiveTime += naiveRes.getExecutionTimeMs();
                    naiveOps += naiveRes.getDsuOperations();
                }

                double avgOptTime = optTime / REPEATS;
                double avgNaiveTime = naiveTime / REPEATS;
                long avgOptOps = optOps / REPEATS;
                long avgNaiveOps = naiveOps / REPEATS;

                pw.printf(Locale.US, "%s,%d,%d,Optimized(PathComp+Rank),%.5f,%d%n",
                        cfg.name, cfg.V, cfg.E, avgOptTime, avgOptOps);
                pw.printf(Locale.US, "%s,%d,%d,Naive(NoPathComp+NoRank),%.5f,%d%n",
                        cfg.name, cfg.V, cfg.E, avgNaiveTime, avgNaiveOps);

                System.out.printf("[%s: V=%d, E=%d]%n", cfg.name, cfg.V, cfg.E);
                System.out.printf("  Optimized DSU: Time = %.4f ms | DSU Ops = %d%n", avgOptTime, avgOptOps);
                System.out.printf("  Naive DSU:     Time = %.4f ms | DSU Ops = %d (speedup: %.2fx)%n",
                        avgNaiveTime, avgNaiveOps, avgNaiveTime / Math.max(1e-6, avgOptTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTopologicalSortBenchmark(Config[] configs) {
        System.out.println("\n=== Topological Ordering Benchmark: DAG vs Cyclic ===");
        String csvPath = "results/tables/topo_benchmark.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {
            pw.println("GraphSize,V,E,GraphType,IsDAG,AvgTimeMs,AvgVertexVisits,AvgEdgeExams,AvgTotalOps");

            for (Config cfg : configs) {
                // Test DAG
                double dagTime = 0;
                long dagVisits = 0, dagEdges = 0;
                for (int r = 0; r < REPEATS; r++) {
                    Graph dag = GraphGenerator.generateDAG(cfg.V, cfg.E, FIXED_SEED + r, 1.0, 100.0);
                    TopologicalSort res = new TopologicalSort(dag);
                    dagTime += res.getExecutionTimeMs();
                    dagVisits += res.getVertexVisits();
                    dagEdges += res.getEdgeExaminations();
                }
                double avgDagTime = dagTime / REPEATS;
                long avgDagVisits = dagVisits / REPEATS;
                long avgDagEdges = dagEdges / REPEATS;

                pw.printf(Locale.US, "%s,%d,%d,DAG,true,%.5f,%d,%d,%d%n",
                        cfg.name, cfg.V, cfg.E, avgDagTime, avgDagVisits, avgDagEdges, avgDagVisits + avgDagEdges);

                // Test Cyclic
                double cycleTime = 0;
                long cycleVisits = 0, cycleEdges = 0;
                for (int r = 0; r < REPEATS; r++) {
                    Graph cyclic = GraphGenerator.generateDirectedGraphWithCycles(cfg.V, cfg.E, FIXED_SEED + r, 1.0, 100.0);
                    TopologicalSort res = new TopologicalSort(cyclic);
                    cycleTime += res.getExecutionTimeMs();
                    cycleVisits += res.getVertexVisits();
                    cycleEdges += res.getEdgeExaminations();
                }
                double avgCycleTime = cycleTime / REPEATS;
                long avgCycleVisits = cycleVisits / REPEATS;
                long avgCycleEdges = cycleEdges / REPEATS;

                pw.printf(Locale.US, "%s,%d,%d,Cyclic,false,%.5f,%d,%d,%d%n",
                        cfg.name, cfg.V, cfg.E, avgCycleTime, avgCycleVisits, avgCycleEdges, avgCycleVisits + avgCycleEdges);

                System.out.printf("[%s: V=%d, E=%d]%n", cfg.name, cfg.V, cfg.E);
                System.out.printf("  DAG:    Time = %.4f ms | Ops = %d (Valid DAG)%n", avgDagTime, avgDagVisits + avgDagEdges);
                System.out.printf("  Cyclic: Time = %.4f ms | Ops = %d (Cycle detected)%n", avgCycleTime, avgCycleVisits + avgCycleEdges);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runSCCBenchmark(Config[] configs) {
        System.out.println("\n=== SCC Benchmark: Kosaraju's Algorithm ===");
        String csvPath = "results/tables/scc_benchmark.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {
            pw.println("GraphSize,V,E,Algorithm,AvgTimeMs,AvgDfsVisits,AvgEdgeExams,AvgTotalOps,AvgComponentCount");

            for (Config cfg : configs) {
                double totalTime = 0;
                long totalVisits = 0;
                long totalEdges = 0;
                long totalComponents = 0;

                for (int r = 0; r < REPEATS; r++) {
                    Graph cyclic = GraphGenerator.generateDirectedGraphWithCycles(cfg.V, cfg.E, FIXED_SEED + r, 1.0, 100.0);
                    KosarajuSCC res = new KosarajuSCC(cyclic);
                    totalTime += res.getExecutionTimeMs();
                    totalVisits += res.getDfsVisits();
                    totalEdges += res.getEdgeExaminations();
                    totalComponents += res.getComponentCount();
                }

                double avgTime = totalTime / REPEATS;
                long avgVisits = totalVisits / REPEATS;
                long avgEdges = totalEdges / REPEATS;
                double avgComps = (double) totalComponents / REPEATS;

                pw.printf(Locale.US, "%s,%d,%d,KosarajuSCC,%.5f,%d,%d,%d,%.1f%n",
                        cfg.name, cfg.V, cfg.E, avgTime, avgVisits, avgEdges, avgVisits + avgEdges, avgComps);

                System.out.printf("[%s: V=%d, E=%d]%n", cfg.name, cfg.V, cfg.E);
                System.out.printf("  Kosaraju SCC: Time = %.4f ms | Ops = %d | Components = %.1f%n",
                        avgTime, avgVisits + avgEdges, avgComps);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runDijkstraBenchmark(Config[] configs) {
        System.out.println("\n=== Dijkstra Benchmark: Shortest Paths ===");
        String csvPath = "results/tables/dijkstra_benchmark.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {
            pw.println("GraphSize,V,E,Algorithm,AvgTimeMs,AvgRelaxations,AvgPqOps,AvgComparisons,AvgTotalOps");

            for (Config cfg : configs) {
                double totalTime = 0;
                long totalRelax = 0;
                long totalPq = 0;
                long totalComp = 0;

                for (int r = 0; r < REPEATS; r++) {
                    Graph g = GraphGenerator.generateConnectedUndirectedGraph(cfg.V, cfg.E, FIXED_SEED + r, 1.0, 100.0);
                    Dijkstra res = new Dijkstra(g, 0);
                    totalTime += res.getExecutionTimeMs();
                    totalRelax += res.getEdgeRelaxations();
                    totalPq += res.getPqOperations();
                    totalComp += res.getComparisons();
                }

                double avgTime = totalTime / REPEATS;
                long avgRelax = totalRelax / REPEATS;
                long avgPq = totalPq / REPEATS;
                long avgComp = totalComp / REPEATS;
                long avgTotal = avgRelax + avgPq + avgComp;

                pw.printf(Locale.US, "%s,%d,%d,Dijkstra,%.5f,%d,%d,%d,%d%n",
                        cfg.name, cfg.V, cfg.E, avgTime, avgRelax, avgPq, avgComp, avgTotal);

                System.out.printf("[%s: V=%d, E=%d]%n", cfg.name, cfg.V, cfg.E);
                System.out.printf("  Dijkstra: Time = %.4f ms | Relaxations = %d | PQ Ops = %d | Total Ops = %d%n",
                        avgTime, avgRelax, avgPq, avgTotal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
