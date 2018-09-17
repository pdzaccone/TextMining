package analyzers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.SparseRealVector;

import analysis.IAnalysisResult;
import analysis.IWeightedAnalysis;
import analysis.WordsMatrix;
import analysis.WordsVector;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import linearAlgebra.ITermsVector;
import linearAlgebra.TermsVectorApacheCommons;
import utils.Languages;
import utils.Pair;
import utils.PairAnalysisResults;

/**
 * This {@link IAnalyzer} prepares a term-document matrix to be used by clustering algorithms
 * @author Pdz
 *
 */
public class TDMatrixPreparator implements IDocAnalyzer, ICorpusAnalyzer {

	/**
	 * Internal storage with all words
	 */
	private Map<Languages, List<String>> words;
	
	/**
	 * List with all original documents
	 */
	private List<IDataUnitDoc> documents;
	
	/**
	 * Whether the {@link IAnalyzer} has been initialized successfully
	 */
	private boolean isInitialized;
	
	/**
	 * Whether this {@link IAnalyzer} should overwrite already existing results from previous analysis if they exist
	 */
	private final boolean shouldOverwrite;

	/**
	 * Constructor with parameter
	 * @param overwrite Whether this {@link IAnalyzer} should overwrite already existing results from previous analysis if they exist
	 */
	public TDMatrixPreparator(boolean overwrite) {
		this.words = new HashMap<>();
		this.documents = new ArrayList<>();
		this.shouldOverwrite = overwrite;
		this.isInitialized = false;
	}
	
	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		Objects.requireNonNull(input);
		this.documents.add(input);
		PairAnalysisResults results = new PairAnalysisResults();
		if (input.getAnalysisResults(AnalysisTypes.weights).size() == 1) {
			IWeightedAnalysis anResProper = (IWeightedAnalysis) input.getAnalysisResults(AnalysisTypes.weights).get(0);
			WordsVector matrix = new WordsVector(input.getID());
			for (Languages lang : anResProper.getLanguages()) {
				matrix.add(lang, createDataVector(input.getID(), lang, anResProper.getWeights(lang)));
			}
			results.addResult(new Pair<>(matrix, shouldOverwrite), IAnalyzer.SEND_UP);
		}
		return results;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitCorpus input) {
		PairAnalysisResults results = new PairAnalysisResults();
		WordsMatrix matrix = new WordsMatrix(this.words, this.documents);
		for (IAnalysisResult anRes : input.getAnalysisResults(AnalysisTypes.weightVector)) {
			matrix.addDocumentVector((WordsVector) anRes);
		}
		matrix.markAsFinal();
		results.addResult(new Pair<>(matrix, shouldOverwrite), IAnalyzer.LOCAL);
		return results;
	}

	@Override
	public void initialize(IDataUnitCorpus data) {
		if (data != null && data.getAnalysisResults(AnalysisTypes.weights).size() == 1) {
			IWeightedAnalysis corpusData = (IWeightedAnalysis) data.getAnalysisResults(AnalysisTypes.weights).get(0);
			for (Languages lang : corpusData.getLanguages()) {
				initVocabularlyForLanguage(lang, corpusData.getWeights(lang));
			}
			this.isInitialized = true;
		}
	}

	@Override
	public boolean isInitialized() {
		return this.isInitialized;
	}

	/**
	 * Creates terms vector from the provided input data
	 * @param id Vector ID
	 * @param language Language
	 * @param input Input data to populate resulting vector with
	 * @return Resulting {@link ITermsVector} object
	 */
	private ITermsVector createDataVector(int id, Languages language, Map<String, ? extends Number> input) {
		return new TermsVectorApacheCommons(id, this.words.get(language), input);
	}

	/**
	 * Updates internal vocabulary for a specific language
	 * @param language Language
	 * @param newData New terms
	 */
	private void initVocabularlyForLanguage(Languages language, Map<String, ? extends Number> newData) {
		List<String> allWords = words.get(language);
		if (allWords == null) {
			allWords = new ArrayList<>();
		}
		for (String term : newData.keySet()) {
			if (!allWords.contains(term)) {
				allWords.add(term);
			}
		}
		words.put(language, allWords);
	}
}
