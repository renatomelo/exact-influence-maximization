package activable_network;

/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * Modified and adapted to the math IT framework by Andreas de Vries (2013)
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 */

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.Animator;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
import interfaces.Activatable;
import interfaces.NetworkOfActivatables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * This class provides a visualization frame to show a specified graph. It bases
 * on the JUNG Java Universal Network/Graph Framework
 * (<a href="http://jung.sourceforge.net">http://jung.sourceforge.net</a>),
 * version 2.0.1 from 2010.
 * <p>
 * In particular, this class is an adaption of the class
 * <code>VisualizationViewer.java</code> in the package
 * <code>edu.uci.ics.jung.visualization</code>, written by Tom Nelson.
 * </p>
 *
 * @author Tom Nelson, Andreas de Vries
 * @version 1.0
 * @param <V>
 *            the type of the vertices
 * @param <E>
 *            the type of the edges
 */
@SuppressWarnings("serial")
public class GraphViewer<V, E> extends JFrame {

	/**
	 * the graph
	 */
	// protected org.mathIT.graphs.Graph<V> invokerGraph;
	protected Graph<V, E> invokerGraph;
	protected edu.uci.ics.jung.graph.Graph<V, E> graph;
	protected edu.uci.ics.jung.graph.Graph<V, E> collapsedGraph;
	/**
	 * the visual component and renderers for the graph
	 */
	protected Canvas<V, E> canvas;
	protected Layout layout;
	protected GraphCollapser collapser;
	protected ScalingControl scaler;

	// --- GUI components: ---
	Container content;
	private JPanel controls;
	private JPanel layoutChoice = new JPanel();
	private JComboBox jcb;
	// private JComboBox<Layout<V,E>> jcb;
	private javax.swing.JCheckBox edgeLabels;
	private javax.swing.JCheckBox thresholds;
	private JPanel edgePanel;
	private JPanel thresPanel;
	private GraphZoomScrollPane gzsp;
	private JPanel modePanel;
	private JPanel buttonPanel;

	/**
	 * Constructs a graph viewer from the specified graph
	 * 
	 * @param graph
	 *            a graph of the class {@link Graph}
	 */
	@SuppressWarnings("unchecked")
	public GraphViewer(Graph<V, E> graph) {
		this.invokerGraph = graph;
		// this.graph = new DirectedSparseGraph<>();
		this.graph = new UndirectedSparseGraph<>();

		for (V v : graph.vertexSet()) {
			this.graph.addVertex(v);
		}

		// Collect edges of this graph and add them to the JUNG graph:
		Set<E> edges = graph.edgeSet();
		for (E e : edges) {
			this.graph.addEdge((E) e, graph.getEdgeSource(e), graph.getEdgeTarget(e));
		}
	}

	/**
	 * Constructs a graph viewer from the specified graph
	 * 
	 * @param graph
	 *            a graph of the class <a href=
	 *            "http://jung.sourceforge.net/doc/api/edu/uci/ics/jung/graph/Graph.html">edu.uci.ics.jung.graph.Graph</a>
	 */
	public GraphViewer(edu.uci.ics.jung.graph.Graph<V, E> graph) {
		this.invokerGraph = null;
		this.graph = graph;
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the frame.
	 */
	@SuppressWarnings("unchecked")
	public void initComponents() {
		collapsedGraph = this.graph;
		collapser = new GraphCollapser(this.graph);

		layout = new KKLayout<V, E>(this.graph);

		Dimension preferredSize = new Dimension(1024, 600);
		final VisualizationModel<V, E> visualizationModel = new DefaultVisualizationModel<V, E>(layout, preferredSize);
		canvas = new Canvas<V, E>(this, visualizationModel, preferredSize);

		final PredicatedParallelEdgeIndexFunction<V, E> eif = PredicatedParallelEdgeIndexFunction.getInstance();
		final Set<E> exclusions = new HashSet<E>();
		eif.setPredicate(new Predicate<E>() {
			@Override
			public boolean evaluate(E e) {
				return exclusions.contains(e);
			}
		});

		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(GraphViewer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(GraphViewer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(GraphViewer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			Logger.getLogger(GraphViewer.class.getName()).log(Level.SEVERE, null, ex);
		}

		canvas.setBackground(Color.white);

		// ---- Vertex color: ----
		canvas.getRenderer().setVertexRenderer(new GradientVertexRenderer<V, E>(Color.white, Color.white, // colors in
																											// normal
		Color.orange, Color.orange, // colors in picked state
		canvas.getPickedVertexState(), false));
		// canvas.getRenderContext().setVertexFillPaintTransformer(new
		// PickableVertexPaintTransformer<V>(canvas.getPickedVertexState(), Color.red,
		// Color.yellow));

		pickActivated();

		// --- Vertex labels: -----
		canvas.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<V>());
		canvas.getRenderer().getVertexLabelRenderer().setPositioner(new BasicVertexLabelRenderer.InsidePositioner());
		canvas.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

		// add a listener for ToolTips
		canvas.setVertexToolTipTransformer(new ToStringLabeller<V>() {
			public String transform(V v) {
				if (v instanceof Graph) { // collapsed vertices
					return ((Graph<V, E>) v).vertexSet().toString();
				}
				return v.toString();
			}
		});

		// add a listener for ToolTips
		canvas.setEdgeToolTipTransformer(new ToStringLabeller<E>() {
			@Override
			public String transform(E e) {
				return e.toString();
			}
		});

		// --- Edge Labels: ---
		canvas.getRenderContext().getEdgeLabelRenderer();

		/**
		 * the regular graph mouse for the normal view
		 */
		final DefaultModalGraphMouse<V, E> graphMouse = new DefaultModalGraphMouse<V, E>();

		canvas.setGraphMouse(graphMouse);

		// --- Control Panel: ---------------
		content = getContentPane();
		gzsp = new GraphZoomScrollPane(canvas);
		content.add(gzsp);

		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		modePanel = new JPanel();
		modePanel.setBorder(BorderFactory.createTitledBorder("Activated"));
		modePanel.setLayout(new java.awt.GridLayout(1, 1));

		if (invokerGraph instanceof Graph) {
			JButton runAll = new JButton("Activation");
			runAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					runActivation();
				}
			});
			modePanel.add(runAll);
		}

		scaler = new CrossoverScalingControl();

		buttonPanel = new JPanel();

		Class<? extends Layout>[] combos = getCombos();
		jcb = new JComboBox(combos);
		// use a renderer to shorten the layout name presentation
		jcb.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				String valueString = value.toString();
				valueString = valueString.substring(valueString.lastIndexOf('.') + 1);
				return super.getListCellRendererComponent(list, valueString, index, isSelected, cellHasFocus);
			}
		});
		jcb.addActionListener(new GraphViewer.LayoutChooser(jcb, canvas));
		jcb.setSelectedItem(layout.getClass());

		layoutChoice = new JPanel();
		layoutChoice.setBorder(BorderFactory.createTitledBorder("Graph Layout"));
		layoutChoice.setLayout(new java.awt.GridLayout(1, 2));

		// Vertex Thresholds:
		thresPanel = new JPanel();
		thresholds = new javax.swing.JCheckBox("Threshold");
		thresPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING));
		thresholds.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				writeVertexThresholds();
			}
		});
		thresPanel.add(thresholds);
		thresPanel.add(thresholds);
		layoutChoice.add(thresPanel);

		layoutChoice.add(jcb); // Combobox

		// --- Build up Control Panel: ----
		controls = new JPanel();
		controls.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
		controls.add(layoutChoice);
		controls.add(modePanel);

		controls.add(buttonPanel);
		content.add(controls, BorderLayout.SOUTH);

		// Frame Properties:
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Picks activated vertices in this graph. That means, if the vertices are
	 * instances of the interface <cod>{@link Activated Activated}</code>, the
	 * active one among them are marked as picked.
	 */
	private void pickActivated() {
		Set<V> vSet = invokerGraph.vertexSet();
		V ator = vSet.iterator().next();
		if (!(ator instanceof Activatable))
			return;
		// Pick activated actors in a social network:
		for (V v : graph.getVertices()) {
			if (((Activatable) v).isActive()) {
				canvas.getPickedVertexState().pick(v, true);
			}
		}
	}

	/**
	 * Computes the nodes activated by the specified active generation of nodes in
	 * this network, after a single activation step. It in essence calls the
	 * implemented method
	 * {@link NetworkOfActivatables#nextActivationStep(java.util.HashSet)
	 * nextActivationStep} of the interface {@link NetworkOfActivatables
	 * NetworkOfActivatables}. The current active generation for this step is
	 * determined from the picked vertices in this visualized graph.
	 */
	private void runActivation() {
		Set<V> vSet = invokerGraph.vertexSet();
		for (V v : vSet) {
			if (((Activatable) v).isActive()) {
				canvas.getPickedVertexState().pick((V) v, true);
			}
		}
	}

	/** Writes the vertex thresholds. */
	private void writeVertexThresholds() {
		Transformer<V, String> stringer;
		if (thresholds.isSelected()) {
			stringer = new Transformer<V, String>() {
				@Override
				public String transform(V v) {
					if (v instanceof Vertex) {
						int t = (int) ((Vertex) v).getThreshold();
						return Integer.toString(t);
					} else
						return v.toString();
				}
			};
		} else {
			stringer = new Transformer<V, String>() {
				@Override
				public String transform(V v) {
					return v.toString();
				}
			};
		}

		canvas.getRenderContext().setVertexLabelTransformer(stringer);
		canvas.repaint();
	}

	private class LayoutChooser implements ActionListener {

		private final JComboBox jcb;
		private final Canvas<V, E> canvas;

		private LayoutChooser(JComboBox jcb, Canvas<V, E> canvas) {
			super();
			this.jcb = jcb;
			this.canvas = canvas;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent arg0) {
			Object[] constructorArgs = { collapsedGraph };

			Class<? extends Layout<V, E>> layoutC = (Class<? extends Layout<V, E>>) jcb.getSelectedItem();

			try {
				Constructor<? extends Layout<V, E>> constructor = layoutC
						.getConstructor(new Class[] { edu.uci.ics.jung.graph.Graph.class });
				Object o = constructor.newInstance(constructorArgs);
				Layout<V, E> l = (Layout<V, E>) o;
				l.setInitializer(canvas.getGraphLayout());
				l.setSize(canvas.getSize());
				layout = l;
				LayoutTransition<V, E> lt = new LayoutTransition<V, E>(canvas, canvas.getGraphLayout(), l);
				Animator animator = new Animator(lt);
				animator.start();
				canvas.getRenderContext().getMultiLayerTransformer().setToIdentity();
				canvas.repaint();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return an array of {@link Layout Layouts}
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends Layout>[] getCombos() {
		List<Class<? extends Layout>> layouts = new ArrayList<Class<? extends Layout>>();
		layouts.add(KKLayout.class);
		layouts.add(FRLayout.class);
		layouts.add(CircleLayout.class);
		layouts.add(SpringLayout.class);
		layouts.add(SpringLayout2.class);
		layouts.add(ISOMLayout.class);
		return layouts.toArray(new Class[0]);
	}

	private static final Color[] palette = { Color.YELLOW, Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
			Color.MAGENTA, Color.ORANGE, Color.LIGHT_GRAY, Color.PINK, Color.BLACK };

}
