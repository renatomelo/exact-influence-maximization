import gurobi.*;

public class TSP extends GRBCallback {

	private GRBVar[][] variaveis;

	public TSP(GRBVar[][] variaveis) {
		this.variaveis = variaveis;
	}
	
	protected void callback() {
		try {
			if(where == GRB.CB_MIPSOL) {
				int n = variaveis.length;
				System.out.println("Calback method!!");
//				System.out.println("variaveis lenght: "+n);
//				double[][] solut = getSolution(variaveis);
//				System.out.println("\n Vetor de solucao do callback:");
//				for (int i = 0; i < solut.length; i++) {
//					for (int j = 0; j < solut.length; j++) {
//						System.out.print(solut[i][j]+" ");
//					}
//					System.out.println();
//				}
//				System.out.println();
				int[] passeio = encontrarSubPasseio(getSolution(variaveis));
				
				if(passeio.length < n) {
					GRBLinExpr expressao = new GRBLinExpr();
					for (int i = 0; i < passeio.length; i++) {
						for (int j = i + 1; j < passeio.length; j++) 
							expressao.addTerm(1, variaveis[passeio[i]][passeio[j]]);
						addLazy(expressao, GRB.LESS_EQUAL, passeio.length - 1);						
					}
				}
			}
		} catch (GRBException e) {
			System.out.println("Codigo de erro: "+ e.getErrorCode()+". "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	protected static int[] encontrarSubPasseio(double[][] solucao) {
		int n = solucao.length;
		System.out.println("encontrar sub passeio!!");
//		System.out.println("n:"+n);
		boolean[] visitado = new boolean[n];
		int[] passeio = new int[n];
		int bestind, bestlen;
		int i, node, len, start;
		
		for (i = 0; i < n; i++) {
			visitado[i] = false;
		}
		
		start = 0;
		bestlen = n + 1;
		bestind = -1;
		node = 0;
		
		while(start < n) {
//			System.out.print("\n visitado[]: ");
			for (node = 0; node < n; node++) {
//				System.out.print(visitado[node]+" ");
				if(!visitado[node]) break;
			}
			if (node == n) break;
			
			for(len = 0; len < n; len++) {
//				System.out.println("start:"+start);
//				System.out.println("len:"+len);
//				System.out.println("passeio[start + len] = "+node);
				passeio[start + len] = node;
				visitado[node] = true;
				for (i = 0; i < n; i++) {
					if (solucao[node][i] > 0.5 && !visitado[i]) {
						node = i;
						break;
					}
				}
				if(i == n) {
					len++;
					if(len < bestlen) {
						bestlen = len;
						bestind = start;
					}
					start += len;
					break;
				}
			}
		}
		System.out.println("bestlen: "+bestlen);
		System.out.println("bestind: "+bestind);
		int[] result = new int[bestlen];
		for (int j = 0; j < bestlen; j++) {
			result[j] = passeio[bestind + j];
		}
		return result;
	}

	/**
	 * Distancia euclidiana entre os ponto i e j.
	 * @param x vetor de inteiros
	 * @param y vetor de inteiros
	 * @param i inteiro
	 * @param j inteiro
	 * @return
	 */
	private static double distancia(double[] x, double[] y, int i, int j) {
		double dx = x[i] - x[j];
		double dy = y[i] - y[j];
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	//Modo de uso: java TSP <numero de cidades>
	public static void main(String[] args) {
		int n = Integer.parseInt(args[0]);
		
		try {
			GRBEnv ambiente = new GRBEnv();
			GRBModel m = new GRBModel(ambiente);
			
			m.set(GRB.IntParam.LazyConstraints, 1);
			
			double[] x = new double[n];
			double[] y = new double[n];
			
			for (int i = 0; i < n; i++) {
				x[i] = Math.random();
				y[i] = Math.random();
			}
			System.out.println("\nVetores x e y");
			for (int i = 0; i < n; i++) 
				System.out.printf("%.3f ",x[i]);
			System.out.println();
			for (int i = 0; i < n; i++) 
				System.out.printf("%.3f ",y[i]);
			
			GRBVar[][] variaveis = new GRBVar[n][n];
			
			for (int i = 0; i < n; i++) {
				for (int j = 0; j <= i; j++) {
					variaveis[i][j] = m.addVar(0, 1, distancia(x, y, i, j),  GRB.BINARY, "x"+i+"_"+j);
					variaveis[j][i] = variaveis[i][j];
				}
			}
			
			System.out.println("\n\nMatriz de Coeficientes");
			for (int i = 0; i < variaveis.length; i++) {
				for (int j = 0; j < variaveis.length; j++) {
					System.out.printf("%.3f ",distancia(x, y, i, j));
				}
				System.out.println();
			}
			System.out.println();
			
			//Resticoes de grau 2
			for (int i = 0; i < n; i++) {
				GRBLinExpr expressao = new GRBLinExpr();
				for (int j = 0; j < n; j++) {
					expressao.addTerm(1, variaveis[i][j]);
				}
				m.addConstr(expressao, GRB.EQUAL, 2, "grau2_"+String.valueOf(i));
			}
			
			for (int i = 0; i < n; i++) {
				variaveis[i][i].set(GRB.DoubleAttr.UB, 0);
			}
			
			m.setCallback(new TSP(variaveis));
			m.optimize();
			
			if(m.get(GRB.IntAttr.SolCount) > 0) {
				int[] passeio = encontrarSubPasseio(m.get(GRB.DoubleAttr.X, variaveis));
				assert passeio.length == n;
				
				System.out.print("\nPasseio: ");
				for (int i = 0; i < passeio.length; i++) {
					System.out.print(passeio[i] + " ");					
				}
				System.out.println();
			}
			
			m.dispose();
			ambiente.dispose();
		} catch (GRBException e) {
			System.out.println("Codigo de erro: "+e.getErrorCode()+". "+e.getMessage());
		}
	}
}
