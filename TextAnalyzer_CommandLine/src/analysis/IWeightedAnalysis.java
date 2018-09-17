package analysis;

import java.util.Map;

import filters.IWeightsFilter;
import utils.Languages;

/**
 * This interface defines several classes, responsible for setting weights to terms within the document and / or within the document corpus 
 * @author Pdz
 *
 */
public interface IWeightedAnalysis extends IMultilingual {
	
	/**
	 * Filters out weights based on the {@link IWeightsFilter} object
	 * @param filter Filter to use when filtering out "unneeded" weights
	 * @return Results of filtering
	 */
	public IAnalysisResult filter(IWeightsFilter filter);
	
	/**
	 * Uses previously calculated TF and IDF to produce TF-IDF from them
	 * @param corpusData Input data
	 * @return Resulting analysis result
	 */
	public IAnalysisResult calculateTFIDF(IWeightedAnalysis corpusData);
	
	/**
	 * Gets weights data for a specified language
	 * @param lang Language
	 * @return Weights map (empty if no weights can be found)
	 */
	public Map<String, ? extends Number> getWeights(Languages lang);
}