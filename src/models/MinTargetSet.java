package models;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.util.SupplierUtil;

import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import problems.TSS;

public class MinTargetSet extends TSS {
	// private GRBVar[][] tournament;
	private int[] thr;

	public MinTargetSet(Graph<Vertex, DefaultEdge> g) {
		super(g);
		this.thr = majorityThreshold(g.vertexSet());
	}

	public GRBModel model(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		//thr = majorityThreshold(vSet);

		// Model
		GRBModel model;
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Target set decision variables: s[v] == 1 if the vertex v is in S.
		s = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v); // Testar com 1 no coef
		}

		// Edge tournament variables: y[u][v] = 1 if (u,v) is an arc
		// of the tournament.
		y = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				y[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "e_" + i + "" + j);
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
				lhs.addTerm(1, y[u.getIndex()][v.getIndex()]);
			}
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, thr[v.getIndex()], "activation_of_" + v.getName());
		}

		// Tournament constraints
		// direction of the arcs: e_uv + e_vu = 1 for i != j
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				lhs = new GRBLinExpr();
				lhs.addTerm(1, y[i][j]);
				lhs.addTerm(1, y[j][i]);
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
							lhs.addTerm(1, y[u][v]);
							lhs.addTerm(1, y[v][w]);
							lhs.addTerm(1, y[w][u]);
							model.addConstr(lhs, GRB.LESS_EQUAL, 2, "cycle(" + u + "" + v + "" + w + ")");
						}
					}
				}
			}
		}

		return model;
	}

	// Create the VertexFactory so the generator can create vertices
	Supplier<Vertex> supplier = new Supplier<Vertex>() {
		private int id = 0;

		@Override
		public Vertex get() {
			Vertex v = new Vertex("v" + (id + 1), id);
			id++;
			return v;
		}
	};

	public Graph<Vertex, DefaultEdge> graphCopy(Graph<Vertex, DefaultEdge> g, Map<Vertex, Vertex> nodeRef,
			Map<DefaultEdge, DefaultEdge> arcRef) {

		Graph<Vertex, DefaultEdge> h;

		Vertex[] vertices = new Vertex[g.vertexSet().size()];

		h = new DirectedPseudograph<>(supplier, SupplierUtil.createDefaultEdgeSupplier(), false);
		int i = 0;
		for (Vertex v : g.vertexSet()) {
			Vertex w = h.addVertex();
			vertices[i++] = w;
			nodeRef.put(w, v);
		}

		for (DefaultEdge e : g.edgeSet()) {
			int j = g.getEdgeSource(e).getIndex();
			int k = g.getEdgeTarget(e).getIndex();
			DefaultEdge a = h.addEdge(vertices[j], vertices[k]);
			arcRef.put(a, e);
		}

		for (i = 0; i < vertices.length; i++) {
			Vertex v = vertices[i];
			if (h.inDegreeOf(v) == 0) {
				System.out.println("removing " + v);
				h.removeVertex(v);
			}

		}
		for (Vertex v : g.vertexSet()) {
			System.out.println(v);
		}

		return h;
	}
	
	/**
	 * Take a copy of the input graph g and remove vertices and arcs
	 * @param g original graph
	 * @param thr threshold vector
	 * @param target set to storage the selected target set
	 * @return pre-processed graph
	 */
	@SuppressWarnings("unchecked")
	public Graph<Vertex, DefaultEdge> preprocessing(
			Graph<Vertex, DefaultEdge> g, 
			int[] thrCopy,
			Set<Vertex> target){
		Graph<Vertex, DefaultEdge> h;
		
		h = (Graph<Vertex, DefaultEdge>) ((AbstractBaseGraph<Vertex, DefaultEdge>) g).clone();

		//int[] thrCopy = new int[g.vertexSet().size()];
		for (int i = 0; i < thrCopy.length; i++) {
			thrCopy[i] = thr[i];
		}

		Set<Vertex> toRemove = new HashSet<>();

		for (Vertex v : h.vertexSet()) {
			if (h.inDegreeOf(v) == 0 && h.outDegreeOf(v) > 0) {
				//s[v.getIndex()].set(GRB.DoubleAttr.LB, 1);
				// System.out.println("in-degree zero, removing " + v);

				// fixing the outgoing edges of v
				for (DefaultEdge e : h.outgoingEdgesOf(v)) {
					// target vertex on the original graph
					Vertex w = g.getEdgeTarget(e);

					// decrease the threshold of w by 1
					thrCopy[w.getIndex()]--;

					//y[v.getIndex()][w.getIndex()].set(GRB.DoubleAttr.LB, 1);
					//System.out.println("fixing edge in one: " + e);
				}
				
				target.add(v);
				toRemove.add(v);
			} else if (h.outDegreeOf(v) == 0 && h.inDegreeOf(v) > 0) {
				
				//s[v.getIndex()].set(GRB.DoubleAttr.UB, 0);
				// System.out.println("out-degree zero, removing " + v);

				// fixing the incoming edges of v
				for (DefaultEdge e : h.incomingEdgesOf(v)) {
					// source vertex on the original graph
					Vertex u = g.getEdgeSource(e);
					//y[u.getIndex()][v.getIndex()].set(GRB.DoubleAttr.LB, 1);
					//System.out.println("fixing edge in one: " + e);
				}

				toRemove.add(v);
			} else if (h.inDegreeOf(v) == 0 && h.outDegreeOf(v) == 0) {
				//s[v.getIndex()].set(GRB.DoubleAttr.LB, 1);
				toRemove.add(v);
			}
			//System.out.println("h.inDegreeOf(" + v + ") = " + h.inDegreeOf(v));
		}

		h.removeAllVertices(toRemove);

		do {
			toRemove.clear();

			for (Vertex v : h.vertexSet()) {
				if (thrCopy[v.getIndex()] <= 0) {
					//System.out.println("thr <= zero " + v);
					//s[v.getIndex()].set(GRB.DoubleAttr.UB, 0);
					for (DefaultEdge e : h.incomingEdgesOf(v)) {
						Vertex u = h.getEdgeSource(e);
						//System.out.println("fixing edge in zero: " + e);
						//y[u.getIndex()][v.getIndex()].set(GRB.DoubleAttr.UB, 0);
					}
					
					//decrease the threshold of the target of arcs removed
					for (DefaultEdge e : h.outgoingEdgesOf(v)) {
						Vertex w = h.getEdgeTarget(e);
						
						// decrease the threshold of w by 1
						thrCopy[w.getIndex()]--;
						
						//y[v.getIndex()][w.getIndex()].set(GRB.DoubleAttr.LB, 1);
					}

					toRemove.add(v);
					break;
				} else if (h.outDegreeOf(v) == 0 && h.inDegreeOf(v) > 0) {
					
					//s[v.getIndex()].set(GRB.DoubleAttr.UB, 0);
					// System.out.println("out-degree zero, removing " + v);

					// fixing the incoming edges of v
					for (DefaultEdge e : h.incomingEdgesOf(v)) {
						// source vertex on the original graph
						Vertex u = g.getEdgeSource(e);
						//y[u.getIndex()][v.getIndex()].set(GRB.DoubleAttr.LB, 1);
						//System.out.println("fixing edge in one: " + e);
					}

					toRemove.add(v);
				}
			}

			h.removeAllVertices(toRemove);
		} while (!toRemove.isEmpty());
		
		return h;
	}

	/*
	 * New model formulation without the tournament constraints
	 */
	public GRBModel model2(GRBEnv env, int[] thr) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		//thr = majorityThreshold(vSet);

		// Model
		GRBModel model;
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Target set decision variables: s[v] == 1 if the vertex v is in S.
		s = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v); // Testar com 1 no coef
		}

		// Edge tournament variables: tournament[u][v] = 1 if (u,v) is an arc
		// of the tournament.
		y = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				y[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "e_" + i + "" + j);
			}
		}

		// Set objective function: maximize a[0] + a[1] + ... + a[n-1]
		GRBLinExpr obj = new GRBLinExpr();
		for (int i = 0; i < n; i++)
			obj.addTerm(1, s[i]);
		model.setObjective(obj, GRB.MINIMIZE);

		// Fixing variables, if the in-degree of v is zero then s[v] = 1
		for (Vertex v : vSet) {
			if (g.inDegreeOf(v) == 0)
				s[v.getIndex()].set(GRB.DoubleAttr.LB, 1);
			else if (g.outDegreeOf(v) == 0)
				s[v.getIndex()].set(GRB.DoubleAttr.UB, 0);
		}
			
		GRBLinExpr lhs;

		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least t(v)
		for (Vertex v : vSet) {
			lhs = new GRBLinExpr();
			for (DefaultEdge e : g.incomingEdgesOf(v)) {
				Vertex u = g.getEdgeSource(e);
				lhs.addTerm(1, y[u.getIndex()][v.getIndex()]);
			}
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, thr[v.getIndex()], "activation_of_" + v.getName());
		}

		// Tournament constraints
		// direction of the arcs: e_uv + e_vu = 1 for i != j
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				lhs = new GRBLinExpr();
				lhs.addTerm(1, y[i][j]);
				lhs.addTerm(1, y[j][i]);
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
							lhs.addTerm(1, y[u][v]);
							lhs.addTerm(1, y[v][w]);
							lhs.addTerm(1, y[w][u]);
							model.addConstr(lhs, GRB.LESS_EQUAL, 2, "cycle(" + u + "" + v + "" + w + ")");
						}
					}
				}
			}
		}

		return model;
	}

	/*
	 * New model formulation without the tournament constraints
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

		return model;
	}

	/*
	 * New model formulation without the tournament constraints but with the edge
	 * variables
	 */
	public GRBModel model4(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		thr = majorityThreshold(vSet);

		// Model
		GRBModel model;
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Target set decision variables: s[v] == 1 if the vertex v is in S.
		s = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.CONTINUOUS, "s_" + v); // Testar com 1 no coef
		}

		// Edge tournament variables: tournament[u][v] = 1 if (u,v) is an arc
		// of the tournament.
		y = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				y[i][j] = model.addVar(0, 1, 0, GRB.CONTINUOUS, "e_" + i + "" + j);
			}
		}

		// Set objective function: minimize s[0] + s[1] + ... + s[n-1]
		GRBLinExpr obj = new GRBLinExpr();
		for (int i = 0; i < n; i++)
			obj.addTerm(1, s[i]);
		model.setObjective(obj, GRB.MINIMIZE);

		// Fixing variables, if the in-degree of v is zero then s[v] = 1
		for (Vertex v : vSet) {
			if (g.inDegreeOf(v) == 0)
				s[v.getIndex()].set(GRB.DoubleAttr.LB, 1);
			else if (g.outDegreeOf(v) == 0)
				s[v.getIndex()].set(GRB.DoubleAttr.UB, 0);
		}

		GRBLinExpr lhs;

		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least t(v)
		for (Vertex v : vSet) {
			lhs = new GRBLinExpr();
			for (DefaultEdge e : g.incomingEdgesOf(v)) {
				Vertex u = g.getEdgeSource(e);
				// lhs.addTerm(1, y[v.getIndex()][u.getIndex()]); //estava funcionando assim
				lhs.addTerm(1, y[u.getIndex()][v.getIndex()]);
			}
			lhs.addTerm(thr[v.getIndex()], s[v.getIndex()]);
			model.addConstr(lhs, GRB.GREATER_EQUAL, thr[v.getIndex()], "activation_of_" + v.getName());
		}

		return model;
	}
}
