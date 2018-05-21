import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;

import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class WTSS {
	private GRBVar[] s;
	private GRBVar[][] h;
	private Graph<Vertex, DefaultEdge> g;
	private GRBModel model;
	private int[] thr, weight;
	private Set<Vertex> targetSet;

	public WTSS(Graph<Vertex, DefaultEdge> g) {
		this.g = g;
	}

	public GRBModel IPModel(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		thr = majorityThreshold(vSet);
		weight = weight(vSet);

		// Model
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Target set decision variables: s[v] == 1 if plant v is in S.
		s = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v); // Testar com 1 no coef
		}

		// Influence direction variables: h[u][v] = 1 if the influence goes from u to v
		h = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				h[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "e_" + i + "" + j);
			}
		}

		// Set objective function: minimize s[0] + s[1] + ... + s[n-1]
		GRBLinExpr obj = new GRBLinExpr();
		for (int i = 0; i < n; i++)
			obj.addTerm(weight[i], s[i]);
		model.setObjective(obj, GRB.MINIMIZE);

		GRBLinExpr lhs;

		// direction of the arcs: h_uv + h_vu = 1 for i != j
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				lhs = new GRBLinExpr();
				lhs.addTerm(1, h[i][j]);
				lhs.addTerm(1, h[j][i]);
				model.addConstr(lhs, GRB.EQUAL, 1, "arc_" + i + "" + j);
			}
		}
		
		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least t(v)
		for (Vertex v : vSet) {
			lhs = new GRBLinExpr();
			for (Vertex u : Graphs.neighborListOf(g, v))
				lhs.addTerm(1, h[v.getIndex()][u.getIndex()]);
			
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			
			model.addConstr(lhs, GRB.GREATER_EQUAL, thr[v.getIndex()], 
					"activation_of_" + v.getName());
		}
		
		return model;
	}

	/**
	 * Draw an weight each vertex in the graph
	 * 
	 * @param vSet
	 *            set of vertex
	 * @return a vector b of weights
	 */
	private int[] weight(Set<Vertex> vSet) {
		int[] b = new int[vSet.size()];
		
		Random r = new Random();
		
		for (Vertex v : vSet) {
			b[v.getIndex()] = r.nextInt(vSet.size() - 1) + 1;
			v.setWeight(b[v.getIndex()]);
		}
		return b;
	}
	
	/**
	 * Determine the threshold of each vertex in the graph
	 * 
	 * @param vSet
	 *            set of vertex
	 * @return a vector of threshold
	 */
	private int[] majorityThreshold(Set<Vertex> vSet) {
		int[] thr = new int[vSet.size()];
		for (Vertex v : vSet) {
			thr[v.getIndex()] = (int) g.inDegreeOf(v) / 2 + 1;
			v.setThreshold(thr[v.getIndex()]);
		}
		return thr;
	}

	private Set<Vertex> getTargetSet() throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		Set<Vertex> tSet = new HashSet<Vertex>();
		for (Vertex v : vSet) {
			if (s[v.getIndex()].get(GRB.DoubleAttr.X) == 1)
				tSet.add(v);
		}
		return tSet;
	}

}
