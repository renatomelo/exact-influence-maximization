package problems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import com.google.common.base.Stopwatch;
import activable_network.GraphGen;
import activable_network.GraphViewer;
import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import models.MinTargetSet;
import readgraph.GraphReader;

public class TestFormulations {
	static Set<Vertex> tSet = null;
	static int count = 0, count2 = 0;

	public double medel1(MinTargetSet tss) throws GRBException {
		GRBEnv env;
		GRBModel model;
		env = new GRBEnv();
		model = tss.model(env);
		model.set(GRB.IntParam.LogToConsole, 0);
		// model.set(GRB.DoubleParam.TimeLimit, 1800);
		model.optimize();

		double runningtime = model.get(GRB.DoubleAttr.Runtime);
		System.out.println(
				"Objetive: " + model.get(GRB.DoubleAttr.ObjVal) + ", Gap: " + model.get(GRB.DoubleAttr.MIPGap));
		// System.out.printf("Running time: %f\n", runningtime);

		// System.out.println("\n");
		model.dispose();
		env.dispose();

		return runningtime;
	}

	public double medel2(MinTargetSet tss) throws GRBException {
		GRBEnv env;
		GRBModel model;
		double runningtime;

		// do the preprocessing before solve the model
		Set<Vertex> target = new HashSet<>();
		int[] thr = new int[tss.g.vertexSet().size()];

		// double start = System.nanoTime();
		Stopwatch time = Stopwatch.createStarted();
		Graph<Vertex, DefaultEdge> h = tss.preprocessing(tss.g, thr, target);
		time.stop();

		// runningtime = time.elapsed(TimeUnit.SECONDS);
		runningtime = (double) time.elapsed(TimeUnit.MILLISECONDS) / 1000;

		count++;
		if (!h.vertexSet().isEmpty()) {
			count2++;
			// System.out.println("Optimum NOT found in preprocessing: " + target.size());
			System.out.println("Vertices of reduced graph = " + h.vertexSet().size());
			System.out.println("Edges of reduced graph = " + h.edgeSet().size());

			//System.exit(0);

			env = new GRBEnv();

			Map<Vertex, Vertex> nodeRef = new HashMap<>();
			Map<DefaultEdge, DefaultEdge> arcRef = new HashMap<>();
			Graph<Vertex, DefaultEdge> copy = tss.graphCopy(h, nodeRef, arcRef);

			// save the thresholds of the new copy graph
			int[] thrCopy = new int[copy.vertexSet().size()];

			for (Vertex v : copy.vertexSet()) {
				thrCopy[v.getIndex()] = thr[nodeRef.get(v).getIndex()];
			}

			tss.g = copy; // passing the processed graph to the model

			model = tss.model2(env, thrCopy);
			model.set(GRB.IntParam.LogToConsole, 0);
			// model.set(GRB.DoubleParam.TimeLimit, 1800);
			model.optimize();

			runningtime += model.get(GRB.DoubleAttr.Runtime);

			// merge the solution with the preprocessing
			tSet = tss.getTargetSet();
			for (Vertex v : tSet) {
				target.add(nodeRef.get(v));
			}
			model.dispose();
			env.dispose();
		}

		System.out.println("Objetive: " + target.size() + ", count = " + count + ", count2 = " + count2);

		return runningtime;
	}

	public double medel3(MinTargetSet tss) throws GRBException {
		double runningtime;
		GRBEnv env;
		GRBModel model;
		env = new GRBEnv();
		model = tss.model3(env);
		// model.set(GRB.IntParam.Presolve, 0);
		// model.set(GRB.DoubleParam.TimeLimit, 100.0);
		model.set(GRB.IntParam.LogToConsole, 0);
		model.optimize();

		runningtime = model.get(GRB.DoubleAttr.Runtime);

		// print the optimal objective
		System.out.println("Objetive Value: " + model.get(GRB.DoubleAttr.ObjVal));
		// tSet = tss.getTargetSet();
		model.dispose();
		env.dispose();

		return runningtime;
	}

	public double model4(MinTargetSet tss) throws GRBException {
		double runningtime;
		GRBEnv env;
		GRBModel model;
		env = new GRBEnv();
		model = tss.model4(env);
		model.set(GRB.IntParam.Presolve, 0);
		// model.set(GRB.DoubleParam.TimeLimit, 100.0);
		model.set(GRB.IntParam.LogToConsole, 0);
		model.optimize();

		runningtime = model.get(GRB.DoubleAttr.Runtime);

		// print the optimal objective
		System.out.println("Objetive Value: " + model.get(GRB.DoubleAttr.ObjVal));
		// tSet = tss.getTargetSet();
		model.dispose();
		env.dispose();

		return runningtime;
	}

