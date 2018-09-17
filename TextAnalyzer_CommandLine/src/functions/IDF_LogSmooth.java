package functions;

import java.util.List;

import utils.WeightedMap;

/**
 * Specific implementation of {@link IFunctionIDF}, calculating IDF as a smoothed logarithmical function of other terms' weights 
 * @author Pdz
 *
 */
public class IDF_LogSmooth implements IFunctionIDF {

	@Override
	public double calculate(String term, List<WeightedMap> inputCorpus) {
		long count = inputCorpus.stream().filter(val -> val.containsKey(term) && val.get(term) != 0).count();
		return Math.log((double)inputCorpus.size() / (double)count);
	}
}