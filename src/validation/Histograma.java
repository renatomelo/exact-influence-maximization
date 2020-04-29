package validation;

/*
 * Gera um histograma da distribuição de graus de um grafo e plota 
 * gráficos em escala normal e em escala log log. Útil para grafos 
 * Power Law
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.jgrapht.Graph;

public class Histograma {

	// Conta o grau de cada vértice do grafo e armazena em um vetor
	// Cada valor j do vetor v conta +1 na posicao j do vetor h
	public <V, E> int[] gerarHistograma(Graph<V, E> g) {
		Set<E> vizinhos;

		Set<V> vertices = g.vertexSet();
		int i = 0, n = vertices.size();
		int[] v = new int[n];
		for (V vertice : vertices) {
			//vizinhos = g.edgesOf(vertice);
			//v[i++] = vizinhos.size();
			v[i++] = g.degreeOf(vertice);
		}

		int j = 0;
		int h[] = new int[n];

		for (int k = 0; k < n; k++) {
			j = v[k];
			h[j] = h[j] + 1;
		}
		return h;
	}

	// Gera um arquivo contendo a distribuição de graus, onde a primeira coluna
	// (eixo x) é o grau e a segunda (eixo y) é o número vertices em cada grau.
	// Execta um script do gnuplot que está na raiz do projeto e gera dois
	// graficos como saida
	public void plotarGraficos(int h[]) throws IOException {

		File dist = new File("distribuicao.dat");
		dist.createNewFile();
		FileWriter writer = new FileWriter(dist);

		writer.write("#Grau | vertices\n");
		for (int i = 0; i < h.length; i++) {
			if (h[i] != 0)
				writer.write("" + i + " " + h[i] + "\n");
		}

		writer.flush();
		writer.close();

		Runtime run = Runtime.getRuntime();

		try {
			run.exec("gnuplot comandos");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
