import csv
import os
import matplotlib.pyplot as plt

os.makedirs('results/plots', exist_ok=True)

def read_csv(filename):
    rows = []
    with open(filename, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for r in reader:
            rows.append(r)
    return rows

mst_data = read_csv('results/tables/mst_benchmark.csv')
dsu_data = read_csv('results/tables/dsu_benchmark.csv')
topo_data = read_csv('results/tables/topo_benchmark.csv')
scc_data = read_csv('results/tables/scc_benchmark.csv')
dijkstra_data = read_csv('results/tables/dijkstra_benchmark.csv')

v_vals = [10, 100, 1000]
e_vals = [30, 500, 5000]

kruskal_times = [float(r['AvgTimeMs']) for r in mst_data if r['Algorithm'] == 'Kruskal']
prim_times = [float(r['AvgTimeMs']) for r in mst_data if r['Algorithm'] == 'Prim']
dijkstra_times = [float(r['AvgTimeMs']) for r in dijkstra_data]
scc_times = [float(r['AvgTimeMs']) for r in scc_data]
topo_dag_times = [float(r['AvgTimeMs']) for r in topo_data if r['GraphType'] == 'DAG']

# 1. Execution time vs Number of Vertices
plt.figure(figsize=(7, 4.5))
plt.plot(v_vals, kruskal_times, marker='o', label='Kruskal MST')
plt.plot(v_vals, prim_times, marker='s', label='Prim MST')
plt.plot(v_vals, dijkstra_times, marker='^', label='Dijkstra')
plt.plot(v_vals, scc_times, marker='d', label='Kosaraju SCC')
plt.plot(v_vals, topo_dag_times, marker='v', label='Topological Sort')

plt.title('Execution Time vs. Number of Vertices')
plt.xlabel('Vertices (V)')
plt.ylabel('Time (ms)')
plt.xscale('log')
plt.yscale('log')
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig('results/plots/execution_time_vs_vertices.png')
plt.close()

# 2. Execution time vs Number of Edges
plt.figure(figsize=(7, 4.5))
plt.plot(e_vals, kruskal_times, marker='o', label='Kruskal MST')
plt.plot(e_vals, prim_times, marker='s', label='Prim MST')
plt.plot(e_vals, dijkstra_times, marker='^', label='Dijkstra')
plt.plot(e_vals, scc_times, marker='d', label='Kosaraju SCC')
plt.plot(e_vals, topo_dag_times, marker='v', label='Topological Sort')

plt.title('Execution Time vs. Number of Edges')
plt.xlabel('Edges (E)')
plt.ylabel('Time (ms)')
plt.xscale('log')
plt.yscale('log')
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig('results/plots/execution_time_vs_edges.png')
plt.close()

# 3. Kruskal vs Prim Comparison
labels = ['Small (10/30)', 'Medium (100/500)', 'Large (1000/5000)']
x = range(len(labels))
width = 0.35

plt.figure(figsize=(10, 4))

plt.subplot(1, 2, 1)
plt.bar([p - width/2 for p in x], kruskal_times, width=width, label='Kruskal')
plt.bar([p + width/2 for p in x], prim_times, width=width, label='Prim')
plt.title('MST Execution Time (ms)')
plt.ylabel('Time (ms)')
plt.xticks(x, labels)
plt.grid(True, axis='y')
plt.legend()

kruskal_ops = [float(r['AvgTotalOps']) for r in mst_data if r['Algorithm'] == 'Kruskal']
prim_ops = [float(r['AvgTotalOps']) for r in mst_data if r['Algorithm'] == 'Prim']

plt.subplot(1, 2, 2)
plt.bar([p - width/2 for p in x], kruskal_ops, width=width, label='Kruskal')
plt.bar([p + width/2 for p in x], prim_ops, width=width, label='Prim')
plt.title('MST Operations Count')
plt.ylabel('Operations')
plt.yscale('log')
plt.xticks(x, labels)
plt.grid(True, axis='y')
plt.legend()

plt.tight_layout()
plt.savefig('results/plots/kruskal_vs_prim_comparison.png')
plt.close()

# 4. DSU Comparison: Optimized vs Naive
opt_dsu_ops = [float(r['AvgTotalOps']) for r in dsu_data if 'Optimized' in r['DSU_Mode']]
naive_dsu_ops = [float(r['AvgTotalOps']) for r in dsu_data if 'Naive' in r['DSU_Mode']]
opt_dsu_time = [float(r['AvgTimeMs']) for r in dsu_data if 'Optimized' in r['DSU_Mode']]
naive_dsu_time = [float(r['AvgTimeMs']) for r in dsu_data if 'Naive' in r['DSU_Mode']]

plt.figure(figsize=(10, 4))

plt.subplot(1, 2, 1)
plt.bar([p - width/2 for p in x], opt_dsu_ops, width=width, label='Path Compression + Rank')
plt.bar([p + width/2 for p in x], naive_dsu_ops, width=width, label='Naive DSU')
plt.title('DSU Operations')
plt.ylabel('Operations')
plt.yscale('log')
plt.xticks(x, labels)
plt.grid(True, axis='y')
plt.legend()

plt.subplot(1, 2, 2)
plt.bar([p - width/2 for p in x], opt_dsu_time, width=width, label='Optimized')
plt.bar([p + width/2 for p in x], naive_dsu_time, width=width, label='Naive')
plt.title('DSU Execution Time (ms)')
plt.ylabel('Time (ms)')
plt.xticks(x, labels)
plt.grid(True, axis='y')
plt.legend()

plt.tight_layout()
plt.savefig('results/plots/dsu_performance_comparison.png')
plt.close()

# 5. Dijkstra Operations
relaxations = [float(r['AvgRelaxations']) for r in dijkstra_data]
pq_ops = [float(r['AvgPqOps']) for r in dijkstra_data]
comparisons = [float(r['AvgComparisons']) for r in dijkstra_data]

plt.figure(figsize=(7, 4.5))
plt.plot(v_vals, relaxations, marker='o', label='Edge Relaxations')
plt.plot(v_vals, pq_ops, marker='s', label='PQ Operations')
plt.plot(v_vals, comparisons, marker='^', label='Key Comparisons')
plt.title('Dijkstra Operations Breakdown')
plt.xlabel('Vertices (V)')
plt.ylabel('Operation Count')
plt.xscale('log')
plt.yscale('log')
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig('results/plots/dijkstra_operations.png')
plt.close()

print('Plots generated successfully.')
