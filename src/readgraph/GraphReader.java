package readgraph;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.util.SupplierUtil;

import activable_network.Vertex;
import validation.*;

public class GraphReader {

	// Create the VertexFactory so the generator can create vertices
	Supplier<Vertex> supplier = new Supplier<Vertex>() {
		private int id = 0;

		@Override
		public Vertex get() {
			Vertex v = new Vertex("v" + (id + 1), id);
			id++;
			return v;
		}
	};

	public Graph<Vertex, DefaultEdge> run(int n, int m, String arquivo) {

		Vertex[] vertices = new Vertex[n];
		Graph<Vertex, DefaultEdge> g;
		g = new SimpleDirectedGraph<>(supplier, SupplierUtil.createDefaultEdgeSupplier(), false);

		try {
			FileInputStream fstream = new FileInputStream(arquivo);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {

				String[] str = null;
				if (strLine.contains(" ")) {
					str = strLine.split(" ");
				} else if (strLine.contains("\t")) {
					str = strLine.split("\t");
				}

				int[] values = new int[str.length];
				for (int i = 0; i < str.length; i++) {
					values[i] = Integer.valueOf(str[i]);
				}

				// create n vertices
				if (values[0] == n && values[1] == m) {
					for (int i = 0; i < values[0]; i++) {
						Vertex v = g.addVertex();
						vertices[i] = v;
					}
					continue;
				}

				if (values[0] >= 0 && values[0] <= n && values[1] >= 0 && values[1] <= n) {
					// Adiciona uma aresta e, caso já exista e = null
					int v1 = values[0];
					int v2 = values[1];

					if (v1 != v2) {
						g.addEdge(vertices[v1], vertices[v2]);
					}
				}
			}

			in.close();
		} catch (Exception e) {
			System.err.println("Erro ao ler o arquivo: " + e.getMessage());
		}

		return g;
	}

	public Graph<Vertex, DefaultEdge> readEpinions() {
		int n = 75888;
		int m = 508837;
		String arquivo = "data/social_networks/soc-Epinions1.txt";
		return run(n, m, arquivo);
	}

	public Graph<Vertex, DefaultEdge> readHepPh() {
		int n = 34546;
		int m = 421578;
		String arquivo = "data/social_networks/hepph.txt";
		return run(n, m, arquivo);
	}

	public Graph<Vertex, DefaultEdge> readHepTh() {
		int n = 27770;
		int m = 352807;
		String arquivo = "data/social_networks/hepth.txt";
		return run(n, m, arquivo);
	}

	public Graph<Vertex, DefaultEdge> readSlashdot0902() {
		int n = 82168;
		int m = 948464;
		String arquivo = "data/social_networks/soc-Slashdot0902.txt";
		return run(n, m, arquivo);
	}

	public Graph<Vertex, DefaultEdge> readSlashdot0811() {
		int n = 77360;
		int m = 905468;
		String arquivo = "data/social_networks/soc-Slashdot0811.txt";
		return run(n, m, arquivo);
	}

	public Graph<Vertex, DefaultEdge> readTwitter() {
		int n = 81306;
		int m = 1768149;
		String arquivo = "data/social_networks/twitter.txt";
		return run(n, m, arquivo);
	}

	public Graph<Vertex, DefaultEdge> readWikivote() {
		int n = 7115;
		int m = 103689;
		String arquivo = "data/social_networks/wikivote.txt";
		return run(n, m, arquivo);
	}

	public Graph<Vertex, DefaultEdge> readDblp() {
		int n = 12591;
		int m = 49743;
		String arquivo = "data/social_networks/dblp.txt";
		return run(n, m, arquivo);
	}
	
	public Graph<Vertex, DefaultEdge> readReddit() {
		int n = 35776;
		int m = 137821;
		String arquivo = "data/social_networks/reddit.txt";
		return run(n, m, arquivo);
	}
	
	public Graph<Vertex, DefaultEdge> readBtcAlpha() {
		int n = 3783;  
		int m = 24186; 
		String arquivo = "data/social_networks/btcalpha.txt";
		return run(n, m, arquivo);
	}
	
	public Graph<Vertex, DefaultEdge> readBtcOTC() {
		int n = 5881;  
		int m = 35592;  
		String arquivo = "data/social_networks/btcotc.txt";
		return run(n, m, arquivo);
	}
	
	public Graph<Vertex, DefaultEdge> readEmailEuALL() {
		int n = 265214;  
		int m = 420045;  
		String arquivo = "data/social_networks/Email-EuAll.txt";
		return run(n, m, arquivo);
	}

	public static void main(String args[]) {
		GraphReader reader = new GraphReader();
		Graph<Vertex, DefaultEdge> g;
		//g = reader.readDblp();
		//g = reader.readEpinions();
		//g = reader.readWikivote();
		//g = reader.readTwitter();
		//g = reader.readSlashdot0902();
		//g = reader.readHepPh();
		//g = reader.readHepTh();
		//g = reader.readReddit();
		//g = reader.readBtcAlpha();
		//g = reader.readBtcOTC();
		g = reader.readEmailEuALL();

		Histograma histograma = new Histograma();

		int[] h = histograma.gerarHistograma(g);
		try {
			histograma.plotarGraficos(h);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("|V(G)| = " + g.vertexSet().size());
		System.out.println("|E(G)| = " + g.edgeSet().size());
		System.out.println("Gráficos do histograma gerados!");
	}
}
