package functions;

import utils.WeightedMap;

/**
 * Specific implementation of {@link IFunctionTF}, calculating TF as a number of occurrences of term in a document
 * @author Pdz
 *
 */
public class TF_Count implements IFunctionTF {

	@Override
	public double calculate(String term, WeightedMap data) {
		return data.get(term);
	}
}