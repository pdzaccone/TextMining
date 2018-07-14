package analysis;

import java.util.Map;
import java.util.Set;

import filters.IWeightsFilter;
import utils.Languages;

public interface IWeightedAnalysis extends IMultilingual {
	public IAnalysisResult filter(IWeightsFilter filter);
	public IAnalysisResult calculateTFIDF(IWeightedAnalysis corpusData);
	public Map<String, ? extends Number> getWeights(Languages lang);
}