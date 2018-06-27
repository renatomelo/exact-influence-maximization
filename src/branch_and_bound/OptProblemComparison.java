package branch_and_bound;

import java.util.Comparator;

public class OptProblemComparison implements Comparator<OptimizationProblem> {

	@Override
	public int compare(OptimizationProblem p1, OptimizationProblem p2) {
		if(p1.getValue() < p2.getValue()) 
			return 1;
		if(p1.getValue() > p2.getValue()) 
			return -1;
		return 0;
	}
}
