package problems;

import java.text.DecimalFormat;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.mxgraph.analysis.mxGraphProperties.GraphType;

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
	
	public void medel1(MinTargetSet tss) throws GRBException {
		GRBEnv env;
		GRBModel model;
		env = new GRBEnv();
		model = tss.model(env);
		model.set(GRB.DoubleParam.TimeLimit, 10000.0);
		model.set(GRB.IntParam.LogToConsole, 0);
		model.optimize();

		double runningtime = model.get(GRB.DoubleAttr.Runtime);
		System.out.println("Objetive Value: "+model.get(GRB.DoubleAttr.ObjVal));
		System.out.printf("Running time: %f\n", runningtime);
		
		System.out.println("\n");
		model.dispose();
		env.dispose();
	}
	
	public void medel2(MinTargetSet tss) throws GRBException {
		GRBEnv env;
		GRBModel model;
		env = new GRBEnv();
		model = tss.model2(env);
		model.set(GRB.DoubleParam.TimeLimit, 10000.0);
		model.set(GRB.IntParam.LogToConsole, 0);
		model.optimize();

		double runningtime = model.get(GRB.DoubleAttr.Runtime);
		System.out.println("Objetive Value: "+model.get(GRB.DoubleAttr.ObjVal));
		System.out.printf("Running time: %f \n", runningtime);
		System.out.println("\n");
//		tSet = tss.getTargetSet();	
		model.dispose();
		env.dispose();
	}
	
	public void medel3(MinTargetSet tss) throws GRBException {
		double runningtime;
		GRBEnv env;
		GRBModel model;
		env = new GRBEnv();
		model = tss.model3(env);
		model.set(GRB.IntParam.Presolve, 0);
		model.set(GRB.DoubleParam.TimeLimit, 1000.0);
//		model.set(GRB.IntParam.LogToConsole, 0);
		model.optimize();
		
		runningtime = model.get(GRB.DoubleAttr.Runtime);
//		DecimalFormat df = new DecimalFormat("#0.000000000000000000000");
//		System.out.println("\n\nFormato decimal: "+df.format(runningtime));
//		System.out.println("\n");
		
		//print the optimal objective
		System.out.println("Objetive Value: "+model.get(GRB.DoubleAttr.ObjVal));
		System.out.printf("Running time: %f\n", runningtime);
		System.out.println("\n");
//		tSet = tss.getTargetSet();	
		//print the solution variables
		//print the running time
		model.dispose();
		env.dispose();
	}
	
	public void medel4(MinTargetSet tss) throws GRBException {
		double runningtime;
		GRBEnv env;
		GRBModel model;
		env = new GRBEnv();
		model = tss.model4(env);
		model.set(GRB.IntParam.Presolve, 0);
		model.set(GRB.DoubleParam.TimeLimit, 1000.0);
//		model.set(GRB.IntParam.LogToConsole, 0);
		model.optimize();
		
		runningtime = model.get(GRB.DoubleAttr.Runtime);
		//print the optimal objective
		System.out.println("Objetive Value: "+model.get(GRB.DoubleAttr.ObjVal));
		System.out.printf("Running time: %f\n", runningtime);
		System.out.println("\n");
//		tSet = tss.getTargetSet();	
		model.dispose();
		env.dispose();
	}
	
	public static void main(String[] args) {
		TestFormulations teste = new TestFormulations();
		GraphViewer<Vertex, DefaultEdge> viewer;

		int size = 120000;
		Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
//		Graph<Vertex, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
		
		new GraphGen().scaleFree(g, size);
//		new GraphGen().gnp(g, size);
//		g = new GraphGen().tree(g, size);
//		Graph<Vertex, DefaultEdge> g = new GraphGen().wtss_instance2();

		MinTargetSet tss = new MinTargetSet(g);
//		MaxActiveSet tss = new MaxActiveSet(g);
//		WTSS tss = new WTSS(g);


		// Formulate a Integer Linear Program using the random graph as instance
		try {
			
//			teste.medel1(tss);
//			teste.medel2(tss);
			teste.medel3(tss);
//			teste.medel4(tss);
			
//			tSet = tss.getTargetSet();			
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

//		 for (Vertex v : tSet)
//			 v.setActive(true);
//		 viewer = new GraphViewer<>(g);
//		 viewer.initComponents();
	}
}
