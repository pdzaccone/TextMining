package analyzers;

import java.util.HashMap;
import java.util.Map;

import utils.Pair;

public enum StatisticsFormat {
	id(0, "ID"),
	language(1, "Languages"),
	category(2, "Categories");
	
	private static final Map<StatisticsFormat, Pair<Integer, Integer>> map = new HashMap<>();
	
	static {
		for (StatisticsFormat val : values()) {
			map.put(val, new Pair<>(val.getOrder(), val.getOrder()));
		}
	}
	
	private final int orderNumber;
	private final String headerText;
	
	private StatisticsFormat(int order, String text) {
		this.orderNumber = order;
		this.headerText = text;
	}
	
	public int getOrder() {
		return this.orderNumber;
	}
	
	@Override
	public String toString() {
		return this.headerText;
	}
}
