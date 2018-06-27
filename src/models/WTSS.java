package models;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import problems.TSS;

public class WTSS extends TSS {
	private GRBVar[][] direction, h;
	private Graph<Vertex, DefaultEdge> dummy;
	private Set<Vertex> D;

	public WTSS(Graph<Vertex, DefaultEdge> g) {
		super(g);
	}

	public GRBModel model(GRBEnv env) throws GRBException {
		Set<Vertex> vSet = g.vertexSet();
		int n = vSet.size();

		majorityThreshold(vSet);
		assignWeight(vSet);

		// Model
		GRBModel model;
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Target set decision variables: s[v] == 1 if plant v is in S.
		s = new GRBVar[n];
		for (int v = 0; v < n; ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v);
		}

		// Influence direction variables: h[u][v] = 1 if the influence goes from u to v
		direction = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				direction[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "e_" + i + "" + j);
			}
		}

		// Set objective function: minimize s[0] + s[1] + ... + s[n-1]
		GRBLinExpr obj = new GRBLinExpr();
		for (Vertex v : g.vertexSet())
			obj.addTerm(v.getWeight(), s[v.getIndex()]);
		model.setObjective(obj, GRB.MINIMIZE);

		GRBLinExpr lhs;

		// direction of the arcs: h_uv + h_vu = 1 for i != j
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				lhs = new GRBLinExpr();
				lhs.addTerm(1, direction[i][j]);
				lhs.addTerm(1, direction[j][i]);
				model.addConstr(lhs, GRB.EQUAL, 1, "arc_" + i + "" + j);
			}
		}

		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least t(v)
		for (Vertex v : vSet) {
			lhs = new GRBLinExpr();
			for (Vertex u : Graphs.neighborListOf(g, v))
				lhs.addTerm(1, direction[v.getIndex()][u.getIndex()]);

			lhs.addTerm(v.getThreshold(), s[v.getIndex()]);

			model.addConstr(lhs, GRB.GREATER_EQUAL, v.getThreshold(), "activation_of_" + v.getName());
		}

		return model;
	}

	/**
	 * tight and compact extended formulation for the WTSS problem on trees from S.
	 * Raghavan's paper
	 * 
	 * @param env
	 * @return
	 * @throws GRBException
	 */
	public GRBModel model2(GRBEnv env) throws GRBException {
		dummy = generateDummyGraph(g);
		int n = dummy.vertexSet().size();

		majorityThreshold(g.vertexSet());
		assignWeight(g.vertexSet());

		// Visualize dummy graph
		// GraphViewer<Vertex, DefaultEdge> viewer;
		// viewer = new GraphViewer<>(dummy);
		// viewer.initComponents();

		// Model
		GRBModel model;
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "MinTS");

		// Target set decision variables: s[v] == 1 if plant v is in S.
		s = new GRBVar[g.vertexSet().size()];
		for (int v = 0; v < g.vertexSet().size(); ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.CONTINUOUS, "s_" + v);
		}

		// Influence direction variables: h[u][v] = 1 if the influence goes from u to v
		direction = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				direction[i][j] = model.addVar(0, 1, 0, GRB.CONTINUOUS, "e_" + i + "" + j);
			}
		}

		// Set objective function: minimize s[0] + s[1] + ... + s[n-1]
		GRBLinExpr obj = new GRBLinExpr();
		for (Vertex v : g.vertexSet())
			obj.addTerm(v.getWeight(), s[v.getIndex()]);
		model.setObjective(obj, GRB.MINIMIZE);

		GRBLinExpr lhs, rhs;

		// Each dummy node has at least one incoming arc
		for (Vertex d : D) {
			lhs = new GRBLinExpr();
			for (Vertex v : Graphs.neighborListOf(dummy, d))
				lhs.addTerm(1, direction[v.getIndex()][d.getIndex()]);

			model.addConstr(lhs, GRB.GREATER_EQUAL, 1, "incoming_of_" + d.getName());
		}

		// if a node is selected, then, it sends out influence to all its neighbors
		for (Vertex v : g.vertexSet()) {
			for (Vertex d : Graphs.neighborListOf(dummy, v)) {
				lhs = new GRBLinExpr();
				rhs = new GRBLinExpr();
				lhs.addTerm(1, s[v.getIndex()]);
				rhs.addTerm(1, direction[v.getIndex()][d.getIndex()]);
				model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "s_" + v.getIndex() + "<=y_" + d.getIndex());
			}
		}

		// direction of the arcs: h_uv + h_vu = 1 for i != j
		for (Vertex d : D) {
			for (Vertex w : Graphs.neighborListOf(dummy, d)) {
				lhs = new GRBLinExpr();
				lhs.addTerm(1, direction[w.getIndex()][d.getIndex()]);
				lhs.addTerm(1, direction[d.getIndex()][w.getIndex()]);
				model.addConstr(lhs, GRB.EQUAL, 1, "arc_" + d.getName() + "" + w.getName());
			}
		}

		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least 1
		for (Vertex v : g.vertexSet()) {
			lhs = new GRBLinExpr();
			for (Vertex d : Graphs.neighborListOf(dummy, v)) {
				lhs.addTerm(1, direction[d.getIndex()][v.getIndex()]);
			}
			lhs.addTerm(v.getThreshold(), s[v.getIndex()]);
			model.addConstr(lhs, GRB.EQUAL, v.getThreshold(), "activatin_of_" + v.getName());
		}

		return model;
	}

	/**
	 * Tight and compact extended formulation for the WTSS problem on general graphs
	 * from S. Raghavan's paper
	 * 
	 * @param env
	 * @return
	 * @throws GRBException
	 */
	public GRBModel model3(GRBEnv env) throws GRBException {
		dummy = generateDummyGraph(g);
		int n = dummy.vertexSet().size();

		majorityThreshold(g.vertexSet());
		assignWeight(g.vertexSet());

		// Model
		GRBModel model;
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "GeneralWTSS");

		// Target set decision variables: s[v] == 1 if plant v is in S.
		s = new GRBVar[g.vertexSet().size()];
		for (int v = 0; v < g.vertexSet().size(); ++v) {
			s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" + v);
		}

		// Influence direction variables: y[u][v] = 1 if the influence goes from u to v
		direction = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				direction[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "e_" + i + "" + j);
			}
		}

		// Influence direction variables: h[u][v] = 1 if the influence goes from u to v
		h = new GRBVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				h[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "h_" + i + "" + j);
			}
		}

		// Set objective function: minimize s[0] + s[1] + ... + s[n-1]
		GRBLinExpr obj = new GRBLinExpr();
		for (Vertex v : g.vertexSet())
			obj.addTerm(v.getWeight(), s[v.getIndex()]);
		model.setObjective(obj, GRB.MINIMIZE);

		GRBLinExpr lhs, rhs;

		// Each dummy node has at least one incoming arc
		for (Vertex d : D) {
			lhs = new GRBLinExpr();
			for (Vertex v : Graphs.neighborListOf(dummy, d))
				lhs.addTerm(1, direction[v.getIndex()][d.getIndex()]);

			model.addConstr(lhs, GRB.GREATER_EQUAL, 1, "incoming_of_" + d.getName());
		}

		// if a node is selected, then it sends out influence to all its neighbors
		for (Vertex v : g.vertexSet()) {
			for (Vertex d : Graphs.neighborListOf(dummy, v)) {
				lhs = new GRBLinExpr();
				rhs = new GRBLinExpr();
				lhs.addTerm(1, s[v.getIndex()]);
				rhs.addTerm(1, direction[v.getIndex()][d.getIndex()]);
				model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "s_" + v.getIndex() + "<=y_" + d.getIndex());
			}
		}

		// direction of the arcs: h_uv + h_vu = 1 for i != j
		for (Vertex d : D) {
			for (Vertex w : Graphs.neighborListOf(dummy, d)) {
				lhs = new GRBLinExpr();
				lhs.addTerm(1, direction[w.getIndex()][d.getIndex()]);
				lhs.addTerm(1, direction[d.getIndex()][w.getIndex()]);
				model.addConstr(lhs, GRB.EQUAL, 1, "arc_" + d.getName() + "" + w.getName());
			}
		}

		// Activation constraints: a vertex v will be activated if the number of
		// active in-neighbors is at least 1
		for (Vertex v : g.vertexSet()) {
			lhs = new GRBLinExpr();
			for (Vertex d : Graphs.neighborListOf(dummy, v)) {
				lhs.addTerm(1, direction[d.getIndex()][v.getIndex()]);
			}
			lhs.addTerm(v.getThreshold(), s[v.getIndex()]);
			model.addConstr(lhs, GRB.EQUAL, v.getThreshold(), "activatin_of_" + v.getName());
		}

		// direction of the arcs: h_uv + h_vu = 1
		for (Vertex v : g.vertexSet()) {
			for (Vertex w : Graphs.neighborListOf(g, v)) {
				lhs = new GRBLinExpr();
				lhs.addTerm(1, h[v.getIndex()][w.getIndex()]);
				lhs.addTerm(1, h[w.getIndex()][v.getIndex()]);
				model.addConstr(lhs, GRB.EQUAL, 1, "arc_" + v.getName() + "" + w.getName());
			}
		}

		// Synchronize the influence propagation process between the graph G and the Dummy graph
		for (Vertex v : g.vertexSet()) {
			for (Vertex d : Graphs.neighborListOf(dummy, v)) {
				// as a dummy node d has only two neighbors, chose a neighbor w
				Vertex w = Graphs.neighborListOf(dummy, d).iterator().next();
				// if w = v then pick the next neighbor of d
				if (w.getIndex() == v.getIndex())
					w = Graphs.neighborListOf(dummy, d).iterator().next();
				
				//h_vw <= y_vd
				lhs = new GRBLinExpr();	rhs = new GRBLinExpr();
				lhs.addTerm(1, h[v.getIndex()][w.getIndex()]);
				rhs.addTerm(1, direction[v.getIndex()][d.getIndex()]);
				model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "h_" + v.getIndex() + w.getIndex() +"<=y_" + d.getIndex());
				
				//h_wv <= y_wd
				lhs = new GRBLinExpr();	rhs = new GRBLinExpr();
				lhs.addTerm(1, h[w.getIndex()][v.getIndex()]);
				rhs.addTerm(1, direction[w.getIndex()][d.getIndex()]);
				model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "h_"+ w.getIndex()  + v.getIndex() + "<=y_" + d.getIndex());
			}
		}
		
		// directed cycle elimination
		for(int i = 3; i <= g.vertexSet().size(); i++) {
			lhs = new GRBLinExpr();
			for (int j = i+1; j < g.vertexSet().size(); j++) {
				lhs.addTerm(1, h[i][j]);
			}
			model.addConstr(lhs, GRB.LESS_EQUAL, i-1, "cycle_"+ i + "<=c-1");
		}
		

		return model;
	}

	/**
	 * Draw an weight each vertex in the graph
	 * 
	 * @param vSet
	 *            set of vertex
	 * @param g
	 * @return a vector b of weights
	 */
	private void assignWeight(Set<Vertex> vSet) {
		Random r = new Random();

		for (Vertex v : g.vertexSet())
			v.setWeight(r.nextInt(vSet.size() - 1) + 1);
	}

	/**
	 * Creates a dummy graph from a undirected graph. For each edge of a graph G are
	 * created a new vertex d. Obs: Running only in tree for while!
	 * 
	 * @param g
	 *            the original graph
	 * @return dummy a new graph
	 */
	private Graph<Vertex, DefaultEdge> generateDummyGraph(Graph<Vertex, DefaultEdge> g) {
		int n = g.vertexSet().size();
		boolean visited[] = new boolean[n];
		Graph<Vertex, DefaultEdge> dummy = new SimpleGraph<>(DefaultEdge.class);
		Queue<Vertex> Q = new LinkedList<>();
		D = new HashSet<>();

		// arbitrary vertex to be the root of bfs
		Vertex u = g.vertexSet().iterator().next();

		for (int j = 0; j < n; j++)
			visited[j] = false;

		visited[u.getIndex()] = true;
		dummy.addVertex(u);
		Q.add(u);

		int i = 0;
		while (!Q.isEmpty()) {
			Vertex v = Q.remove();
			for (Vertex w : Graphs.neighborListOf(g, v)) {
				if (!visited[w.getIndex()]) {
					visited[w.getIndex()] = true;
					dummy.addVertex(w);
					Vertex d = dummyVertex(n + i++);
					dummy.addVertex(d);
					dummy.addEdge(v, d);
					dummy.addEdge(w, d);
					D.add(d);
					Q.add(w);
				}
			}
		}
		return dummy;
	}

	private Vertex dummyVertex(int i) {
		Vertex d = new Vertex("" + i, i);
		d.setWeight(Integer.MAX_VALUE);
		d.setThreshold(1);
		return d;
	}
}
