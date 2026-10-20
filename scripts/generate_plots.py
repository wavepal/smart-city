import csv
import os
import matplotlib.pyplot as plt

os.makedirs('results/plots', exist_ok=True)

# Set clean aesthetic style
plt.style.use('seaborn-v0_8-whitegrid' if 'seaborn-v0_8-whitegrid' in plt.style.available else 'default')
plt.rcParams['font.sans-serif'] = 'DejaVu Sans'
plt.rcParams['axes.edgecolor'] = '#cccccc'
plt.rcParams['axes.linewidth'] = 0.8

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

# 1. Execution time vs Number of Vertices
fig, ax = plt.subplots(figsize=(8, 5), dpi=300)
v_vals = [10, 100, 1000]

kruskal_times = [float(r['AvgTimeMs']) for r in mst_data if r['Algorithm'] == 'Kruskal']
prim_times = [float(r['AvgTimeMs']) for r in mst_data if r['Algorithm'] == 'Prim']
dijkstra_times = [float(r['AvgTimeMs']) for r in dijkstra_data]
scc_times = [float(r['AvgTimeMs']) for r in scc_data]
topo_dag_times = [float(r['AvgTimeMs']) for r in topo_data if r['GraphType'] == 'DAG']

ax.plot(v_vals, kruskal_times, marker='o', linewidth=2, label='Kruskal MST (DSU)', color='#1f77b4')
ax.plot(v_vals, prim_times, marker='s', linewidth=2, label='Prim MST (PriorityQueue)', color='#ff7f0e')
ax.plot(v_vals, dijkstra_times, marker='^', linewidth=2, label="Dijkstra's Shortest Path", color='#2ca02c')
ax.plot(v_vals, scc_times, marker='d', linewidth=2, label="Tarjan SCC", color='#d62728')
ax.plot(v_vals, topo_dag_times, marker='v', linewidth=2, label="Topological Sort (DAG)", color='#9467bd')

ax.set_title('Execution Time vs. Number of Vertices (V)', fontsize=13, fontweight='bold', pad=12)
ax.set_xlabel('Number of Vertices (V)', fontsize=11)
ax.set_ylabel('Execution Time (ms)', fontsize=11)
ax.set_xscale('log')
ax.set_yscale('log')
ax.legend(frameon=True, facecolor='white', framealpha=0.9)
plt.tight_layout()
plt.savefig('results/plots/execution_time_vs_vertices.png')
plt.close()
print("Saved execution_time_vs_vertices.png")

# 2. Execution time vs Number of Edges
fig, ax = plt.subplots(figsize=(8, 5), dpi=300)
e_vals = [30, 500, 5000]

ax.plot(e_vals, kruskal_times, marker='o', linewidth=2, label='Kruskal MST', color='#1f77b4')
ax.plot(e_vals, prim_times, marker='s', linewidth=2, label='Prim MST', color='#ff7f0e')
ax.plot(e_vals, dijkstra_times, marker='^', linewidth=2, label="Dijkstra's Algorithm", color='#2ca02c')
ax.plot(e_vals, scc_times, marker='d', linewidth=2, label="Tarjan SCC", color='#d62728')
ax.plot(e_vals, topo_dag_times, marker='v', linewidth=2, label="Topological Sort (DAG)", color='#9467bd')

ax.set_title('Execution Time vs. Number of Edges (E)', fontsize=13, fontweight='bold', pad=12)
ax.set_xlabel('Number of Edges (E)', fontsize=11)
ax.set_ylabel('Execution Time (ms)', fontsize=11)
ax.set_xscale('log')
ax.set_yscale('log')
ax.legend(frameon=True, facecolor='white', framealpha=0.9)
plt.tight_layout()
plt.savefig('results/plots/execution_time_vs_edges.png')
plt.close()
print("Saved execution_time_vs_edges.png")

# 3. Kruskal vs Prim Comparison (Time & Operations)
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5), dpi=300)
labels = ['Small (10/30)', 'Medium (100/500)', 'Large (1000/5000)']
x = range(len(labels))
width = 0.35

ax1.bar([p - width/2 for p in x], kruskal_times, width=width, label='Kruskal', color='#1f77b4')
ax1.bar([p + width/2 for p in x], prim_times, width=width, label='Prim', color='#ff7f0e')
ax1.set_title('MST Execution Time Comparison', fontsize=12, fontweight='bold')
ax1.set_ylabel('Execution Time (ms)')
ax1.set_xticks(x)
ax1.set_xticklabels(labels)
ax1.legend()

