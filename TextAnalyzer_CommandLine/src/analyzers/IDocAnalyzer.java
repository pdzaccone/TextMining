package analyzers;

import dataUnits.IDataUnitDoc;
import utils.PairAnalysisResults;

public interface IDocAnalyzer extends IAnalyzer {
	public PairAnalysisResults feed(IDataUnitDoc input);
}
