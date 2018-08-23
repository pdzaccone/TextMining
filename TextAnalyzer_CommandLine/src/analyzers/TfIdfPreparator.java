package analyzers;

import java.util.ArrayList;
import java.util.List;

import analysis.IAnalysisResult;
import analysis.MetadataModification;
import analysis.WeightsTable;
import analysis.WordTable;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import dataUnits.IDataUnitElemental;
import functions.FunctionIDF;
import functions.FunctionTF;
import utils.Languages;
import utils.Pair;
import utils.PairAnalysisResults;

/**
 * This class prepares data for the normalization with the help of the TF-IDF algorithm - it counts words and calculates their weights
 * with the help of the provided TF and IDF functions 
 * @author Pdz
 *
 */
public class TfIdfPreparator implements IElementalAnalyzer, IDocAnalyzer, ICorpusAnalyzer {

	private FunctionTF functionTF;
	private FunctionIDF functionIDF;
	private boolean isInitialized;
	private final boolean shouldOverwrite;
	private boolean multipleLangsPerDoc;
	
	public TfIdfPreparator(boolean overwrite, FunctionTF funcTF, FunctionIDF funcIDF, boolean multiLanguage) {
		this.shouldOverwrite = overwrite;
		this.functionTF = funcTF;
		this.functionIDF = funcIDF;
		this.isInitialized = false;
		this.multipleLangsPerDoc = multiLanguage;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitCorpus input) {
		PairAnalysisResults result = new PairAnalysisResults();
		WordTable table = new WordTable();
		List<WordTable> allDocs = new ArrayList<>();
		for (IAnalysisResult anRes : input.getAnalysisResults(AnalysisTypes.wordTable)) {
			if (anRes instanceof WordTable) {
				table.add((WordTable) anRes);
				allDocs.add(new WordTable((WordTable) anRes));
			}
		}
		WeightsTable weights = table.calculateIDF(functionIDF, allDocs);
		weights.markAsFinal();
		result.addResult(new Pair<>(weights, shouldOverwrite), IAnalyzer.LOCAL);
		return result;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		PairAnalysisResults result = new PairAnalysisResults();
		WordTable table = new WordTable();
		if (multipleLangsPerDoc) {
			input.getAnalysisResults(AnalysisTypes.wordTable).stream().filter(val -> val instanceof WordTable)
			 .forEach(val -> table.add((WordTable) val));
		} else {
			input.getAnalysisResults(AnalysisTypes.wordTable).stream().filter(val -> {
				if (val instanceof WordTable) {
					return ((WordTable)val).getLanguages().size() == 1 && ((WordTable)val).getLanguages().contains(input.getMainLanguage());
				}
				return false;
			}).forEach(val -> table.add((WordTable) val));
		}
		WeightsTable weights = table.calculateTF(functionTF);
		weights.markAsFinal();
		result.addResult(new Pair<>(weights, shouldOverwrite), IAnalyzer.LOCAL);
		result.addResult(new Pair<>(table, shouldOverwrite), IAnalyzer.SEND_UP);
		return result;
}

	@Override
	public PairAnalysisResults feed(IDataUnitElemental input) {
		PairAnalysisResults result = new PairAnalysisResults();
		Languages lang = Languages.unknown;
		if (!input.getAnalysisResults(AnalysisTypes.language).isEmpty()) {
			if (((MetadataModification)input.getAnalysisResults(AnalysisTypes.language).get(0)).getData().size() > 1) {
				int zzz = 0;
				zzz++;
			}
			lang = Languages.fromString(((MetadataModification)input.getAnalysisResults(AnalysisTypes.language)
					.get(0)).getData().last().getData());
		}
		WordTable table = new WordTable();
		table.addRaw(lang, input.getValue());
		result.addResult(new Pair<>(table, shouldOverwrite), IAnalyzer.SEND_UP);
		return result;
	}
	
	@Override
	public void initialize(IDataUnitCorpus data) {
		this.isInitialized = true;
	}
	
	@Override
	public boolean isInitialized() {
		return this.isInitialized;
	}
}