package branch_and_bound;

/**
 * All the problems to be solved (the original and its relaxations) are required to implement
 *  this interface
 * @author renato
 *
 */
public interface OptimizationProblem {
	public OptimizationProblem getRelaxation();
	public int getProblemSize();
	public double getValue();
	public OptimizationProblemSolution getSolution();
	public boolean isValid(OptimizationProblemSolution ops);
	public OptimizationProblem[] branch();
	public void performUpperBounding(double upperbound);
}
