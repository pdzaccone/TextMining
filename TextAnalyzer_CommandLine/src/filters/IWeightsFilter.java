package filters;

import java.util.List;

/**
 * This interface defines a "filter", allowing to define which values in the list can be ignored 
 * @author Pdz
 *
 */
public interface IWeightsFilter {
	
	/**
	 * Analyzes provided list with values and finds lowest threshold that is still of importance
	 * @param list Values to analyze
	 * @return Discovered threshold
	 */
	public double calculateMinimumAllowedValue(List<Double> list);
}
