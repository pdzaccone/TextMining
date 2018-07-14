package filters;

import java.util.Collections;
import java.util.List;

public class WeightsFilterPercentage implements IWeightsFilter {

	private final double notPassedTill;
	
	public WeightsFilterPercentage(double threshold) {
		notPassedTill = threshold;
	}
	
	@Override
	public double calculateMinimumAllowedValue(List<Double> list) {
		Collections.sort(list);
		double min = list.get(0);
		double max = list.get(list.size() - 1);
		return min + notPassedTill * (max - min);
	}
}
