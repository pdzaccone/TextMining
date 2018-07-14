package analyzers;

import dataUnits.IDataUnitElemental;
import utils.PairAnalysisResults;

public interface IElementalAnalyzer extends IAnalyzer {
	public PairAnalysisResults feed(IDataUnitElemental input);
}
