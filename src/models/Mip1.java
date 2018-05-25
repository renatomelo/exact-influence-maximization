package models;
import gurobi.*;

public class Mip1 {
	public static void main(String[] args) {
		System.out.println("first line!");
		try {
			GRBEnv amb = new GRBEnv("mip1.log");
			GRBModel m = new GRBModel(amb);
			
			//Variaveis de decisao
			GRBVar x = m.addVar(0, 1, 0, GRB.BINARY, "x");
			GRBVar y = m.addVar(0, 1, 0, GRB.BINARY, "y");
			GRBVar z = m.addVar(0, 1, 0, GRB.BINARY, "z");
			
			//Funcao objetivo
			GRBLinExpr expr = new GRBLinExpr();
			expr.addTerm(1.0, x);
			expr.addTerm(1.0, y);
			expr.addTerm(2.0, z);
			m.setObjective(expr, GRB.MAXIMIZE);
			
			//restricoes
			// x + 2y + 3z <= 4
			expr = new GRBLinExpr();
			expr.addTerm(1.0, x);
			expr.addTerm(2.0, y);
			expr.addTerm(3.0, z);
			m.addConstr(expr, GRB.LESS_EQUAL, 4, "c0");
			
			// x + y >= 1
			expr = new GRBLinExpr();
			expr.addTerm(1.0, x);
			expr.addTerm(1.0, y);
			m.addConstr(expr, GRB.GREATER_EQUAL, 1, "c1");
			
			m.optimize();
			System.out.println("otimizou!");
			//apresenta a solucao
			System.out.println(x.get(GRB.StringAttr.VarName)+" "+x.get(GRB.DoubleAttr.X));
			System.out.println(y.get(GRB.StringAttr.VarName)+" "+y.get(GRB.DoubleAttr.X));
			System.out.println(z.get(GRB.StringAttr.VarName)+" "+z.get(GRB.DoubleAttr.X));
			
			System.out.println("Obj: " + m.get(GRB.DoubleAttr.ObjVal));
			
			m.dispose();
			amb.dispose();
			
		} catch (GRBException e) {
			System.out.println("Codigo de erro: "+ e.getErrorCode()+". "+ e.getMessage());
		}
	}
}
