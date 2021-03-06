package branch_and_bound;

import java.util.Date;
import java.util.Vector;

public class BnB {
	private OptimizationProblem P;
	private double U;
	private OptimizationProblem currentbest = null;
	private Vector<OptimizationProblem> activeproblems;

	static double M = Double.MAX_VALUE / 1000;
	private long nodesGenerated = 0;
	private double elapsedtime = 0;
	private OptProblemComparison opc;

	public BnB(OptimizationProblem problem) {
		this.P = problem;
		int n = P.getProblemSize();
		this.activeproblems = new Vector<>(n * n, n);
		activeproblems.addElement(P);
		this.U = M;
		this.opc = new OptProblemComparison();
	}

	public OptimizationProblem solve() {
		OptimizationProblem problem, relaxation;
		double lowerbound;
		Date time = new Date();

		while (activeproblems.size() > 0) {
			problem = selectProblem();
			relaxation = problem.getRelaxation();
			lowerbound = relaxation.getValue();
			if (lowerbound < U) {
				if (P.isFeasible(relaxation.getSolution())) {
					U = lowerbound;
					this.currentbest = relaxation;
				} else {
					//optimal upper bounding
					relaxation.performUpperBounding(U);
					
					//branching
					OptimizationProblem[] subProblems = relaxation.branch();
					for (int i = 0; i < subProblems.length; i++) {
						this.activeproblems.addElement(subProblems[i]);
						this.nodesGenerated++;
					}
				}
			}
		}
		
		Date time1 = new Date();
		this.elapsedtime = (double) (time1.getTime() - time.getTime())/1000;
		return currentbest;
	}

	private OptimizationProblem selectProblem() {
		OptimizationProblem selected;
		
		//sort the vector by the value
		activeproblems.sort(opc);
		
		//select the best element and remove it from the list
		selected = activeproblems.remove(activeproblems.size() - 1);
		
		return selected;
	}
	
	public double getElapsedTime() {
		return this.elapsedtime;
	}
	
	public long getNodeCount() {
		return this.nodesGenerated;
	}
}
