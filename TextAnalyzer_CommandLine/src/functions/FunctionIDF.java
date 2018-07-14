package functions;

import java.util.List;

import utils.WeightedMap;

public interface FunctionIDF {
	public double calculate(String term, List<WeightedMap> inputCorpus);
}
