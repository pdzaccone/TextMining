package crawlers;

import analyzers.IAnalyzer;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import dataUnits.IDataUnitElemental;
import utils.PairDataUnitAnalysis;

/**
 * This interface defines main methods of the crawler object.
 * <p> Crawler is responsible for navigating the document corpus and analyzing its single elements 
 * @author Pdz
 *
 */
public interface ICrawler {
	/**
	 * Starts crawling the provided document corpus
	 * @param input Document corpus to process
	 * @return Resulting document corpus
	 */
	public IDataUnitCorpus crawl(IDataUnitCorpus input);
	
	/**
	 * Processes single document
	 * @param input Document to process
	 * @return Resulting analysis data
	 */
	public PairDataUnitAnalysis crawl(IDataUnitDoc input);
	
	/**
	 * Processes single elemental data unit
	 * @param input Elemental data unit to process
	 * @return Resulting analysis data
	 */
	public PairDataUnitAnalysis crawl(IDataUnitElemental input);
	
	/**
	 * Adds another {@link IAnalyzer} object to the {@link ICrawler}
	 * @param analyzer Analyzer object
	 * @param crawlNumber Number of crawl when this analyzer should be activated
	 */
	public void addAnalyzer(IAnalyzer analyzer, int crawlNumber);
	
	/**
	 * Generates empty document to be filled by the analyzer
	 * @param input Document "template"
	 * @return Resulting document
	 */
	public IDataUnitDoc generateDataUnitDocLevel(IDataUnitDoc input);
}
