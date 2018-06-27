package models;
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
import problems.TSS;

public class MinTargetSet extends TSS{
	private GRBVar[][] tournament;
	private int[] thr;
	
	public MinTargetSet(Graph<Vertex, DefaultEdge> g) {
		super(g);
	}

	public GRBModel model(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		thr = majorityThreshold(vSet);

		// Model
		GRBModel model;
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

	public GRBModel model2(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		thr = majorityThreshold(vSet);

		// Model
		GRBModel model;
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

		// Set as target each vertex with no
		// in-neighbors
		for (Vertex v : vSet) {
			// s[v] >= 1 - in_deg(v)
			lhs = new GRBLinExpr();
			lhs.addTerm(1, s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, 1 - g.inDegreeOf(v), "in_deg_of_" + v.getName());
		}
		// and set as no target every node with no out-neighbors
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
	public GRBModel model3(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		thr = majorityThreshold(vSet);

		// Model
		GRBModel model;
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "no_tournament");
		
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

		// Set objective function: minimize s[0] + s[1] + ... + s[n-1]
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
				lhs.addTerm(1, a[u.getIndex()]);
			}
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, thr[v.getIndex()], "activation_of_" + v.getName());
		}
		
		// Set as target each vertex with no in-neighbors
		for (Vertex v : vSet) {
			// s[v] >= 1 - in_deg(v)
			lhs = new GRBLinExpr();
			lhs.addTerm(1, s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, 1 - g.inDegreeOf(v), "in_deg_of_" + v.getName());
		}
		
		return model;
	}
}