	/**
	 * Perfoms experiments with synthetic graphs in different ILP models and save
	 * the running time to compare
	 * 
	 * @throws GRBException
	 */
	void simular() throws GRBException {
		int n = 10; // fix in 10 or 30
		for (int i = 48; i <= 100; i++) {
			double t1 = 0, t2 = 0, t3 = 0, t4 = 0;
			int size = 0;

			for (int j = 0; j < n; j++) {
				size = i * 1000;
				// Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
				// Graph<Vertex, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

				Graph<Vertex, DefaultEdge> g = new GraphGen().directedScaleFree(size);

				MinTargetSet tss = new MinTargetSet(g);
				// MaxActiveSet tss = new MaxActiveSet(g);
				// WTSS tss = new WTSS(g);
				// t1 = t1 + medel1(tss);

				// MinTargetSet tss2 = new MinTargetSet(gCopy);
				t2 = t2 + medel2(tss);
				// t3 = t3 + medel3(tss);
				// t4 = t4 + model4(tss);
			}
			// System.out.printf("\n%d \t\t%f \t\t %f \t\t %f \t\t %f\n", size, t1 / i, t2 /
			// i, t3 / i, t4 / i);
			// System.out.printf("\n%d \t\t %f \t\t %.8f\n", size, (t1 / n), (t2 / n));
			System.out.printf("\n%d \t\t %f\n", size, (t2 / n));
			System.out.println();
		}
	}

	public static void main(String[] args) {
		TestFormulations teste = new TestFormulations();
		// GraphViewer<Vertex, DefaultEdge> viewer;
		// Graph<Vertex, DefaultEdge> solution = null;
		//
		// GraphReader reader = new GraphReader();
		// Graph<Vertex, DefaultEdge> g;

		// g = reader.readBtcAlpha();
		// g = reader.readBtcOTC();
		// g = reader.readWikivote();
		// g = reader.readDblp();
		// g = reader.readReddit();
		// g = reader.readEpinions();
		// g = reader.readSlashdot0902();
		// g = reader.readEmailEuALL();

		// System.out.println("|V(G)| = " + g.vertexSet().size());
		// System.out.println("|E(G)| = " + g.edgeSet().size());
		//
		// MinTargetSet tss = new MinTargetSet(g);
		// // System.out.println("Large Thresholds:");
		// // int[] thr = tss.largeThreshold(g.vertexSet());
		// // int[] thr2 = tss.smallThreshold(g.vertexSet());
		// // int[] thr3 = tss.majorityThreshold(g.vertexSet());
		// // for (Vertex v : g.vertexSet()) {
		// // System.out.println(thr[v.getIndex()]+" "+thr3[v.getIndex()]+"
		// // "+thr2[v.getIndex()]);
		// // }
		// // System.exit(0);
		//
		// try {
		// // if (GraphTests.isStronglyConnected(g)) {
		// // System.out.println("the graph is strongly connected");
		// // System.exit(0);
		// // } else {
		// // System.out.println("the graph is NOT strongly connected");
		// // System.exit(0);
		// // }
		// teste.medel2(tss);
		// } catch (GRBException e) {
		// e.printStackTrace();
		// }

		// Formulate a Integer Linear Program using the random graph as instance

		try {

			teste.simular();

			/*
			 * Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
			 * // Graph<Vertex, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class); new
			 * GraphGen().scaleFree(g, 20); MinTargetSet tss = new MinTargetSet(g); // WTSS
			 * tss = new WTSS(g);
			 * 
			 * GRBEnv env; GRBModel model; env = new GRBEnv(); model = tss.model2(env); //
			 * model.set(GRB.IntParam.Presolve, 0); // model.set(GRB.IntParam.LogToConsole,
			 * 0); model.optimize();
			 * 
			 * System.out.println("\nObjetive Value: " + model.get(GRB.DoubleAttr.ObjVal));
			 * 
			 * tSet = tss.getTargetSet(); //solution = tss.getSolutionGraph(); // TODO a
			 * method to simulate the spread for a given target set in order to // validate
			 * the solution model.dispose(); env.dispose();
			 * 
			 * for (Vertex v : tSet) v.setActive(true); viewer = new GraphViewer<>(g);
			 * viewer.initComponents();
			 */

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}
}
