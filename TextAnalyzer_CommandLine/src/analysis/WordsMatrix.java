package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.stream.XMLEventReader;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import dataUnits.IDataUnitDoc;
import io.IWriterXML;
import linearAlgebra.DTMatrixApacheCommons;
import linearAlgebra.IDocTermMatrix;
import linearAlgebra.ITermsVector;
import linearAlgebra.TermsVectorApacheCommons;
import utils.Languages;

/**
 * This class is responsible for composing and filling a Document-Term matrix
 * @author Pdz
 *
 */
public class WordsMatrix implements IAnalysisResult, IMultilingual {

	/**
	 * Type of the {@link IAnalysisResult}
	 */
	private static final AnalysisTypes type = AnalysisTypes.weightMatrix;
	
	/**
	 * This {@link IAnalysisResult} is not supposed to be saved to / loaded from an XML file, so this method remains a stub
	 * @param reader
	 * @param analysisTypes
	 * @return Always NULL
	 */
	public static IAnalysisResult createFromXML(XMLEventReader reader, AnalysisTypes analysisTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Internal data, rows are words and columns are documents 
	 */
	private Map<Languages, IDocTermMatrix> data;
	
	/**
	 * Internal list of all documents in the document corpus
	 */
	private Map<Languages, List<IDataUnitDoc>> documents;
	
	/**
	 * Parallel lists of document IDs
	 */
	private Map<Languages, List<Integer>> documentIDs;

	/**
	 * Vocabulary data
	 */
	private Map<Languages, List<String>> vocabulary;
	
	/**
	 * Whether this {@link IAnalysisResult} is complete for this session
	 */
	private boolean markedFinal; 

	/**
	 * Constructor with parameters
	 * @param words All terms
	 * @param docs All documents
	 */
	public WordsMatrix(Map<Languages, List<String>> words, List<IDataUnitDoc> docs) {
		this.data = new HashMap<>();
		this.vocabulary = new HashMap<>(words);
		this.markedFinal = false;
		separateInputDocumentsByLanguage(docs);
	}
	
	/**
	 * Copy constructor
	 * @param input {@link WordsMatrix} object to copy
	 */
	public WordsMatrix(WordsMatrix input) {
		this.data = new HashMap<>(input.data);
		this.vocabulary = new HashMap<>(input.vocabulary);
		this.documents = new HashMap<>(input.documents);
		this.markedFinal = input.isFinal();
	}

	/**
	 * Adds a single term vector to the matrix
	 * @param input Term vector to add
	 */
	public void addDocumentVector(WordsVector input) {
		for (Languages lang : input.getLanguages()) {
			if (!this.documents.containsKey(lang) || !this.documentIDs.get(lang).contains(input.getID())) {
				continue;
			}
			int numberOfWords = this.vocabulary.get(lang).size();
			IDocTermMatrix matrix = this.data.get(lang);
			if (matrix == null) {
				matrix = new DTMatrixApacheCommons(numberOfWords, this.documents.get(lang).size());
			}
			matrix.setColumnVector(this.documentIDs.get(lang).indexOf(input.getID()), input.getData(lang));
			this.data.put(lang, matrix);
		}
	}

	@Override
	/**
	 * This class does not support writing to XML, so this method is a stub
	 */
	public boolean writeToXML(IWriterXML writer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AnalysisTypes getType() {
		return type;
	}

	@Override
	public void update(IDataUnit obj, boolean shouldOverwrite) {
		Objects.requireNonNull(obj);
		if (!shouldOverwrite && obj.analysisIsFinalized(getType())) {
			return;
		}
		if (this.isFinal() || obj.analysisIsFinalized(getType())) {
			obj.resetAnalysis(getType());
		}
		if (!obj.analysisIsFinalized(getType())) {
			obj.addAnalysis(getType(), this);
		}
		obj.resetAnalysis(AnalysisTypes.weightVector);
	}

	@Override
	public Set<Languages> getLanguages() {
		return this.data.keySet();
	}

	/**
	 * Gets terms matrix for a specified language
	 * @param lang Language
	 * @return Resulting matrix
	 */
	public IDocTermMatrix getDataMatrix(Languages lang) {
		return this.data.get(lang);
	}

	/**
	 * Gets number of documents for a specified language
	 * @param lang Language
	 * @return Resulting number of columns / documents
	 */
	public int getColumnDimension(Languages lang) {
		return this.data.containsKey(lang) ? this.data.get(lang).getNumberDocs() : 0;
	}

	/**
	 * Gets number of terms for a specified language
	 * @param lang Language
	 * @return Resulting number of rows / terms
	 */
	public int getRowDimension(Languages lang) {
		return this.data.containsKey(lang) ? this.data.get(lang).getNumberTerms() : 0;
	}

	@Override
	public void markAsFinal() {
		this.markedFinal = true;
	}

	@Override
	public boolean isFinal() {
		return this.markedFinal;
	}

	/**
	 * Gets all terms for a specified language
	 * @param lang Language
	 * @return List with terms
	 */
	public List<String> getTerms(Languages lang) {
		return this.vocabulary.get(lang);
	}

	/**
	 * Gets all documents for a specified language
	 * @param lang Language
	 * @return List with documents
	 */
	public List<IDataUnitDoc> getDocuments(Languages lang) {
		return this.documents.get(lang);
	}
	
	/**
	 * This method does the preliminary sorting of documents, dividing them by language and filling some of the internal lists
	 * @param docs Input documents
	 */
	private void separateInputDocumentsByLanguage(List<IDataUnitDoc> docs) {
		this.documents = new HashMap<>();
		this.documentIDs = new HashMap<>();
		for (IDataUnitDoc doc : docs) {
			List<IAnalysisResult> analysisResults = doc.getAnalysisResults(AnalysisTypes.weights);
			if (analysisResults == null || analysisResults.size() != 1) {
				continue;
			}
			for (Languages lang : ((WeightsTable)analysisResults.get(0)).getLanguages()) {
				List<IDataUnitDoc> listForLang = this.documents.get(lang);
				List<Integer> idsForLang = this.documentIDs.get(lang);
				if (listForLang == null) {
					listForLang = new ArrayList<>();
				}
				if (idsForLang == null) {
					idsForLang = new ArrayList<>();
				}
				listForLang.add(doc);
				idsForLang.add(doc.getID());
				this.documents.put(lang, listForLang);
				this.documentIDs.put(lang, idsForLang);
			}
		}
	}

	/**
	 * Updates matrix with new data
	 * @param lang Language
	 * @param input New matrix data
	 * @throws Exception
	 */
	public void updateDataMatrix(Languages lang, IDocTermMatrix input) throws Exception {
		if (this.data.containsKey(lang) && input != null) {
			List<Integer> removedRows = input.removeEmptyRows();
			this.data.get(lang).updateMatrix(input);
			for (int index = removedRows.size() - 1; index >= 0; index--) {
				this.vocabulary.get(lang).remove((int)removedRows.get(index));
			}
		}
	}

	/**
	 * Gets document index
	 * @param lang Language
	 * @param doc Document to identify
	 * @return Resulting index or {@link IDataUnitDoc#DEFAULT_ID}
	 */
	public int getIndex(Languages lang, IDataUnitDoc doc) {
		if (this.documentIDs.containsKey(lang) && doc != null) {
			return this.documentIDs.get(lang).indexOf(doc.getID());
		}
		return IDataUnitDoc.DEFAULT_ID;
	}
	
	@Override
	public boolean isEmpty() {
		return this.data.isEmpty() || this.documents.isEmpty() || this.vocabulary.isEmpty();
	}

	/**
	 * Creates term vector based on a provided list of keywords. This method is used when importing categories
	 * @param lang Language
	 * @param keywords List of terms
	 * @param defaultWeight Default terms weight
	 * @return Resulting terms-vector or null
	 */
	public ITermsVector createVector(Languages lang, List<String> keywords, double defaultWeight) {
		if (keywords != null && this.getLanguages().contains(lang)) {
			Map<String, Double> weights = new HashMap<>();
			for (String keyword : keywords) {
				weights.put(keyword, defaultWeight);
			}
			return new TermsVectorApacheCommons(ITermsVector.DEFAULT_ID, this.vocabulary.get(lang), weights);
		}
		return null;
	}
}