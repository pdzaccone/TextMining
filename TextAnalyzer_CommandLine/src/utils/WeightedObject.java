package utils;

public class WeightedObject implements Comparable<WeightedObject> {

	private String data;
	private double weight;
	
	public WeightedObject(String data, double weight) {
		this.data = data;
		this.weight = weight;
	}

	public WeightedObject() {
		this.data = null;
		this.weight = 0;
	}

	@Override
	public int compareTo(WeightedObject obj) {
		if (this.weight != obj.weight) {
			return this.weight > obj.getWeight() ? 1 : -1;
		} else {
			return this.data.compareTo(obj.getData());
		}
	}

	public String getData() {
		return data;
	}

	public double getWeight() {
		return weight;
	}
}
