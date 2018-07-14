package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ListMap<T extends Comparable<T>, V> {
	
	private Map<T, List<V>> data;
	
	public ListMap() {
		this.data = new HashMap<>();
	}
	
	public ListMap(ListMap<T, V> input) {
		this.putAll(input);
	}

	public void put(T key, V value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
//		if (data.containsKey(key) && data.get(key).contains(value)) {
//			return;
//		}
		List<V> list = null;
		if (data.containsKey(key)) {
			list = data.get(key);
		} else {
			list = new ArrayList<V>();
		}
		list.add(value);
		data.put(key, list);
	}
	
	public void putAll(ListMap<T, V> input) {
		Objects.requireNonNull(input);
		for (T type : input.keySet()) {
			for (V val : input.get(type)) {
				put(type, val);
			}
		}
	}

	public int size() {
		return data.size();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	public Set<T> keySet() {
		return data.keySet();
	}
	
	public List<V> get(T key) {
		if (this.data.containsKey(key)) {
			return data.get(key);
		}
		return new ArrayList<V>();
	}
	
	public List<V> getAll() {
		List<V> result = new ArrayList<>();
		this.data.values().stream().forEach(val -> result.addAll(val));
		return result;
	}

	public void removeType(T type) {
		this.data.remove(type);
	}
}
