package functions;

import java.util.List;

import utils.WeightedMap;

/**
 * Base interface for the IDF-function
 * @author Pdz
 *
 */
public interface IFunctionIDF {
	
	/**
	 * Calculates IDF for the given term, based on the provided weighted map of terms for the whole document corpus
	 * @param term Term to analyze
	 * @param inputCorpus All terms in document corpus as a weight map 
	 * @return Calculated IDF value
	 */
	public double calculate(String term, List<WeightedMap> inputCorpus);
}
