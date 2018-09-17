package analyzers;

import dataUnits.IDataUnitDoc;
import utils.PairAnalysisResults;

/**
 * This interface is used to mark those {@link IAnalyzer} objects that also work on document-level 
 * @author Pdz
 *
 */
public interface IDocAnalyzer extends IAnalyzer {

	/**
	 * Feeds data to the {@link IAnalyzer}
	 * @param input Input data
	 * @return Object with analysis results, together with instructions on how to use them 
	 */
	public PairAnalysisResults feed(IDataUnitDoc input);
}
