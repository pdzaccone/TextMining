package functions;

import java.util.List;

import utils.WeightedMap;

public class IDF_LogSmooth implements FunctionIDF {

	@Override
	public double calculate(String term, List<WeightedMap> inputCorpus) {
		long count = inputCorpus.stream().filter(val -> val.containsKey(term) && val.get(term) != 0).count();
		return Math.log(inputCorpus.size() / count);
	}
}