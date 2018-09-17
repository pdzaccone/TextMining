package utils;

/**
 * Helper class, holding an object and its weight
 * @author Pdz
 *
 */
public class WeightedObject implements Comparable<WeightedObject> {

	/**
	 * String data
	 */
	private String data;
	
	/**
	 * Its weight
	 */
	private double weight;
	
	/**
	 * Constructor with parameters
	 * @param data Data
	 * @param weight Weight
	 */
	public WeightedObject(String data, double weight) {
		this.data = data;
		this.weight = weight;
	}

	/**
	 * Default constructor
	 */
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
