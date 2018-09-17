package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents a weighted map. When an element is added to the map that is already present there, it's weight increases
 * @author Pdz
 *
 */
public class WeightedMap {
	
	/**
	 * Total number of elements (added, not stored)
	 */
	private int size;
	
	/**
	 * Data - keys are terms, values are weights
	 */
	private Map<String, Double> data;
	
	/**
	 * Constructor without parameters
	 */
	public WeightedMap() {
		this.data = new HashMap<>();
		this.size = 0;
	}
	
	/**
	 * Adds an element with known weight. If map already has such element, weights are added
	 * @param input Term
	 * @param count Weight
	 */
	public void add(String input, double count) {
		if (count > 0) {
			if (data.containsKey(input)) {
				data.put(input, data.get(input) + count);
			} else {
				data.put(input, count);
			}
			size += count;
		}
	}

	/**
	 * Gets weights as a sorted tree set
	 * @return Sorted set with weights, already adjusted considering the total number of elements
	 */
	public TreeSet<WeightedObject> getWeights() {
		TreeSet<WeightedObject> result = new TreeSet<>();
		for (String key : this.data.keySet()) {
			result.add(new WeightedObject(key, (double) this.data.get(key) / (double) size));
		}
		return result;
	}

	/**
	 * Checks whether the map is empty
	 * @return True, if empty
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Gets all unique terms
	 * @return Set of keys
	 */
	public Set<String> keySet() {
		return this.data.keySet();
	}

	/**
	 * Gets weight of an individual term
	 * @param term Term
	 * @return Weight
	 */
	public double get(String term) {
		return this.data.get(term);
	}

	/**
	 * Checks whether the map contains the given key 
	 * @param term Term to find
	 * @return True - map has it, otherwise false
	 */
	public boolean containsKey(String term) {
		return this.data.containsKey(term);
	}

	/**
	 * Increases current map by another weighted map
	 * @param input Weighted map being added
	 */
	public void add(WeightedMap input) {
		for (String key : input.keySet()) {
			this.add(key, input.get(key));
		}
	}
	
	/**
	 * Gets total number of element added (not stored)
	 * @return
	 */
	public int size() {
		return this.data.size();
	}
}