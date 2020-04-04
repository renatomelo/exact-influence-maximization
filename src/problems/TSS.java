package problems;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

public abstract class TSS {
	protected GRBVar[] s;
	protected GRBVar[] a;
	protected GRBVar[][] direction, h,y; // defined for the wtss
	protected Graph<Vertex, DefaultEdge> g;
	protected Set<Vertex> targetSet;

	public TSS(Graph<Vertex, DefaultEdge> g) {
		this.g = g;
	}

	public abstract GRBModel model(GRBEnv env) throws GRBException;

	/**
	 * Determine the threshold of each vertex in the graph
	 * 
	 * @param vSet
	 *            set of vertex
	 * @return a vector of threshold
	 */
	public int[] majorityThreshold(Set<Vertex> vSet) {
		int[] thr = new int[vSet.size()];

		for (Vertex v : vSet) {
			if (g.inDegreeOf(v) == 0) {
				thr[v.getIndex()] = 1;
			} else {
				thr[v.getIndex()] = (int) Math.ceil((double) g.inDegreeOf(v) / 2);
			}
			v.setThreshold(thr[v.getIndex()]);
		}
		return thr;
	}
	
	public int[] randomThreshold(Set<Vertex> vSet) {
		int[] thr = new int[vSet.size()];
		Random r = new Random();
		for (Vertex v : vSet) {
			if (g.inDegreeOf(v) <= 1) {
				thr[v.getIndex()] = 1;
			} else {
				int t = r.nextInt(g.inDegreeOf(v) - 1) + 1;
				thr[v.getIndex()] = t;
			}
			v.setThreshold(thr[v.getIndex()]);
		}
		return thr;
	}

	public Set<Vertex> getTargetSet() throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		Set<Vertex> tSet = new HashSet<Vertex>();
		for (Vertex v : vSet) {
			System.out.println("[" + v.getIndex() + "] = " + s[v.getIndex()].get(GRB.DoubleAttr.X));
			if (s[v.getIndex()].get(GRB.DoubleAttr.X) == 1)
				tSet.add(v);
		}
		// print the selected edges
//		for (int i = 0; i < y.length; i++) {
//			for (int j = 0; j < y.length; j++) {
//				// System.out.print("["+i+", "+j+"] = " +
//				// direction[i][j].get(GRB.DoubleAttr.X));
//				System.out.print(y[i][j].get(GRB.DoubleAttr.X) + "\t");
//			}
//			System.out.println();
//		}
		return tSet;
	}

	public DefaultDirectedGraph<Vertex, DefaultEdge> getSolutionGraph() throws GRBException {
		DefaultDirectedGraph<Vertex, DefaultEdge> solution = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (Vertex v : g.vertexSet()) 
			solution.addVertex(v);
		
		for (DefaultEdge e : g.edgeSet()) {
			Vertex v = g.getEdgeSource(e);
			Vertex w = g.getEdgeTarget(e);
			if(y[v.getIndex()][w.getIndex()].get(GRB.DoubleAttr.X) == 1)
				solution.addEdge(v, w);
		}
		
		return solution;
	}
}
