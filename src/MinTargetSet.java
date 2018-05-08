import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import activable_network.GraphGen;
import activable_network.GraphViewer;
import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class MinTargetSet {
	private GRBVar[] s;
	private GRBVar[] a;
	private GRBVar[][] tournament;
	private Graph<Vertex, DefaultEdge> g;
	private GRBModel model;
	private int[] thr;
	private Set<Vertex> targetSet;

	public MinTargetSet(Graph<Vertex, DefaultEdge> g) {
		this.g = g;
	}

	public GRBModel IPModel(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		thr = majorityThreshold(vSet);

		// Model
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Target set decision variables: s[v] == 1 if plant v is in S.
		s = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v); // Testar com 1 no coef
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
		for (int i = 0; i < n; i++)
			obj.addTerm(1, s[i]);
		model.setObjective(obj, GRB.MINIMIZE);

		GRBLinExpr lhs;

		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least t(v)
		for (Vertex v : vSet) {
			lhs = new GRBLinExpr();
			for (DefaultEdge e : g.incomingEdgesOf(v)) {
				Vertex u = g.getEdgeSource(e);
				lhs.addTerm(1, tournament[v.getIndex()][u.getIndex()]);
			}
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, thr[v.getIndex()], "activation_of_" + v.getName());
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

		return model;
	}

	public GRBModel IPModel2(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		thr = majorityThreshold(vSet);

		// Model
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Target set decision variables: s[v] == 1 if plant v is in S.
		s = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v); // Testar com 1 no coef
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
		for (int i = 0; i < n; i++)
			obj.addTerm(1, s[i]);
		model.setObjective(obj, GRB.MINIMIZE);

		GRBLinExpr lhs;

		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least t(v)
		for (Vertex v : vSet) {
			lhs = new GRBLinExpr();
			for (DefaultEdge e : g.incomingEdgesOf(v)) {
				Vertex u = g.getEdgeSource(e);
				lhs.addTerm(1, tournament[v.getIndex()][u.getIndex()]);
			}
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, thr[v.getIndex()], "activation_of_" + v.getName());
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

		// Two new constraints whose the goal is to set as target each vertex with no
		// in-neighbors
		// and set as no target every node with no out-neighbors
		for (Vertex v : vSet) {
			// s[v] >= 1 - in_deg(v)
			lhs = new GRBLinExpr();
			lhs.addTerm(1, s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, 1 - g.inDegreeOf(v), "in_deg_of_" + v.getName());
		}

		for (Vertex v : vSet) {
			// s[v] <= out_deg(v)
			lhs = new GRBLinExpr();
			lhs.addTerm(1, s[v.getIndex()]);
			model.addConstr(lhs, GRB.LESS_EQUAL, g.outDegreeOf(v), "in_deg_of_" + v.getName());
		}

		// Are above constraint redundant?

		return model;
	}

	/*
	 * New model formulation to avoid the tournament constraints
	 */
	public GRBModel IPModel3(GRBEnv env) throws GRBException {
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
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v); // Testar com 1 no coef
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
		for (int i = 0; i < n; i++)
			obj.addTerm(1, s[i]);
		model.setObjective(obj, GRB.MINIMIZE);

//		for (Vertex v : vSet) {
//			if (g.inDegreeOf(v) == 0) {
//				for (int i = 0; i < n; i++) {
//					tournament[i][v.getIndex()].set(DoubleAttr.UB, 0);
//				}
//			} 
//			else if (g.outDegreeOf(v) == 0) {
//				for (int i = 0; i < n; i++) {
//					tournament[v.getIndex()][i].set(DoubleAttr.UB, 0);
//				}
//			}
//		}
		
		GRBLinExpr lhs;
		GRBLinExpr rhs;
		// In-edges in the tournament should be zero if v has no in-neighbors in G
//		for (Vertex v : vSet) {
////			for (int i = 0; i < n; i++) {
////				lhs = new GRBLinExpr();
////				lhs.addTerm(1, tournament[i][v.getIndex()]);
////				model.addConstr(lhs, GRB.LESS_EQUAL, g.inDegreeOf(v), "in_edge_of_"+v.getName());
////			}
//			for (int i = 0; i < n; i++) {
//				lhs = new GRBLinExpr();
//				lhs.addTerm(1, tournament[v.getIndex()][i]);
//				model.addConstr(lhs, GRB.LESS_EQUAL, g.outDegreeOf(v), "in_edge_of_"+v.getName());
//			}
//		}
		
		// Active set constraint: the size of the active set is equals to n
		lhs = new GRBLinExpr();
		for (int i = 0; i < n; i++)
			lhs.addTerm(1, a[i]);
		model.addConstr(lhs, GRB.EQUAL, n, "size_of_a");
		
		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least t(v)
		for (Vertex v : vSet) {
			lhs = new GRBLinExpr();
			for (DefaultEdge e : g.incomingEdgesOf(v)) {
				Vertex u = g.getEdgeSource(e);
				lhs.addTerm(1, a[u.getIndex()]);
			}
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, thr[v.getIndex()], "activation_of_" + v.getName());
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

	private Set<Vertex> getTargetSet() throws GRBException {
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

		int size = 90;
		Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

		// Generate a random scale free graph
		new GraphGen().scaleFree(g, size);
//		g = new GraphGen().star2(g, size);

		MinTargetSet tss = new MinTargetSet(g);
		// MaxActiveSet tss = new MaxActiveSet(g);
		Set<Vertex> tSet = null;

		// To work with a fixed graph

		// Formulate a Integer Linear Program using the random graph as instance
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model;

			model = tss.IPModel3(env);
			model.optimize();

			model.dispose();
			env.dispose();

			env = new GRBEnv();
			model = tss.IPModel2(env);
			model.optimize();

//			model.dispose();
//			env.dispose();
//
//			env = new GRBEnv();
//			model = tss.IPModel(env);
//			model.optimize();

//			 tSet = tss.getTargetSet();

			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

//		 for (Vertex v : tSet)
//		 v.setActive(true);

//		 viewer = new GraphViewer<>(g);
//		 viewer.initComponents();
	}
}
