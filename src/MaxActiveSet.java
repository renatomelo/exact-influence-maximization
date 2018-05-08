import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import activable_network.GraphGen;
import activable_network.GraphViewer;
import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class MaxActiveSet {
	private GRBVar[] s;
	private GRBVar[] a;
	private GRBVar[][] tournament;
	private Graph<Vertex, DefaultEdge> g;
	private GRBModel model;
	private int[] thr;
	private Set<Vertex> targetSet;
	
	int k = 3;

	public MaxActiveSet(Graph<Vertex, DefaultEdge> g) {
		this.g = g;
	}

	public GRBModel IPModel(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		thr = majorityThreshold(vSet);

		// Model
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Active set decision variables: a[v] == 1 if plant v is active.
		a = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			a[v] = model.addVar(0, 1, 0, GRB.BINARY, "a_" + v);
		}

		// Target set decision variables: s[v] == 1 if plant v is in S.
		s = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v);
		}

		// Edge tournament variables: tournament[u][v] = 1 if (u,v) is an arc
		// of the tournament.
		tournament = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				tournament[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "e_" + i + "" + j);
			}
		}

		// Set objective function: maximize a[0] + a[1] + ... + a[n-1]
		GRBLinExpr obj = new GRBLinExpr();
		for (int i = 0; i < a.length; i++)
			obj.addTerm(1, a[i]);
		model.setObjective(obj, GRB.MAXIMIZE);

		GRBLinExpr lhs;
		GRBLinExpr rhs;

		// Target set constraint: the size of the target set is at most k
		lhs = new GRBLinExpr();
		for (int i = 0; i < n; i++)
			lhs.addTerm(1, s[i]);
		model.addConstr(lhs, GRB.LESS_EQUAL, k, "size_of_s");

		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least t(v)
		for (Vertex v : vSet) {
			lhs = new GRBLinExpr();
			for (DefaultEdge e : g.incomingEdgesOf(v)) {
				Vertex u = g.getEdgeSource(e);
				lhs.addTerm(1, tournament[v.getIndex()][u.getIndex()]);
			}

			rhs = new GRBLinExpr();
			rhs.addTerm(thr[v.getIndex()], a[v.getIndex()]); // t_i(a_i - s_i)
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, rhs, "activation_of_" + v.getName());
		}

		// Tournament constraints
		// direction of the arcs: e_uv + e_vu = 1 for i != j
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				lhs = new GRBLinExpr();
				lhs.addTerm(1, tournament[i][j]);
				lhs.addTerm(1, tournament[j][i]);
				model.addConstr(lhs, GRB.EQUAL, 1, "arc_" + i + "" + j);
			}
		}

		// Cycle prevention constraints
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				if (v != u) {
					for (int w = 0; w < n; w++) {
						if (w != v && w != u) {
							lhs = new GRBLinExpr();
							lhs.addTerm(1, tournament[u][v]);
							lhs.addTerm(1, tournament[v][w]);
							lhs.addTerm(1, tournament[w][u]);
							model.addConstr(lhs, GRB.LESS_EQUAL, 2, "cycle(" + u + "" + v + "" + w + ")");
						}
					}
				}
			}
		}

		// Target set is in active set
		for (int i = 0; i < n; i++) {
			lhs = new GRBLinExpr();
			lhs.addTerm(1, a[i]);
			rhs = new GRBLinExpr();
			rhs.addTerm(1, s[i]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, rhs, "tgt_actv" + i);
		}

		return model;
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

	public Set<Vertex> getTargetSet() throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		Set<Vertex> tSet = new HashSet<Vertex>();
		for (Vertex v : vSet) {
			if (s[v.getIndex()].get(GRB.DoubleAttr.X) == 1)
				tSet.add(v);
		}
		return tSet;
	}

	public static void main(String[] args) {
		GraphViewer<Vertex, DefaultEdge> viewer;

		int size = 5;
		Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

		// Generate a random scale free graph
		new GraphGen().scaleFree(g, size);

		MaxActiveSet tss = new MaxActiveSet(g);
		Set<Vertex> tSet = null;
		// Formulate a Integer Linear Program using the random graph as instance
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = tss.IPModel(env);
			model.optimize();

			tSet = tss.getTargetSet();

			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

		for (Vertex v : tSet)
			v.setActive(true);

		viewer = new GraphViewer<>(g);
		viewer.initComponents();
	}
}