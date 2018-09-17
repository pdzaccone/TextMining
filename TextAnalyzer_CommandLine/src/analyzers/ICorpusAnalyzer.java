package analyzers;

import dataUnits.IDataUnitCorpus;
import utils.PairAnalysisResults;

/**
 * This interface is used to mark those {@link IAnalyzer} objects that also work on corpus-level 
 * @author Pdz
 *
 */
public interface ICorpusAnalyzer extends IAnalyzer {
	
	/**
	 * Feeds data to the {@link IAnalyzer}
	 * @param input Input data
	 * @return Object with analysis results, together with instructions on how to use them 
	 */
	public PairAnalysisResults feed(IDataUnitCorpus input);
}
