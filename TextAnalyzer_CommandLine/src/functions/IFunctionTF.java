package functions;

import utils.WeightedMap;

/**
 * Base interface for the TF-function
 * @author Pdz
 *
 */
public interface IFunctionTF {

	/**
	 * Calculates TF for the given term, based on provided weighted map of terms for the current document
	 * @param term Term to analyze
	 * @param data All terms in document as a weight map
	 * @return Calculated TF value
	 */
	public double calculate(String term, WeightedMap data);
}