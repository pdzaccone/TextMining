package analyzers;

import java.util.TreeSet;

import analysis.IAnalysisResult;
import analysis.MetadataModification;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import dataUnits.IDataUnitElemental;
import utils.Languages;
import utils.Pair;
import utils.PairAnalysisResults;
import utils.RegexHelper;
import utils.WeightedMap;
import utils.WeightedObject;

/**
 * This analyzer class defines the language of the document.
 * 
 * @author Pdz
 *
 */
public class LanguageAnalyzer extends LanguageAnalyzerDefault {
	
	private static final double LANGUAGE_RECOGNITION_PRECISION = 0.8;
	
	public LanguageAnalyzer(boolean overwrite) {
		super(overwrite);
	}

	@Override
	public PairAnalysisResults feed(IDataUnitElemental input) {
		//TODO As I now know, the ElementalUnit "meta" has keywords like Stellennummer, but the rest of the words 
		//TODO are always in the language of the document. Should be processed somehow. Maybe this tag should be ignored for a while?
		PairAnalysisResults result = new PairAnalysisResults();
		if (!input.getValue().isEmpty()) {
			WeightedMap counter = new WeightedMap();
			for (Languages lang : Languages.values()) {
				if (input.getValue().contains("Unternehmenskultur") || input.getValue().contains("unternehmenskultur")) {
					int zzz = 0;
					zzz++;
				}
				long count = RegexHelper.split(RegexHelper.patternWords, input.getValue())
						.stream().filter(str -> lang.containsKeyword(str)).count();
				if (count != 0) {
					counter.add(lang.getTagText(), (int) count);
				}
			}
			if (counter.isEmpty()) {
				counter.add(Languages.unknown.getTagText(), 1);
			}
			TreeSet<WeightedObject> weights = counter.getWeights();
			if (!weights.isEmpty() && weights.last().getWeight() >= LANGUAGE_RECOGNITION_PRECISION) {
				result.addResult(new Pair<>(new MetadataModification(AnalysisTypes.language, weights.last()), shouldOverwrite), IAnalyzer.SEND_UP);
				MetadataModification resLocal = new MetadataModification(AnalysisTypes.language, weights.last());
				resLocal.markAsFinal();
				result.addResult(new Pair<>(resLocal, shouldOverwrite), IAnalyzer.LOCAL);
			}
		}
		if (result.isEmpty()) {
			result.addResult(new Pair<>(new MetadataModification(AnalysisTypes.language, new WeightedObject(Languages.unknown.getTagText(), 1)),
						shouldOverwrite), IAnalyzer.SEND_UP);
			MetadataModification resLocal = new MetadataModification(AnalysisTypes.language, new WeightedObject(Languages.unknown.getTagText(), 1));
			resLocal.markAsFinal();
			result.addResult(new Pair<>(resLocal, shouldOverwrite), IAnalyzer.LOCAL);
		}
		return result;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		PairAnalysisResults result = new PairAnalysisResults();
		WeightedMap counter = new WeightedMap();
		boolean shouldIgnore = false;
		for (IAnalysisResult analysis : input.getAnalysisResults(AnalysisTypes.language)) {
			TreeSet<WeightedObject> langs = ((MetadataModification) analysis).getData();
			if (langs.size() == 1) {
				counter.add(langs.last().getData(), 1);
			} else {
				shouldIgnore = true;
				break;
			}
		}
		if (!shouldIgnore) {
			MetadataModification temp = new MetadataModification(AnalysisTypes.language, counter.getWeights());
			temp.markAsFinal();
			result.addResult(new Pair<>(temp, shouldOverwrite), IAnalyzer.LOCAL);
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
}