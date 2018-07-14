package functions;

import java.util.List;

import analysis.IMultilingual;
import analysis.WordsMatrix;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import utils.Languages;

public interface IWeightsToDistancesConverter extends IMultilingual {
	public void initializeData(IDataUnitCorpus input);
	public void prepareData() throws Exception;
	public List<String> getTerms(Languages lang);
	public List<IDataUnitDoc> getDocuments(Languages lang);
	public WordsMatrix getData();
	public void setMatrixFilter(IMatrixFilter input);
}
