package analyzers;

import dataUnits.IDataUnitElemental;
import utils.PairAnalysisResults;

/**
 * This interface is used to mark those {@link IAnalyzer} objects that also work on elemental level 
 * @author Pdz
 *
 */
public interface IElementalAnalyzer extends IAnalyzer {
	
	/**
	 * Feeds data to the {@link IAnalyzer}
	 * @param input Input data
	 * @return Object with analysis results, together with instructions on how to use them 
	 */
	public PairAnalysisResults feed(IDataUnitElemental input);
}
