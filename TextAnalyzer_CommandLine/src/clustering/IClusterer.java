package clustering;

import java.util.Map;

import analysis.IAnalysisResult;
import analysis.ICategory;
import analysis.WordsMatrix;
import dataUnits.IDataUnitDoc;

/**
 * This interface introduces a clustering algorithm
 * @author Pdz
 *
 */
public interface IClusterer {
	
	/**
	 * Begins clustering process
	 * @throws Exception
	 */
	public void doClustering() throws Exception;
	
	/**
	 * Gets resulting categories for a provided document
	 * @param input Document to find the categories for
	 * @return Resulting {@link IAnalysisResult} object with categories-data
	 */
	public IAnalysisResult getCategories(IDataUnitDoc input);
	
	/**
	 * Initializes a clusterer
	 * @param data Input data in form of words matrix
	 * @param categories Input set of categories
	 */
	public void initialize(WordsMatrix data, Map<String, ICategory> categories);
}
