package functions;

import utils.WeightedMap;

/**
 * Specific implementation of {@link IFunctionTF}, calculating TF as a number of occurrences of term in a document, adjusted according to the document size
 * @author Pdz
 *
 */
public class TF_CountAdjusted implements IFunctionTF {

	@Override
	public double calculate(String term, WeightedMap data) {
		return data.get(term) / data.size();
	}
}