kruskal_ops = [float(r['AvgTotalOps']) for r in mst_data if r['Algorithm'] == 'Kruskal']
prim_ops = [float(r['AvgTotalOps']) for r in mst_data if r['Algorithm'] == 'Prim']

ax2.bar([p - width/2 for p in x], kruskal_ops, width=width, label='Kruskal', color='#1f77b4')
ax2.bar([p + width/2 for p in x], prim_ops, width=width, label='Prim', color='#ff7f0e')
ax2.set_title('MST Operation Count Comparison', fontsize=12, fontweight='bold')
ax2.set_ylabel('Total Operation Count (log scale)')
ax2.set_yscale('log')
ax2.set_xticks(x)
ax2.set_xticklabels(labels)
ax2.legend()

plt.tight_layout()
plt.savefig('results/plots/kruskal_vs_prim_comparison.png')
plt.close()
print("Saved kruskal_vs_prim_comparison.png")

# 4. DSU Comparison: Optimized vs Naive
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5), dpi=300)
opt_dsu_ops = [float(r['AvgTotalOps']) for r in dsu_data if 'Optimized' in r['DSU_Mode']]
naive_dsu_ops = [float(r['AvgTotalOps']) for r in dsu_data if 'Naive' in r['DSU_Mode']]
opt_dsu_time = [float(r['AvgTimeMs']) for r in dsu_data if 'Optimized' in r['DSU_Mode']]
naive_dsu_time = [float(r['AvgTimeMs']) for r in dsu_data if 'Naive' in r['DSU_Mode']]

ax1.bar([p - width/2 for p in x], opt_dsu_ops, width=width, label='Optimized (PathComp + Rank)', color='#2ca02c')
ax1.bar([p + width/2 for p in x], naive_dsu_ops, width=width, label='Naive (No PathComp, No Rank)', color='#d62728')
ax1.set_title('DSU Operations: Optimized vs Naive', fontsize=12, fontweight='bold')
ax1.set_ylabel('DSU Operations (log scale)')
ax1.set_yscale('log')
ax1.set_xticks(x)
ax1.set_xticklabels(labels)
ax1.legend()

ax2.bar([p - width/2 for p in x], opt_dsu_time, width=width, label='Optimized DSU', color='#2ca02c')
ax2.bar([p + width/2 for p in x], naive_dsu_time, width=width, label='Naive DSU', color='#d62728')
ax2.set_title('Execution Time: Optimized vs Naive', fontsize=12, fontweight='bold')
ax2.set_ylabel('Time (ms)')
ax2.set_xticks(x)
ax2.set_xticklabels(labels)
ax2.legend()

plt.tight_layout()
plt.savefig('results/plots/dsu_performance_comparison.png')
plt.close()
print("Saved dsu_performance_comparison.png")

# 5. Dijkstra Operation Breakdown
fig, ax = plt.subplots(figsize=(8, 5), dpi=300)
relaxations = [float(r['AvgRelaxations']) for r in dijkstra_data]
pq_ops = [float(r['AvgPqOps']) for r in dijkstra_data]
comparisons = [float(r['AvgComparisons']) for r in dijkstra_data]

ax.plot(v_vals, relaxations, marker='o', linewidth=2, label='Edge Relaxations', color='#1f77b4')
ax.plot(v_vals, pq_ops, marker='s', linewidth=2, label='PriorityQueue Operations', color='#ff7f0e')
ax.plot(v_vals, comparisons, marker='^', linewidth=2, label='Key Comparisons in PQ', color='#2ca02c')
ax.set_title("Dijkstra Operation Breakdown vs. Vertices", fontsize=13, fontweight='bold', pad=12)
ax.set_xlabel('Number of Vertices (V)', fontsize=11)
ax.set_ylabel('Operation Count', fontsize=11)
ax.set_xscale('log')
ax.set_yscale('log')
ax.legend(frameon=True, facecolor='white', framealpha=0.9)
plt.tight_layout()
plt.savefig('results/plots/dijkstra_operations.png')
plt.close()
print("Saved dijkstra_operations.png")
