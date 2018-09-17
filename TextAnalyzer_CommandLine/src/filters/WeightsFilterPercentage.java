package filters;

import java.util.Collections;
import java.util.List;

/**
 * Percentage-based filter 
 * @author Pdz
 *
 */
public class WeightsFilterPercentage implements IWeightsFilter {

	/**
	 * Threshold (0-1)
	 */
	private final double notPassedTill;
	
	/**
	 * Constructor with parameter
	 * @param threshold What part of values should be ignored (0-1)
	 */
	public WeightsFilterPercentage(double threshold) {
		notPassedTill = threshold;
	}
	
	/**
	 * Calculates the threshold, based on minimum and maximum values in the list and on the internal percentage value
	 */
	@Override
	public double calculateMinimumAllowedValue(List<Double> list) {
		Collections.sort(list);
		double min = list.get(0);
		double max = list.get(list.size() - 1);
		return min + notPassedTill * (max - min);
	}
}
