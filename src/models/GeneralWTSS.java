package models;

import gurobi.GRB;
import gurobi.GRBCallback;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;

public class GeneralWTSS extends GRBCallback{
	private GRBVar[][] h;

	/**
	 * Lazy procedure for recognition and generation of violated constraints, which are then
	 *  passed to the solver and added to the model
	 */
	protected void callback() {
		try {
			if(where == GRB.CB_MIPSOL) {
				double[][] solution = getSolution(h);
				int[] dicycle = findDicycle(solution);
				if(hasDicycle(solution)) {
					//Add a directed cycle elimination constraint
					GRBLinExpr lhs = new GRBLinExpr();
					
				}
			}
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}

	private int[] findDicycle(double[][] solution) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean hasDicycle(double[][] solution) {
		return false;
	}

}
