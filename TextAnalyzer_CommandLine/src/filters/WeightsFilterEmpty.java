package filters;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WeightsFilterEmpty implements IWeightsFilter {

	@Override
	public double calculateMinimumAllowedValue(List<Double> list) {
		Objects.requireNonNull(list);
		Collections.sort(list);
		return list.get(0);
	}
}
