package activable_network;

import org.jgrapht.Graph;
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
	
	public void wattsStrogatz(Graph<Vertex, DefaultEdge> g, int size) {
		WattsStrogatzGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new WattsStrogatzGraphGenerator<>(size, size/5, .35);

		generator.generateGraph(g, vFactory, null);
	}
	
	public void gnp(Graph<Vertex, DefaultEdge> g, int size) {
		GnpRandomGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new GnpRandomGraphGenerator<>(size, .35);

		generator.generateGraph(g, vFactory, null);
	}
	
	public void barabasiAlbert(Graph<Vertex, DefaultEdge> g, int size) {
		BarabasiAlbertGraphGenerator<Vertex, DefaultEdge> generator;
		generator = new BarabasiAlbertGraphGenerator<>(size/2,3,size);

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
	public static void main(String[] args) {
		GraphGen gen = new GraphGen();
		Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		gen.star2(g, 10);
		


		GraphViewer<Vertex, DefaultEdge> viewer = new GraphViewer<>(g);
		
		viewer.initComponents();
	}
}
