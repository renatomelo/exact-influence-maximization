package branch_and_bound;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import activable_network.GraphGen;
import activable_network.Vertex;
import models.TSP;

public class TSPBnB {
	double mintour;
	Graph<Vertex, DefaultEdge> g;
	Vector<Vertex> bestList;
	boolean[] mark; //two arrays to keep track of min outgoing edge from
	double[] minedge; //each vertex -- used by method bound()
	PriorityQueue<TSPNode> PQ; 
	
	class TSPNode implements Comparable<TSPNode>{
		int level;
		double lenght, bound;
		Vector<Vertex> path;
		Vertex lastvertex;
		
		protected TSPNode(Vertex v) {
			level = 0;
			lenght = 0;
			bound = 0;
			lastvertex = v;
			path = new Vector<>();
		}
		
		protected void copyList(Vector<Vertex> vec) {
			if (vec == null || vec.isEmpty())
				path = new Vector<Vertex>();
			else
				path = new Vector<Vertex>(vec);
		}
		
		protected void add(Vertex v) {
			// post condition (v is added to the end of the partial tour)
			path.add(v);
		}
		
		@Override
		public int compareTo(TSPNode node) {
			//the bound of the two nodes are compared
			return (int) (node.bound - this.bound);
		}
	}
	
	public TSPBnB(Graph<Vertex, DefaultEdge> graph) {
		this.g = graph;
		mintour = Double.POSITIVE_INFINITY;
		PQ = new PriorityQueue<>();
		mark = new boolean[g.vertexSet().size()];
		minedge = new double[g.vertexSet().size()];
	}
	
	/**
	 * We first compute the minimum possible tour by finding the minimum edge exiting
	 * each vertex. The sum of these edges may not form a possible tour, but since
	 * every vertex must be visited once and only once, every vertex must be exited
	 * once. Therefore, no tour can be shorter than the sum of these minimum edges. 
	 */
	private void tsp() {
		while (!PQ.isEmpty()) {
			//remove node with smallest bound from the queue
			TSPNode tmp = PQ.poll();
			if (tmp.bound < mintour) {
				for (DefaultEdge e : g.outgoingEdgesOf(tmp.lastvertex)) {
					Vertex w = g.getEdgeTarget(e);
					if(!tmp.path.contains(w)) {
						//if vertex w is not already in the partial tour, form a new
						//partial tour that extends the tour in node tmp by appending w
						TSPNode u = new TSPNode(w);
						u.level = tmp.level + 1;
						u.lenght = tmp.lenght + lenght(tmp.lastvertex, w);
						u.copyList(tmp.path);
						u.add(w);
						
						if(u.level == g.vertexSet().size() - 2) {
							//if the new partial tour is of lenght n - 1, there is only
							//one possible complete tour that can be formed - form it now
							for (DefaultEdge e2 : g.outgoingEdgesOf(w)) {
								Vertex x = g.getEdgeTarget(e2);
								if(!u.path.contains(x)) {
									u.add(x);
									u.lenght += lenght(w, x);
									u.add(u.path.get(0));
									u.lenght += lenght(x, u.path.get(0));
									if (u.lenght < mintour) {
										//if this new complete tour is the best so far,
										// save it
										mintour = u.lenght;
										bestList = new Vector<Vertex>(u.path);
									}
									break;
								}
							}
						} else {
							//if the partial tour is "promising" add node to the 
							//priority queue
							u.bound = bound(u);
							if (u.bound < mintour)
								PQ.add(u);
						}
					}
					
				}
			}
		}
	}

	/**
	 * At each subsequent node, the lower bound for a "tour in progress" is the length
	 *  of the tour to that point plus the sum of the minimum edge exiting the end 
	 *  vertex of the partial tour and each of the minimum edges leaving all of the
	 *  remaining unvisited vertices. If this bound is less than the current minimum 
	 *  tour, the node is "promising" and the node is added to the queue.
	 * @param u
	 * @return the bound
	 */
	private double bound(TSPNode u) {
		//keep an array of vertices with the minimum outgoing distance for each vertex
		for(int i = 0; i < g.vertexSet().size(); i++) 
			mark[i] = false;
		//mark all of the vertices in the partial tour
		for (Vertex v : u.path) 
			mark[v.getIndex()] = true;

		//unmark the last vertex in the path
		Vertex last = u.lastvertex;
		mark[last.getIndex()] = false;
		
		double bnd = u.lenght;
		for (int i = 0; i < g.vertexSet().size(); i++)
			if(!mark[i])
				bnd += minedge[i];
		return bnd;
	}

	private double lenght(Vertex v, Vertex w) {
		DefaultEdge e = g.getEdge(v, w);
		
		return g.getEdgeWeight(e);
	}
	
	private void initialMinTour() {
		//find and record the minimum outgoing edge from each vertex
		for (int i = 0; i < g.vertexSet().size(); i++) 
			mark[i] = false;
		for (Vertex v : g.vertexSet()) {
			double cost = Double.POSITIVE_INFINITY;
			for (DefaultEdge e : g.outgoingEdgesOf(v)) {
				double len = g.getEdgeWeight(e);
				if(len < cost)
					cost = len;
			}
			minedge[v.getIndex()] = cost;
		}
	}
	
	public void solve () {
		initialMinTour();
		
		Vertex arbitrary = g.vertexSet().iterator().next();
		TSPNode root = new TSPNode(arbitrary);
		root.add(arbitrary);
		PQ.add(root);
		
		tsp();
		
		System.out.println("The TSP path is: ");
		for (Vertex v : bestList) 
			System.out.println(" "+ (v.getIndex() + 1));
		System.out.println();
		System.out.println("The minimum path length is "+ mintour);
	}
	
	public static void main(String[] args) {
		Graph<Vertex, DefaultEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
		//Create a weighted K5
		new GraphGen().complete(graph, 5);
		
		int weights[][] =  {{0, 14, 4, 10, 20},
							{14, 0, 7, 8, 7},
							{4, 5, 0, 7, 16},
							{11, 7, 9, 0, 2},
							{18, 7, 17, 4, 0}};
		//setting the weights 
		for (DefaultEdge e : graph.edgeSet()) {
			Vertex v = graph.getEdgeSource(e);
			Vertex w = graph.getEdgeTarget(e);
			graph.setEdgeWeight(e, weights[v.getIndex()][w.getIndex()]);
		}
		
		TSPBnB bnb = new TSPBnB(graph);
		bnb.solve();
	}
}
