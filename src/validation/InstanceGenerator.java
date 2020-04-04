package validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

//import org.jgraph.graph.Edge;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

import activable_network.GraphGen;
import activable_network.GraphViewer;
import activable_network.Vertex;
import models.MinTargetSet;

public class InstanceGenerator {
	
	/**
	 * Following the computational experiments of Fischetti et al. 2018.
	 * @param g
	 * @return a list of incentives for each vertex
	 */
	static double[] incentives(Graph<Vertex, DefaultEdge> g) {
		double listOfIncentives[] = new double[4];
		double maxThreshold = 0;
		for (Vertex v : g.vertexSet()) {
			if (v.getThreshold() > maxThreshold)
				maxThreshold = v.getThreshold();
		}			

		listOfIncentives[0] = (double) (.25) * maxThreshold;
		listOfIncentives[1] = (double) (.5) * maxThreshold;
		listOfIncentives[2] = (double) (.75) * maxThreshold;
		listOfIncentives[3] = maxThreshold;
		
		return listOfIncentives;
	}
	
	/**
	 * positions vertices equally spaced on a circle
	 * @param g 
	 * @param height
	 * @param width
	 * @return
	 */
	static Map<Vertex, double[]> cycleLayout(Graph<Vertex, DefaultEdge> g, double height, double width) 
	{
		Map<Vertex, double[]> vCoord = new HashMap<>();
		double radius = 0.45 * (height < width ? height : width);
	
		int i = 0;
		for (Vertex v : g.vertexSet())
		{
			double[] coord = new double[2];
	
			double angle = (2 * Math.PI * i) / g.vertexSet().size();
	
			coord[0] = Math.cos(angle) * radius + width / 2;
			coord[1] = Math.sin(angle) * radius + height / 2;
			
			vCoord.put(v, coord);
			
			i++;
		}
		return vCoord;
	}
	
	Map<DefaultEdge, Integer> reandomWeights(Graph<Vertex, DefaultEdge> g) {
		Random rand = new Random();
		Map<DefaultEdge, Integer> weights = new HashMap<>();
		for (DefaultEdge e : g.edgeSet()) {
			Vertex u = g.getEdgeSource(e);
			Vertex v = g.getEdgeTarget(e);
			
			int weight = 0;
			if (g.inDegreeOf(v) > 1)
				weight = rand.nextInt(g.inDegreeOf(v) - 1) + 1;
			else
				weight = 1;
			
			weights.put(e, weight);
		}
		return weights ;
	}
	
	static void print(Graph<Vertex, DefaultEdge> g, Map<DefaultEdge, Integer> weights) {
		System.out.println("nnodes narcs type");
		System.out.println(g.vertexSet().size() + " " + g.edgeSet().size() + " digraph");
		
		// get the coordinates to put the vertices in a cycle
		Map<Vertex, double[]> cycle = cycleLayout(g, 1024, 1024);
		
		System.out.println("nodename posx posy threshold incentives");
		for (Vertex v : g.vertexSet()) {
			double[] coord = cycle.get(v);
			double x = coord[0];
			double y = coord[1];
			System.out.print( (v.getIndex() + 1) + " " + x 
								+ " " + y + " " + (int)v.getThreshold() + " " );
			
			double incentives[] = incentives(g);
			int i;
			for(i = 0; i < incentives.length - 1; i++) {
				System.out.print( incentives[i] + ",");
			}			
			System.out.println("" + incentives[i]);
		}
		
		System.out.println("tail head influence");
		for (DefaultEdge e : g.edgeSet()) {
			Vertex u = g.getEdgeSource(e);
			Vertex v = g.getEdgeTarget(e);
			
			System.out.println( (u.getIndex() + 1) + " " + (v.getIndex() + 1) + " " + weights.get(e) );
			
		}
	}
	
//	void print(Graph<Vertex, DefaultEdge> g) {
//		System.out.println("nnodes narcs type");
//		System.out.println(g.vertexSet().size() + " " + g.edgeSet().size() + " digraph");
//		
//		// get the coordinates to put the vertices in a cycle
//		Map<Vertex, double[]> cycle = cycleLayout(g, 1024, 1024);
//		
//		System.out.println("nodename threshold");
//		for (Vertex v : g.vertexSet()) {
//			System.out.println( (v.getIndex() + 1) + " " + (int)v.getThreshold() );
//		}
//		
//		System.out.println("tail head influence");
//		for (DefaultEdge e : g.edgeSet()) {
//			Vertex u = g.getEdgeSource(e);
//			Vertex v = g.getEdgeTarget(e);
//			
//			int weight = 1;
//				
//			System.out.println( (u.getIndex() + 1) + " " + (v.getIndex() + 1) + " " + weight );
//			
//		}
//	}
	
	//method to convert the instances of fischetti's paper into 'mylibgraph' format
	static void readGraph(Graph<Vertex, DefaultEdge> g, Map<DefaultEdge, Integer> weights) {
		//File file = new File("/home/renato/workspace/math_programming/data/socnet-instances/SW-n50-k4-b0.1-d1-10-g0.7-i1");
		Scanner scan = null;
		int n = 0, m = 0, k = 0, dmin = 0, dmax = 0;
		double beta = 0, gamma = 0, inr = 0;
//		try {
			scan = new Scanner(System.in);
			scan.useLocale(Locale.ENGLISH);
			
			//skips line that starts with #
			if (scan.next().equals("#"))
				scan.nextLine();
			//first line read the small word's parameters
			n = scan.nextInt();
			k = scan.nextInt();
			beta = scan.nextDouble();
			dmin = scan.nextInt();
			dmax = scan.nextInt();
			gamma = scan.nextDouble();
			inr = scan.nextInt();
			
			scan.nextLine(); // skip line with #
			scan.nextLine();
			n = scan.nextInt();
			m = scan.nextInt();
			scan.nextLine(); // skip line with #
			scan.nextLine();
			
			Vertex vertices[] = new Vertex[n];
			for (int i = 0; i < n; i++) {
				int index = scan.nextInt();
				int thr = scan.nextInt();
				Vertex v = new Vertex(Integer.toString(index), index);
				v.setThreshold(thr);
				vertices[index] = v;
				g.addVertex(v);
				scan.nextLine();
			}
			
			//weights = new HashMap<>();
			scan.nextLine(); // skips line with #
			for (int i = 0; i < m; i++) {
				scan.next(); //skip index
				int source = scan.nextInt();
				int target = scan.nextInt();
				int weight = scan.nextInt();
				DefaultEdge e = g.addEdge(vertices[source], vertices[target]);
				
				weights.put(e, weight);
				//System.out.println(source + " " + target + " " + weight);
			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
	}
	

	public static void main(String[] args) {
		
		Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
		//GraphGen gen = new GraphGen();
		//gen.scaleFree(g, 1000);
		//gen.wattsStrogatz(g, 30, 8, .3);
		
//		int thresholds[] = new MinTargetSet(g).majorityThreshold(g.vertexSet());
		
		//save the weights on edges
		Map<DefaultEdge, Integer> weights = new HashMap<>();
		readGraph(g, weights);
		print(g, weights);
		
//		int thresholds[] = new MinTargetSet(g).randomThreshold(g.vertexSet());
//		
//		for (Vertex v : g.vertexSet()) {
//			v.setThreshold(thresholds[v.getIndex()]);
//		}
//		
//		new InstanceGenerator().print(g);
				
//		GraphViewer< Vertex, DefaultEdge> viewer = new GraphViewer<>(g);
//		viewer.initComponents();
	}
}
