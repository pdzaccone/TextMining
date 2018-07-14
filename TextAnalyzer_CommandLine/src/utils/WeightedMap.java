package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class WeightedMap {
	
	private int size;
	private Map<String, Integer> data;
	
	public WeightedMap() {
		this.data = new HashMap<>();
		this.size = 0;
	}
	
	public void add(String input, int count) {
		if (count > 0) {
			if (data.containsKey(input)) {
				data.put(input, data.get(input) + count);
			} else {
				data.put(input, count);
			}
			size += count;
		}
	}

	public TreeSet<WeightedObject> getWeights() {
		TreeSet<WeightedObject> result = new TreeSet<>();
		for (String key : this.data.keySet()) {
			result.add(new WeightedObject(key, (double) this.data.get(key) / (double) size));
		}
		return result;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public Set<String> keySet() {
		return this.data.keySet();
	}

	public int get(String term) {
		return this.data.get(term);
	}

	public boolean containsKey(String term) {
		return this.data.containsKey(term);
	}

	public void add(WeightedMap input) {
		for (String key : input.keySet()) {
			this.add(key, input.get(key));
		}
	}
	
	public int size() {
		return this.data.size();
	}
}