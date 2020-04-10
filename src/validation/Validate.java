package validation;

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
import gurobi.GRBModel;
import models.MinTargetSet;

public class Validate {
	public static void main(String[] args) {
		GraphViewer<Vertex, DefaultEdge> viewer;

		int size = 100;
		Graph<Vertex, DefaultEdge> g; //= new DefaultDirectedGraph<>(DefaultEdge.class);
//		Graph<Vertex, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
		
		g = new GraphGen().scaleFree(size);
//		new GraphGen().gnp(g, size);
//		g = new GraphGen().tree(g, size);
//		Graph<Vertex, DefaultEdge> g = new GraphGen().wtss_instance2();

		MinTargetSet tss = new MinTargetSet(g);
//		MaxActiveSet tss = new MaxActiveSet(g);
//		WTSS tss = new WTSS(g);
		Set<Vertex> tSet = null;

		// Formulate a Integer Linear Program using the random graph as instance
		try {
			GRBEnv env;
			GRBModel model;

			env = new GRBEnv();
			model = tss.model(env);
			model.optimize();

			model.dispose();
			env.dispose();

//			env = new GRBEnv();
//			model = tss.model2(env);
//			model.optimize();

			model.dispose();
			env.dispose();
			
			env = new GRBEnv();
			model = tss.model3(env);
			model.optimize();
			
//			tSet = tss.getTargetSet();
			
			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

//		 for (Vertex v : tSet)
//			 v.setActive(true);
//
//		 viewer = new GraphViewer<>(g);
//		 viewer.initComponents();
	}
}
