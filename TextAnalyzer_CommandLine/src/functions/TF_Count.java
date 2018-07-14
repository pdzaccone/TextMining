package functions;

import utils.WeightedMap;

public class TF_Count implements FunctionTF {

	@Override
	public double calculate(String term, WeightedMap data) {
		return data.get(term);
	}
}