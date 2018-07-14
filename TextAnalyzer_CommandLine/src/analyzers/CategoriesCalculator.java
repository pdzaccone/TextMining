package analyzers;

import java.util.Objects;

import analysis.IAnalysisResult;
import clustering.IClusterer;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import functions.IWeightsToDistancesConverter;
import utils.Pair;
import utils.PairAnalysisResults;

public class CategoriesCalculator implements IDocAnalyzer, ICorpusAnalyzer {

	private final IWeightsToDistancesConverter converter;
	private final IClusterer clusterer;
	private boolean isInitialized;
	private final boolean shouldOverwrite;
	
	public CategoriesCalculator(boolean overwrite, IWeightsToDistancesConverter inConverter, IClusterer inClusterer) {
		this.shouldOverwrite = overwrite;
		this.converter = inConverter;
		this.clusterer = inClusterer;
		this.isInitialized = false;
	}
	
	@Override
	public void initialize(IDataUnitCorpus input) {
		Objects.requireNonNull(input);
		boolean Ok = true;
		converter.initializeData(input);
		try {
			converter.prepareData();
		} catch (Exception e) {
			Ok = false;
		}
		if (Ok) {
			clusterer.initialize(converter.getData());
			clusterer.doClustering();
		}
		this.isInitialized = Ok;
	}

	@Override
	public boolean isInitialized() {
		return this.isInitialized;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		PairAnalysisResults results = new PairAnalysisResults();
		results.addResult(new Pair<>(clusterer.getCategories(input), shouldOverwrite), IAnalyzer.LOCAL);
		return results;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitCorpus input) {
		PairAnalysisResults results = new PairAnalysisResults();
		for (IAnalysisResult anRes : clusterer.getCategories()) {
			results.addResult(new Pair<>(anRes, shouldOverwrite), IAnalyzer.LOCAL);
		}
		return results;
	}
}