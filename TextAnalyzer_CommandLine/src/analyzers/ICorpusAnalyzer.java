package analyzers;

import dataUnits.IDataUnitCorpus;
import utils.PairAnalysisResults;

public interface ICorpusAnalyzer extends IAnalyzer {
	public PairAnalysisResults feed(IDataUnitCorpus input);
}
