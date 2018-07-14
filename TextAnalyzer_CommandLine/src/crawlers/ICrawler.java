package crawlers;

import analyzers.IAnalyzer;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import dataUnits.IDataUnitElemental;
import utils.PairDataUnitAnalysis;

public interface ICrawler {
	public IDataUnitCorpus crawl(IDataUnitCorpus input);
	public PairDataUnitAnalysis crawl(IDataUnitDoc input);
	public PairDataUnitAnalysis crawl(IDataUnitElemental input);
	public void addAnalyzer(IAnalyzer analyzer, int crawlNumber);
//	public IAnalysisResult getCurrentAnalysis();
//	public void resetAnalysis();
	public IDataUnitDoc generateDataUnitDocLevel(IDataUnitDoc input);
}
