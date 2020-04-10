package problems;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.base.Stopwatch;

import activable_network.GraphGen;
import activable_network.GraphViewer;
import activable_network.Vertex;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import models.MinTargetSet;
import models.WTSS;

public class TestFormulations {
	static Set<Vertex> tSet = null;

	public double medel1(MinTargetSet tss) throws GRBException {
		GRBEnv env;
		GRBModel model;
		env = new GRBEnv();
		model = tss.model(env);
		// model.set(GRB.DoubleParam.TimeLimit, 1000.0);
		model.set(GRB.IntParam.LogToConsole, 0);
		//model.set(GRB.DoubleParam.TimeLimit, 1800);
		model.optimize();

		double runningtime = model.get(GRB.DoubleAttr.Runtime);
		System.out.println("Objetive: " + model.get(GRB.DoubleAttr.ObjVal) + 
						   ", Gap: " + model.get(GRB.DoubleAttr.MIPGap));
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
		
		//do the preprocessing before solve the model
		Set<Vertex> target = new HashSet<>();
		int [] thr = new int[tss.g.vertexSet().size()];
		
		//double start = System.nanoTime();
		Stopwatch time = Stopwatch.createStarted();
		Graph<Vertex, DefaultEdge> h = tss.preprocessing(tss.g, thr, target);
		//runningtime = System.nanoTime() - start;
		time.stop();
		runningtime = time.elapsed(TimeUnit.SECONDS);
		
		if (h.vertexSet().isEmpty()) {
			System.out.println("Optimum found in preprocessing: "+target.size());
//			for (int i = 0; i < thr.length; i++) {
//				System.out.println("thr = " + thr[i]);
//			}
			//System.exit(0);
		}else
		{
			System.out.println("Optimum NOT found in preprocessing: " + target.size());
			System.out.println("Number of nodes = " + h.vertexSet().size());
			env = new GRBEnv();
			tss.g = h; //passing the processed graph to the model
			model = tss.model2(env, thr);
			// model.set(GRB.DoubleParam.TimeLimit, 1000.0);
			model.set(GRB.IntParam.LogToConsole, 0);
			//model.set(GRB.DoubleParam.TimeLimit, 1800);
			model.optimize();

			runningtime += model.get(GRB.DoubleAttr.Runtime);
			System.out.println("Objetive: " + model.get(GRB.DoubleAttr.ObjVal) + 
					   		   ", Gap: " + model.get(GRB.DoubleAttr.MIPGap));
			// System.out.printf("Running time: %f \n", runningtime);
			// System.out.println("\n");
			// tSet = tss.getTargetSet();
			model.dispose();
			env.dispose();
		}
		
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
		// DecimalFormat df = new DecimalFormat("#0.000000000000000000000");
		// System.out.println("\n\nFormato decimal: "+df.format(runningtime));
		// System.out.println("\n");

		// print the optimal objective
		System.out.println("Objetive Value: " + model.get(GRB.DoubleAttr.ObjVal));
		// System.out.printf("Running time: %f\n", runningtime);
		// System.out.println("\n");
		// tSet = tss.getTargetSet();
		// print the solution variables
		// print the running time
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
		// System.out.printf("Running time: %f\n", runningtime);
		// System.out.println("\n");
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
		for (int i = 1; i <= 10; i++) {
			double t1 = 0, t2 = 0, t3 = 0, t4 = 0;
			int size = 0;

			for (int j = 0; j < n; j++) {
				size = 2 * i * 10;
				//Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
				//Graph<Vertex, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

				Graph<Vertex, DefaultEdge> g = new GraphGen().directedScaleFree(size);

				MinTargetSet tss = new MinTargetSet(g);
				// MaxActiveSet tss = new MaxActiveSet(g);
				// WTSS tss = new WTSS(g);
				t1 = t1 + medel1(tss);
				
				//MinTargetSet tss2 = new MinTargetSet(gCopy);
				t2 = t2 + medel2(tss);
				//t3 = t3 + medel3(tss);
				//t4 = t4 + model4(tss);
			}
			//System.out.printf("\n%d \t\t%f \t\t %f \t\t %f \t\t %f\n", size, t1 / i, t2 / i, t3 / i, t4 / i);
			System.out.printf("\n%d \t\t %f \t\t %f\n", size, (t1 / n), (t2 / n));
			//System.out.printf("\n%d \t\t %f\n", size, (t2 / n));
			System.out.println();
		}
	}

	public static void main(String[] args) {
		TestFormulations teste = new TestFormulations();
		GraphViewer<Vertex, DefaultEdge> viewer;
		Graph<Vertex, DefaultEdge> solution = null;
		// Formulate a Integer Linear Program using the random graph as instance
		try {
			teste.simular();

			/*Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
			// Graph<Vertex, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
			new GraphGen().scaleFree(g, 20);
			MinTargetSet tss = new MinTargetSet(g);
			// WTSS tss = new WTSS(g);

			GRBEnv env;
			GRBModel model;
			env = new GRBEnv();
			model = tss.model2(env);
			// model.set(GRB.IntParam.Presolve, 0);
			// model.set(GRB.IntParam.LogToConsole, 0);
			model.optimize();

			System.out.println("\nObjetive Value: " + model.get(GRB.DoubleAttr.ObjVal));

			tSet = tss.getTargetSet();
			//solution = tss.getSolutionGraph();
			// TODO a method to simulate the spread for a given target set in order to
			// validate the solution
			model.dispose();
			env.dispose();

			for (Vertex v : tSet)
				 v.setActive(true);
			viewer = new GraphViewer<>(g);
			viewer.initComponents();*/

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
}
