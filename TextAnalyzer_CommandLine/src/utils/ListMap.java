package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Helper class for storing data in form of map where each key can have multiple values
 * @author Pdz
 *
 * @param <T>
 * @param <V>
 */
public class ListMap<T extends Comparable<T>, V> {
	
	/**
	 * Internal data storage
	 */
	private Map<T, List<V>> data;
	
	/**
	 * Constructor without parameters
	 */
	public ListMap() {
		this.data = new HashMap<>();
	}
	
	/**
	 * Copy constructor
	 * @param input Object to copy
	 */
	public ListMap(ListMap<T, V> input) {
		this.putAll(input);
	}

	/**
	 * Removes duplicate values - each key has only unique values after this method has been called
	 */
	public void removeDuplicates() {
		for (T key : data.keySet()) {
			Set<V> tmp = new HashSet<>();
			tmp.addAll(data.get(key));
			data.get(key).clear();
			data.get(key).addAll(tmp);
		}
	}
	
	/**
	 * Adds new element
	 * @param key Key
	 * @param value Value
	 */
	public void put(T key, V value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		List<V> list = null;
		if (data.containsKey(key)) {
			list = data.get(key);
		} else {
			list = new ArrayList<V>();
		}
		list.add(value);
		data.put(key, list);
	}

	/**
	 * Adds new elements
	 * @param key Key
	 * @param values List of values for the provided key
	 */
	public void put(T key, List<V> values) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(values);
		List<V> list = null;
		if (data.containsKey(key)) {
			list = data.get(key);
		} else {
			list = new ArrayList<V>();
		}
		list.addAll(values);
		data.put(key, list);
	}

	/**
	 * Adds another {@link ListMap} to the existing one
	 * @param input List map to add
	 */
	public void putAll(ListMap<T, V> input) {
		Objects.requireNonNull(input);
		for (T type : input.keySet()) {
			for (V val : input.get(type)) {
				put(type, val);
			}
		}
	}

	/**
	 * Gets number of keys
	 * @return Number of different keys
	 */
	public int size() {
		return data.size();
	}

	/**
	 * Checks whether the {@link ListMap} is empty or not
	 * @return True if empty
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	/**
	 * Gets keyset
	 * @return Keyset
	 */
	public Set<T> keySet() {
		return data.keySet();
	}
	
	/**
	 * Gets all values for all keys
	 * @return Collection with all values
	 */
	public Collection<List<V>> values() {
		return data.values();
	}
	
	/**
	 * Gets all values for a specific key
	 * @param key Key
	 * @return List of values
	 */
	public List<V> get(T key) {
		if (this.data.containsKey(key)) {
			return data.get(key);
		}
		return new ArrayList<V>();
	}
	
	/**
	 * Gets all values packed in a single array
	 * @return All values
	 */
	public List<V> getAll() {
		List<V> result = new ArrayList<>();
		this.data.values().stream().forEach(val -> result.addAll(val));
		return result;
	}

	/**
	 * Removes specified key
	 * @param key Key
	 */
	public void removeKey(T key) {
		this.data.remove(key);
	}

	/**
	 * Removes specific value for specific key
	 * @param key Key
	 * @param val Value to remove
	 */
	public void remove(T key, V val) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(val);
		if (this.data.containsKey(key)) {
			this.data.get(key).remove(val);
		}
	}

	/**
	 * Clears data
	 */
	public void clear() {
		this.data.clear();
	}
}
