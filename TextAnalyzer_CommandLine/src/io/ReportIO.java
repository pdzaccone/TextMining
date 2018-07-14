package io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportIO {
	
	/**
	 * Keys are class names, 0th element - total, 1st - bad
	 */
	private Map<String, List<Integer>> objects;
	private IReadable result;
	
	public ReportIO() {
		objects = new HashMap<>();
		result = null;
	}
	
	public void addObject(String type, IReadable obj) {
		List<Integer> list;
		if (!objects.containsKey(type)) {
			list = new ArrayList<>();
			list.add(0);
			list.add(0);
		} else {
			list = objects.get(type);
		}
		list.set(0, list.get(0) + 1);
		if (obj == null) {
			list.set(1, list.get(1) + 1);
		}
	}

	public boolean isValid(String type) {
		return objects.containsKey(type) && objects.get(type).get(0) > objects.get(type).get(1);
	}

	public void addReturnObject(IReadable input) {
		this.result = input;
		addObject(input.getClass().getName(), input);
	}
}