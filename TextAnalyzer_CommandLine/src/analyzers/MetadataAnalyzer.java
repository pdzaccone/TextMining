package analyzers;

import java.util.Collection;
import java.util.regex.Pattern;

import analysis.EmptyAnalysis;
import analysis.IAnalysisResult;
import analysis.MetadataModification;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitElemental;
import dataUnits.RawDataTags;
import utils.Pair;
import utils.PairAnalysisResults;
import utils.RegexHelper;
import utils.WeightedMap;

/**
 * This {@link IAnalyzer} carries out text analysis by using a set of predefined patterns. These allow it to find such information
 * as document creation date, ID, etc
 * @author Pdz
 *
 */
public class MetadataAnalyzer implements IElementalAnalyzer {

	/**
	 * Regex pattern for recognition of document ID
	 */
	private static final Pattern patternID = Pattern.compile("(?<=(^|\\s))(\\w*)(?=($|\\s))");

	/**
	 * Regex pattern for digit recognition
	 */
	private static final Pattern patternDigit = Pattern.compile("\\d");

	/**
	 * Regex pattern for date recognition
	 */
	private static final Pattern patternDate = Pattern.compile("\\d{1,2}\\.\\d{1,2}\\.\\d{4}");

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
	public MetadataAnalyzer(boolean overwrite) {
		this.shouldOverwrite = overwrite;
		this.isInitialized = false;
	}
	
	@Override
	public PairAnalysisResults feed(IDataUnitElemental input) {
		PairAnalysisResults result = new PairAnalysisResults();
		if (RawDataTags.metadata.getTagText().equalsIgnoreCase(input.getKey())) {
			IAnalysisResult resDateStart = checkForPattern(patternDate, AnalysisTypes.documentDateStart, input.getValue());
			resDateStart.markAsFinal();
			result.addResult(new Pair<>(resDateStart, shouldOverwrite), IAnalyzer.SEND_UP);
			IAnalysisResult resDocID = checkForComplexPattern(patternID, patternDigit, AnalysisTypes.documentID, input.getValue());
			resDocID.markAsFinal();
			result.addResult(new Pair<>(resDocID, shouldOverwrite), IAnalyzer.SEND_UP);
		}
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

	/**
	 * This method checks provided text for a specific pattern and returns resulting information in a form of {@link MetadataModification}
	 * @param pattern Pattern to search for
	 * @param type Type of analysis result to generate as a result
	 * @param input Input text to analyze
	 * @return Resulting {@link IAnalysisResult} or an {@link EmptyAnalysis}
	 */
	private IAnalysisResult checkForPattern(Pattern pattern, AnalysisTypes type, String input) {
		Collection<String> split = RegexHelper.split(pattern, input);
		WeightedMap counter = new WeightedMap();
		for (String str : split) {
			counter.add(str, 1);
		}
		if (!counter.isEmpty()) {
			return new MetadataModification(type, counter.getWeights()); 
		}
		return new EmptyAnalysis();
	}

	//TODO Temporary method until I understand how to recognize the ID properly
	//TODO ID recognition does not work properly for text blocks like "12"
	/**
	 * This method is designed explicitly to recognize document ID
	 * @param pattern Pattern to use for a 1st-level parsing
	 * @param patternInternal Pattern to use for a 2nd-level parsing
	 * @param type Type of {@link IAnalysisResult} to generate
	 * @param input Input data to analyze
	 * @return Resulting {@link IAnalysisResult} or an {@link EmptyAnalysis}
	 */
	private IAnalysisResult checkForComplexPattern(Pattern pattern, Pattern patternInternal, AnalysisTypes type, String input) {
		Collection<String> split = RegexHelper.split(pattern, input);
		WeightedMap counter = new WeightedMap();
		for (String str : split) {
			Collection<String> split1 = RegexHelper.split(patternInternal, str);
			if (!split1.isEmpty()) {
				counter.add(str, 1);
			}
		}
		if (!counter.isEmpty()) {
			return new MetadataModification(type, counter.getWeights()); 
		}
		return new EmptyAnalysis();
	}
}