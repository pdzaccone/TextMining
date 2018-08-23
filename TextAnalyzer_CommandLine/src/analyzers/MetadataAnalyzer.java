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
 * This class carries out text analysis by using a set of predefined patterns. These allow it to find such information
 * as document creation date, ID, etc
 * @author Pdz
 *
 */
public class MetadataAnalyzer implements IElementalAnalyzer {

	private static final Pattern patternID = Pattern.compile("(?<=(^|\\s))(\\w*)(?=($|\\s))");
	private static final Pattern patternDigit = Pattern.compile("\\d");
	private static final Pattern patternDate = Pattern.compile("\\d{1,2}\\.\\d{1,2}\\.\\d{4}");

	private final boolean shouldOverwrite;
	private boolean isInitialized;
	
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