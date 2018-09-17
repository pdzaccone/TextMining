package analyzers;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import utils.WeightedObject;

/**
 * This is a helper class. It helps gathering statistical data about the input data and the text-mining being carried out.
 * <p> It is used by {@link StatisticsGatherer} analyzer
 * @author Pdz
 *
 */
public class StatisticsBlock {

	/**
	 * Internal data storage for "fixed" data, with single value
	 */
	private Map<AnalysisTypes, String> dataRigid;
	
	/**
	 * Internal data storage for data with unknown number of values
	 */
	private Map<AnalysisTypes, Map<String, Double>> dataFlexible;
	
	/**
	 * Constructor
	 */
	public StatisticsBlock() {
		this.dataRigid = new HashMap<>();
		this.dataFlexible = new HashMap<>();
	}

	/**
	 * Gets "flexible" data of specific type
	 * @param type Data type
	 * @return Resulting data or an empty map
	 */
	public Map<String, Double> getFlexible(AnalysisTypes type) {
		if (this.dataFlexible.containsKey(type)) {
			return this.dataFlexible.get(type);
		}
		return new HashMap<>();
	}

	/**
	 * Gets "rigid" data of specific type
	 * @param type Data type
	 * @return Resulting data or an empty string
	 */
	public String getRigid(AnalysisTypes type) {
		if (this.dataRigid.containsKey(type)) {
			return this.dataRigid.get(type);
		}
		return "";
	}

	/**
	 * Adds "flexible" data
	 * @param type Type of data
	 * @param input Data itself
	 */
	public void setParams(AnalysisTypes type, TreeSet<WeightedObject> input) {
		if (input != null && !input.isEmpty()) {
			Map<String, Double> inputMap = new HashMap<>();
			for (WeightedObject wo : input) {
				inputMap.put(wo.getData(), wo.getWeight());
			}
			this.dataFlexible.put(type, inputMap);
		}
	}

	/**
	 * Adds "rigid" data
	 * @param type Type of data
	 * @param input Data itself
	 */
	public void setParams(AnalysisTypes type, String input) {
		this.dataRigid.put(type, input);
	}

	/**
	 * Checks whether the {@link StatisticsBlock} has any meaningful data
	 * @return True - has some data, false - empty
	 */
	public boolean hasData() {
		return !this.dataRigid.isEmpty() || !this.dataFlexible.isEmpty();
	}
}