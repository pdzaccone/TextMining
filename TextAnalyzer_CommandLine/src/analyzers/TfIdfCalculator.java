package analyzers;

import analysis.IAnalysisResult;
import analysis.IWeightedAnalysis;
import analysis.WeightsTable;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import filters.IWeightsFilter;
import utils.Pair;
import utils.PairAnalysisResults;

/**
 * This class finalizes the words weights normalization by updating data for all documents
 * @author Pdz
 *
 */
public class TfIdfCalculator implements IDocAnalyzer {

	private IWeightedAnalysis corpusData;
	private IWeightsFilter filter;
	private boolean isInitialized;
	private final boolean shouldOverwrite;
	
	public TfIdfCalculator(boolean overwrite, IWeightsFilter filter) {
		this.corpusData = new WeightsTable();
		this.filter = filter;
		this.shouldOverwrite = overwrite;
		this.isInitialized = false;
	}
	
	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		PairAnalysisResults result = new PairAnalysisResults();
		for (IAnalysisResult anRes : input.getAnalysisResults(AnalysisTypes.weights)) {
			IAnalysisResult anResCalc = ((IWeightedAnalysis) anRes).calculateTFIDF(corpusData);
			if (filter != null) {
				anResCalc = ((IWeightedAnalysis) anResCalc).filter(this.filter);
			}
			anResCalc.markAsFinal();
			result.addResult(new Pair<>(anResCalc, shouldOverwrite), IAnalyzer.LOCAL);
		}
		return result;
	}
	
	@Override
	public void initialize(IDataUnitCorpus data) {
		if (data != null && !data.getAnalysisResults(AnalysisTypes.weights).isEmpty()
			&& data.getAnalysisResults(AnalysisTypes.weights).get(0) instanceof IWeightedAnalysis) {
			this.corpusData = (IWeightedAnalysis) data.getAnalysisResults(AnalysisTypes.weights).get(0);
			this.isInitialized = true;
		}
	}

	@Override
	public boolean isInitialized() {
		return this.isInitialized;
	}
}
