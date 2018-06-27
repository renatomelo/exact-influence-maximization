package branch_and_bound;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

public class KnapsackBnB {
	private double maxValue;
	private double capacity; // knapsack capacity
	private double[] sizes; //array of sizes
	private double[] values; // array of values (ordered by value density)
	private Vector<Integer> bestList; // members of solution set for current best value
	private int nItens; // number of items in set to select from
	
	private Queue<Node> Q;
	
	class Node {
		int level;
		double size, value, bound;
		Vector<Integer> contains;
		
		protected Node() {
			level = 0;
			size = 0;
			value = 0;
			bound = 0;
			contains = null;
		}
		
		protected void copyList(Vector<Integer> v) {
			if(v == null || v.isEmpty())
				contains = new Vector<Integer>();
			else 
				contains = new Vector<Integer>(v);
		}
		
		protected void add(int index) {
			contains.add(new Integer(index));
		}
	}
	
	public KnapsackBnB(double capacity, double[] size, double[] value, int n) {
		this.maxValue = 0;
		this.capacity = capacity;
		this.sizes = size;
		this.values = value;
		this.nItens = n;
		this.bestList = null;
		this.Q = new LinkedList<>();
	}
	
	private void knapsack() {
		while(!Q.isEmpty()) {
			Node tmp = Q.poll();
			if(tmp.bound > this.maxValue) {
				Node u = new Node();
				u.level = tmp.level + 1;
				u.size = tmp.size + sizes[tmp.level + 1];
				u.value = tmp.value + values[tmp.level + 1];
				u.copyList(tmp.contains);
				u.add(tmp.level + 1);
				
				if(u.size < capacity && u.value > maxValue) {
					maxValue = u.value;
					bestList = new Vector<>(u.contains);
				}
				
				u.bound = bound(u.level, u.size, u.value);
				if(u.bound > maxValue)
					Q.offer(u);
				
				Node w = new Node();
				w.level = tmp.level + 1;
				w.size = tmp.size;
				w.value = tmp.value;
				w.copyList(tmp.contains);
				w.add(tmp.level + 1);
				w.bound = bound(w.level, w.size, w.value);
				if(w.bound > maxValue)
					Q.offer(w);
			}
		}
	}

	private double bound(int item, double size, double value) {
		double bound = value;
		double totalSize = size;
		int k = item + 1;
		
		if(size > capacity)
			return 0;
		
		while(k < nItens && totalSize + sizes[k] <= capacity) {
			bound += values[k];
			totalSize += sizes[k];
			k++;
		}
		
		if(k < nItens)
			bound += (capacity - totalSize) * (values[k]/sizes[k]);
		
		return bound;
	}
	
	public void findSolution() {
		Node root = new Node();
		root.level = 0;
		root.size = 0;
		root.value = 0;
		root.bound = bound(0,0,0);
		root.copyList(null);
		Q.offer(root);
		
		knapsack();
		
		System.out.println("The solution set is: ");
		for(int i = 0; i < bestList.size(); i++)
			System.out.println(" "+ bestList.get(i));
		System.out.println();
		System.out.println("The value contained in the knapsack is: $" + maxValue);
		
	}
	
}
