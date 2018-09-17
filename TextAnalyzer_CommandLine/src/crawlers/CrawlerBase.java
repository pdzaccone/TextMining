package crawlers;

import java.util.ArrayList;
import java.util.List;

import analysis.IAnalysisResult;
import analyzers.IAnalyzer;
import analyzers.ICorpusAnalyzer;
import analyzers.IDocAnalyzer;
import analyzers.IElementalAnalyzer;
import dataUnits.DataUnitElementalBase;
import dataUnits.DocumentBase;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import dataUnits.IDataUnitElemental;
import dataUnits.RawDataTags;
import utils.ListMap;
import utils.Pair;
import utils.PairAnalysisResults;
import utils.PairDataUnitAnalysis;

/**
 * Base {@link ICrawler} class
 * @author Pdz
 *
 */
public class CrawlerBase implements ICrawler {

	/**
	 * Array with all analyzers
	 */
	private ListMap<Integer, IAnalyzer> analyzers;
	
	/**
	 * Document corpus
	 */
	private IDataUnitCorpus data;
	
	/**
	 * Number of the current crawl
	 */
	private int currentCrawl;
	
	/**
	 * Constructor without parameters
	 */
	public CrawlerBase() {
		this.analyzers = new ListMap<>();
	}
	
	@Override
	public IDataUnitCorpus crawl(IDataUnitCorpus input) {
		this.data = input;
		for (Integer crawlNum : analyzers.keySet()) {
			analyzers.get(crawlNum).stream().forEach(val -> val.initialize(this.data));
			currentCrawl = crawlNum;
			data = data.applyCrawler(this);
			analyzers.get(currentCrawl).stream().filter(an -> an instanceof ICorpusAnalyzer)
												.filter(an -> an.isInitialized())
												.forEach(an -> {
													PairAnalysisResults anRes = ((ICorpusAnalyzer)an).feed(data);
													anRes.updateLocal(data);
												});
		}
		return data;
	}

	@Override
	public PairDataUnitAnalysis crawl(IDataUnitDoc input) {
		IDataUnitDoc resultDoc = input.applyCrawler(this);
		List<Pair<IAnalysisResult, Boolean>> resultsAn = new ArrayList<>();
		analyzers.get(currentCrawl).stream().filter(an -> an instanceof IDocAnalyzer)
											.filter(an -> an.isInitialized())
											.forEach(an -> {
												PairAnalysisResults anRes = ((IDocAnalyzer)an).feed(resultDoc);
												anRes.updateLocal(resultDoc);
												resultsAn.addAll(anRes.getResultsToSendUp());
											});
		return new PairDataUnitAnalysis(resultDoc, resultsAn);
	}

	@Override
	public PairDataUnitAnalysis crawl(IDataUnitElemental input) {
		if (!RawDataTags.link.getTagText().equalsIgnoreCase(input.getKey())) {
			IDataUnitElemental resultData = new DataUnitElementalBase(input.getKey(), input.getValue());
			resultData.addAllAnalysis(input.getAllAnalysisResults());
			List<Pair<IAnalysisResult, Boolean>> resultsAn = new ArrayList<>();
			analyzers.get(currentCrawl).stream().filter(an -> an instanceof IElementalAnalyzer)
												.filter(an -> an.isInitialized())
												.forEach(an -> {
													PairAnalysisResults anRes = ((IElementalAnalyzer)an).feed(resultData);
													anRes.updateLocal(resultData);
													resultsAn.addAll(anRes.getResultsToSendUp());
												});
			return new PairDataUnitAnalysis(resultData, resultsAn);
		}
		return new PairDataUnitAnalysis(input, new ArrayList<>());
	}

	@Override
	public void addAnalyzer(IAnalyzer analyzer, int crawlNumber) {
		this.analyzers.put(crawlNumber, analyzer);
	}

	@Override
	public IDataUnitDoc generateDataUnitDocLevel(IDataUnitDoc input) {
		return new DocumentBase(input.getID());
	}
}
