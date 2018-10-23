package problems;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

public abstract class TSS {
	protected GRBVar[] s;
	protected GRBVar[] a;
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

	public Set<Vertex> getTargetSet() throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		Set<Vertex> tSet = new HashSet<Vertex>();
		for (Vertex v : vSet) {
			System.out.println("["+v.getIndex()+"] = " + s[v.getIndex()].get(GRB.DoubleAttr.X));
			if (s[v.getIndex()].get(GRB.DoubleAttr.X) == 1)
				tSet.add(v);
		}
		return tSet;
	}
}
