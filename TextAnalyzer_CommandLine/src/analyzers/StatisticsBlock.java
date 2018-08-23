package analyzers;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import utils.WeightedObject;

public class StatisticsBlock {

	private Map<AnalysisTypes, String> dataRigid;
	private Map<AnalysisTypes, Map<String, Double>> dataFlexible;
	
	public StatisticsBlock() {
		this.dataRigid = new HashMap<>();
		this.dataFlexible = new HashMap<>();
	}

	public Map<String, Double> getFlexible(AnalysisTypes type) {
		if (this.dataFlexible.containsKey(type)) {
			return this.dataFlexible.get(type);
		}
		return new HashMap<>();
	}

	public String getRigid(AnalysisTypes type) {
		if (this.dataRigid.containsKey(type)) {
			return this.dataRigid.get(type);
		}
		return "";
	}

	public void setParams(AnalysisTypes type, TreeSet<WeightedObject> input) {
		if (input != null && !input.isEmpty()) {
			Map<String, Double> inputMap = new HashMap<>();
			for (WeightedObject wo : input) {
				inputMap.put(wo.getData(), wo.getWeight());
			}
			this.dataFlexible.put(type, inputMap);
		}
	}

	public void setParams(AnalysisTypes type, String input) {
		this.dataRigid.put(type, input);
	}

	public boolean hasData() {
		return !this.dataRigid.isEmpty() || !this.dataFlexible.isEmpty();
	}
}
