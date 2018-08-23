package clustering;

import java.util.Map;

import analysis.IAnalysisResult;
import analysis.ICategory;
import analysis.WordsMatrix;
import dataUnits.IDataUnitDoc;

public interface IClusterer {
	public void doClustering() throws Exception;
	public IAnalysisResult getCategories(IDataUnitDoc input);
	public void initialize(WordsMatrix data, Map<String, ICategory> categories);
}
