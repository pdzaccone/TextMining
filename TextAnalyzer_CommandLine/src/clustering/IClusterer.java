package clustering;

import java.util.List;

import analysis.IAnalysisResult;
import analysis.WordsMatrix;
import dataUnits.IDataUnitDoc;
import functions.IWeightsToDistancesConverter;

public interface IClusterer {
	public void doClustering();
	public IAnalysisResult getCategories(IDataUnitDoc input);
	public List<IAnalysisResult> getCategories();
	public void initialize(WordsMatrix data);
}
