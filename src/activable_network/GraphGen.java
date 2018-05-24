package activable_network;

import java.util.LinkedList;
import java.util.Queue;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.KleinbergSmallWorldGraphGenerator;
import org.jgrapht.generate.ScaleFreeGraphGenerator;
import org.jgrapht.generate.StarGraphGenerator;
import org.jgrapht.generate.WattsStrogatzGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

public class GraphGen {

	VertexFactory<Vertex> vFactory = new VertexFactory<Vertex>() {
		int index = 0;

		public Vertex createVertex() {
			Vertex v = new Vertex("v" + index, index);
			index++;
			return v;
		}
	};

	public void scaleFree(Graph<Vertex, DefaultEdge> g, int size) {
		ScaleFreeGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new ScaleFreeGraphGenerator<>(size);

		generator.generateGraph(g, vFactory, null);
	}

	/**
	 * Not working for now
	 * 
	 * @param g
	 * @param size
	 */
	public void wattsStrogatz(Graph<Vertex, DefaultEdge> g, int size) {
		WattsStrogatzGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new WattsStrogatzGraphGenerator<>(size, size / 5, .35);

		generator.generateGraph(g, vFactory, null);
	}

	public void gnp(Graph<Vertex, DefaultEdge> g, int size) {
		GnpRandomGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new GnpRandomGraphGenerator<>(size, .35);

		generator.generateGraph(g, vFactory, null);
	}

	public void barabasiAlbert(Graph<Vertex, DefaultEdge> g, int size) {
		BarabasiAlbertGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new BarabasiAlbertGraphGenerator<>(size / 2, 3, size);

		generator.generateGraph(g, vFactory, null);
	}

	public void kleibergSmallWorld(Graph<Vertex, DefaultEdge> g, int size) {
		KleinbergSmallWorldGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new KleinbergSmallWorldGraphGenerator<>(size, 1, 1, 2);

		generator.generateGraph(g, vFactory, null);
	}

	public void complete(Graph<Vertex, DefaultEdge> g, int size) {
		CompleteGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new CompleteGraphGenerator<>(size);

		generator.generateGraph(g, vFactory, null);
	}

	public void star(Graph<Vertex, DefaultEdge> g, int size) {
		StarGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new StarGraphGenerator<>(size);

		generator.generateGraph(g, vFactory, null);
	}

	public void bipartite(Graph<Vertex, DefaultEdge> g, int size) {
		StarGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new StarGraphGenerator<>(size);

		generator.generateGraph(g, vFactory, null);
	}

	public Graph<Vertex, DefaultEdge> star2(Graph<Vertex, DefaultEdge> g, int size) {
		CompleteGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new CompleteGraphGenerator<>(size);

		generator.generateGraph(g, vFactory, null);

		KruskalMinimumSpanningTree<Vertex, DefaultEdge> mst = new KruskalMinimumSpanningTree<>(g);
		SpanningTree<DefaultEdge> st = mst.getSpanningTree();

		Graph<Vertex, DefaultEdge> h = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (Vertex v : g.vertexSet())
			h.addVertex(v);

		for (DefaultEdge e : st)
			h.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e));
		return h;
	}

	public Graph<Vertex, DefaultEdge> tree(Graph<Vertex, DefaultEdge> g, int size) {
		ScaleFreeGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new ScaleFreeGraphGenerator<>(size);

		generator.generateGraph(g, vFactory, null);

		KruskalMinimumSpanningTree<Vertex, DefaultEdge> mst = new KruskalMinimumSpanningTree<>(g);
		SpanningTree<DefaultEdge> st = mst.getSpanningTree();

		Graph<Vertex, DefaultEdge> h = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (Vertex v : g.vertexSet())
			h.addVertex(v);

		for (DefaultEdge e : st)
			h.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e));
		return h;
	}

	public Graph<Vertex, DefaultEdge> wtss_instance() {

		Graph<Vertex, DefaultEdge> h = new SimpleGraph<>(DefaultEdge.class);

		Vertex v1 = new Vertex("" + 1, 0);
		Vertex v2 = new Vertex("" + 2, 1);
		Vertex v3 = new Vertex("" + 3, 2);
		Vertex v4 = new Vertex("" + 4, 3);
		Vertex v5 = new Vertex("" + 5, 4);
		Vertex v6 = new Vertex("" + 6, 5);

		h.addVertex(v1);
		h.addVertex(v2);
		h.addVertex(v3);
		h.addVertex(v4);
		h.addVertex(v5);
		h.addVertex(v6);

		h.addEdge(v1, v2);
		h.addEdge(v1, v3);
		h.addEdge(v1, v4);
		h.addEdge(v2, v5);
		h.addEdge(v2, v6);

		v1.setThreshold(2);
		v2.setThreshold(2);
		v3.setThreshold(1);
		v4.setThreshold(1);
		v5.setThreshold(1);
		v6.setThreshold(1);

		v1.setWeight(1);
		v2.setWeight(1);
		v3.setWeight(10);
		v4.setWeight(10);
		v5.setWeight(10);
		v6.setWeight(10);

		return h;
	}
	
	public Graph<Vertex, DefaultEdge> wtss_instance2() {

		Graph<Vertex, DefaultEdge> h = new SimpleGraph<>(DefaultEdge.class);

		Vertex v1 = new Vertex("" + 1, 0);
		Vertex v2 = new Vertex("" + 2, 1);
		Vertex v3 = new Vertex("" + 3, 2);
		Vertex v4 = new Vertex("" + 4, 3);
		Vertex v5 = new Vertex("" + 5, 4);
		Vertex v6 = new Vertex("" + 6, 5);
		Vertex v7 = new Vertex("" + 7, 6);
		Vertex v8 = new Vertex("" + 8, 7);
		Vertex v9 = new Vertex("" + 9, 8);
		Vertex v10 = new Vertex("" + 10, 9);
		Vertex v11 = new Vertex("" + 11, 10);

		h.addVertex(v1);
		h.addVertex(v2);
		h.addVertex(v3);
		h.addVertex(v4);
		h.addVertex(v5);
		h.addVertex(v6);
		h.addVertex(v7);
		h.addVertex(v8);
		h.addVertex(v9);
		h.addVertex(v10);
		h.addVertex(v11);

		h.addEdge(v1, v4);
		h.addEdge(v1, v5);
		h.addEdge(v1, v6);
		h.addEdge(v2, v4);
		h.addEdge(v2, v7);
		h.addEdge(v2, v8);
		h.addEdge(v2, v9);
		h.addEdge(v3, v4);
		h.addEdge(v3, v10);
		h.addEdge(v3, v11);

		v1.setThreshold(2);
		v2.setThreshold(3);
		v3.setThreshold(2);
		v4.setThreshold(2);
		v5.setThreshold(1);
		v6.setThreshold(1);
		v7.setThreshold(1);
		v8.setThreshold(1);
		v9.setThreshold(1);
		v10.setThreshold(1);
		v11.setThreshold(1);

		v1.setWeight(15);
		v2.setWeight(4);
		v3.setWeight(30);
		v4.setWeight(20);
		v5.setWeight(5);
		v6.setWeight(6);
		v7.setWeight(7);
		v8.setWeight(8);
		v9.setWeight(9);
		v10.setWeight(10);
		v11.setWeight(11);

		return h;
	}

	

	public static void main(String[] args) {
		GraphGen gen = new GraphGen();
		// Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
		Graph<Vertex, DefaultEdge> g = gen.wtss_instance2();
		GraphViewer<Vertex, DefaultEdge> viewer = new GraphViewer<>(g);

		viewer.initComponents();
	}
}
