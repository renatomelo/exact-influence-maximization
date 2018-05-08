package activable_network;

import interfaces.Activatable;

public class Vertex implements Activatable{
	 protected double threshold;
	 protected int index;
	 protected String name;
	 protected boolean active;
	 
	public Vertex(String nome, int index) {
		super();
		this.index = index;
		this.name = ""+index;
	}

	public Vertex(String name) {
		this.name = name;	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;		
	}
}
