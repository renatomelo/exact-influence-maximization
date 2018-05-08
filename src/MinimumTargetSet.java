
/* 
 * Minimum target set: Find the smallest set S which activates the 
 * 					   entire set of vertices in a graph
 */

import gurobi.*;

public class MinimumTargetSet {

	public static void main(String[] args) {
		try {
			// 1 if the vertex i has a arc to
			int AdjMatix[][] = new int[][] { { 0, 1, 0, 0 }, { 0, 0, 1, 1 }, { 0, 0, 0, 0 }, { 1, 0, 1, 0 } };
//			int AdjMatix[][] = new int[][] { { 0, 1, 0, 0, 0, 0, 0, 0 },
//											 { 0, 0, 1, 0, 0, 0, 1, 1 },
//											 { 0, 0, 0, 1, 0, 1, 0, 0 },
//											 { 0, 0, 1, 0, 1, 0, 0, 0 },
//											 { 0, 0, 0, 1, 0, 1, 0, 0 },
//											 { 0, 0, 0, 0, 0, 0, 1, 0 },
//											 { 0, 0, 0, 0, 0, 1, 0, 0 },
//											 { 1, 0, 0, 0, 0, 0, 1, 0 } };
			// Every vertex v has t(v) = 1
//			int t[] = new int[] { 1, 1, 2, 2, 1, 3, 3, 1 };
			int t[] = new int[] { 1, 1, 2, 1 };

			int n = t.length;
			
			// Model
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
			model.set(GRB.StringAttr.ModelName, "MTS");
			
			// Target set decision variables: s[v] == 1 if plant v is in S.
			GRBVar[] s = new GRBVar[n];
			for (int v = 0; v < n; ++v) {
				s[v] = model.addVar(0, 1, 0, GRB.BINARY, "s_" +v);
			}
			
			// Edge tournament variables: tournament[u][v] = 1 if (u,v) is an arc 
			// of the tournament.
			GRBVar[][] tournament = new GRBVar[n][n];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					tournament[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "e_"+i+""+j);
				}
			}
			
//			//forbid self-loop
//			for (int i = 0; i < n; i++) 
//				tournament[i][i].set(GRB.DoubleAttr.UB, 0);
			
			// Set objective function: maximize a[0] + a[1] + ... + a[n-1]
			GRBLinExpr obj = new GRBLinExpr();
			for (int i = 0; i < n; i++) 
				obj.addTerm(1, s[i]);
			model.setObjective(obj, GRB.MINIMIZE);
			
			GRBLinExpr lhs;
			
			// Activation constraints: a vertex v will be activated if the number of
			// active in-neighbors is at least t(v)
			for (int i = 0; i < n; i++) {
				lhs = new GRBLinExpr();
				for (int j = 0; j < n; j++) 
					lhs.addTerm(AdjMatix[i][j], tournament[i][j]);
					
				lhs.addTerm(t[i], s[i]);
				model.addConstr(lhs, GRB.GREATER_EQUAL, t[i], "activation_"+i);
			}
			
			// Tournament constraints
			// direction of the arcs: e_uv + e_vu = 1 for i != j
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
						lhs = new GRBLinExpr();
						lhs.addTerm(1, tournament[i][j]);
						lhs.addTerm(1, tournament[j][i]);
						model.addConstr(lhs, GRB.EQUAL, 1, "arc_" + i + "" + j);
				}
			}
			
			// Cycle prevention constraints
			for (int u = 0; u < n; u++) {
				for (int v = 0; v < n; v++) {
					if (v != u) {
						for (int w = 0; w < n; w++) {
							if (w != v && w != u) {
								lhs = new GRBLinExpr();
								lhs.addTerm(1, tournament[u][v]);
								lhs.addTerm(1, tournament[v][w]);
								lhs.addTerm(1, tournament[w][u]);
								model.addConstr(lhs, GRB.LESS_EQUAL, 2, 
										"cycle(" + u + "" + v + "" + w + ")");
							}
						} 
					}
				}
			}
			
			model.optimize();
			
			System.out.println("\nMatrix of the tournament:");
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					System.out.print(tournament[i][j].get(GRB.DoubleAttr.X)+"\t");
				}
				System.out.println();
			}
			
			System.out.println("\nMatrix of the G[A]:");
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					System.out.print(tournament[i][j].get(GRB.DoubleAttr.X) 
							* AdjMatix[i][j]+"\t");
				}
				System.out.println();
			}
			
			System.out.println("\n\nTarget Set at the end of activation:");
			for (int i = 0; i < tournament.length; i++)
				System.out.print(s[i].get(GRB.DoubleAttr.X)+"\t");
			System.out.println();
			
			// Dispose of model and environment
			model.dispose();
			env.dispose();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
}
