package analyzers;

import analysis.MetadataModification;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import dataUnits.IDataUnitElemental;
import utils.Languages;
import utils.Pair;
import utils.PairAnalysisResults;
import utils.WeightedObject;

/**
 * This analyzer class defines the language of the document.
 * 
 * @author Pdz
 *
 */
public class LanguageAnalyzerDefault implements IElementalAnalyzer, IDocAnalyzer {
	
	protected boolean shouldOverwrite;
	protected boolean isInitialized;
	
	public LanguageAnalyzerDefault(boolean overwrite) {
		this.shouldOverwrite = overwrite;
		this.isInitialized = false;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitElemental input) {
		//TODO As I now know, the ElementalUnit "meta" has keywords like Stellennummer, but the rest of the words 
		//TODO are always in the language of the document. Should be processed somehow. Maybe this tag should be ignored for a while?
		PairAnalysisResults result = new PairAnalysisResults();
		result.addResult(new Pair<>(new MetadataModification(AnalysisTypes.language, new WeightedObject(Languages.unknown.getTagText(), 1)),
				shouldOverwrite), IAnalyzer.SEND_UP);
		MetadataModification resLocal = new MetadataModification(AnalysisTypes.language, new WeightedObject(Languages.unknown.getTagText(), 1));
		resLocal.markAsFinal();
		result.addResult(new Pair<>(resLocal, shouldOverwrite), IAnalyzer.LOCAL);
		return result;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		PairAnalysisResults result = new PairAnalysisResults();
		MetadataModification temp = new MetadataModification(AnalysisTypes.language, new WeightedObject(Languages.unknown.getTagText(), 1));
		temp.markAsFinal();
		result.addResult(new Pair<>(temp, shouldOverwrite), IAnalyzer.LOCAL);
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