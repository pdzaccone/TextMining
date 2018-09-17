package functions;

import java.util.List;

import analysis.IMultilingual;
import analysis.WordsMatrix;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import utils.Languages;

/**
 * This interface defines a set of methods to be used for data preparation.
 * <p> Its name is not really correct and I do not remember why exactly this name was chosen initially
 * @author Pdz
 *
 */
public interface IWeightsToDistancesConverter extends IMultilingual {
	
	/**
	 * Initializes internal data
	 * @param input Document corpus
	 */
	public void initializeData(IDataUnitCorpus input);
	
	/**
	 * Processes internal data according to the specific implementation
	 * @throws Exception
	 */
	public void prepareData() throws Exception;
	
	/**
	 * Gets all terms for the specific language
	 * @param lang Language
	 * @return Resulting list of terms
	 */
	public List<String> getTerms(Languages lang);
	
	/**
	 * Gets all documents for the specific language 
	 * @param lang Language
	 * @return Resulting list of documents
	 */
	public List<IDataUnitDoc> getDocuments(Languages lang);
	
	/**
	 * Gets internal terms-documents matrix
	 * @return Data matrix
	 */
	public WordsMatrix getData();
	
	/**
	 * Sets matrix filter
	 * @param input Matrix filter
	 */
	public void setMatrixFilter(IMatrixFilter input);
}
