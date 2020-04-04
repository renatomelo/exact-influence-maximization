package activable_network;
import java.awt.Dimension;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgrapht.Graph;
//import org.jgrapht.VertexFactory;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.generate.ScaleFreeGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;

public class TestViewer extends JApplet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Dimension DEFAULT_SIZE = new Dimension(830, 620);
	
	private JGraphXAdapter<Vertex, DefaultEdge> adapter;

	public static void main(String[] args) {
		TestViewer applet = new TestViewer();
		applet.init();
		
		JFrame frame = new JFrame("JGraphT Adapter to JGraphX");
		frame.getContentPane().add(applet);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void init() {
		int size = 10;
		Graph<Vertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
		ScaleFreeGraphGenerator<Vertex, DefaultEdge> generator = new ScaleFreeGraphGenerator<>(size );
		
		/*VertexFactory<Vertex> vFactory = new VertexFactory<Vertex>() {
			int index = 0;
			@Override
			public Vertex createVertex() {
				
				return new Vertex("v" + index++);
			}
		};*/
		
		generator.generateGraph(g, null);
		
		adapter = new JGraphXAdapter<>(g);
		
		getContentPane().add(new mxGraphComponent(adapter));
		resize(DEFAULT_SIZE);
		
		mxCompactTreeLayout l = new mxCompactTreeLayout(adapter);
				
//		mxCircleLayout l = new mxCircleLayout(adapter);
		l.execute(adapter.getDefaultParent());
	}
}
