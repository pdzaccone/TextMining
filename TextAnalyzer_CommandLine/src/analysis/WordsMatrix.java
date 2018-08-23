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

public class WordsMatrix implements IAnalysisResult, IMultilingual {

	private static final AnalysisTypes type = AnalysisTypes.weightMatrix;
	private static final double languageThreshold = 0;

	public static IAnalysisResult createFromXML(XMLEventReader reader, AnalysisTypes analysisTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	//Rows are words and columns are documents
	private Map<Languages, IDocTermMatrix> data;
	private Map<Languages, List<IDataUnitDoc>> documents;
	private Map<Languages, List<Integer>> documentIDs;
//	private Map<Languages, Map<String, Integer>> vocabularly;
	private Map<Languages, List<String>> vocabularly;
	private boolean markedFinal; 
	
//	public WordsMatrix(Map<Languages, Map<String, Integer>> words, List<IDataUnitDoc> docs) {
	public WordsMatrix(Map<Languages, List<String>> words, List<IDataUnitDoc> docs) {
		this.data = new HashMap<>();
		this.vocabularly = new HashMap<>(words);
		this.markedFinal = false;
		separateInputDocumentsByLanguage(docs);
	}
	
	public WordsMatrix(WordsMatrix input) {
		this.data = new HashMap<>(input.data);
		this.vocabularly = new HashMap<>(input.vocabularly);
		this.documents = new HashMap<>(input.documents);
		this.markedFinal = input.isFinal();
	}

	public void addDocumentVector(WordsVector input) {
		for (Languages lang : input.getLanguages()) {
			if (!this.documents.containsKey(lang) || !this.documentIDs.get(lang).contains(input.getID())) {
				continue;
			}
			int numberOfWords = this.vocabularly.get(lang).size();
			IDocTermMatrix matrix = this.data.get(lang);
			if (matrix == null) {
				matrix = new DTMatrixApacheCommons(numberOfWords, this.documents.get(lang).size());
			}
			matrix.setColumnVector(this.documentIDs.get(lang).indexOf(input.getID()), input.getData(lang));
			this.data.put(lang, matrix);
		}
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof WordsMatrixCorpus) {
//			return this.vocabularly.equals(((WordsMatrixCorpus)obj).vocabularly) 
//					&& this.data.equals(((WordsMatrixCorpus)obj).data)
//					&& this.documents.equals(((WordsMatrixCorpus)obj).data);
//		}
//		return false;
//	}
//	
//	@Override
//    public int hashCode() {
//		return 31 + (this.documents == null ? 0 : this.documents.hashCode()) 
//				+ (this.data == null ? 0 : this.data.hashCode())
//				+ (this.vocabularly == null ? 0 : this.vocabularly.hashCode());
//    }

	@Override
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

	public IteratorColumns getIteratorColumns(Languages lang) {
		IteratorColumns iter = null;
		if (this.data.containsKey(lang)) {
			iter = new IteratorColumns(this.data.get(lang).getColumnVectors()); 
		}
		return iter;
	}
	
	public IDocTermMatrix getDataMatrix(Languages lang) {
		return this.data.get(lang);
	}

	public int getColumnDimension(Languages lang) {
		return this.data.containsKey(lang) ? this.data.get(lang).getNumberDocs() : 0;
	}

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

	public List<String> getTerms(Languages lang) {
		return this.vocabularly.get(lang);
	}

	public List<IDataUnitDoc> getDocuments(Languages lang) {
		return this.documents.get(lang);
	}
	
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

	private boolean isLanguageShareHighEnough(MetadataModification input) {
		return input.getData().descendingIterator().next().getWeight() > languageThreshold;
	}
	
	public void updateDataMatrix(Languages lang, IDocTermMatrix input) throws Exception {
		if (this.data.containsKey(lang) && input != null) {
			List<Integer> removedRows = input.removeEmptyRows();
			this.data.get(lang).updateMatrix(input);
			for (int index = removedRows.size() - 1; index >= 0; index--) {
				this.vocabularly.get(lang).remove((int)removedRows.get(index));
			}
		}
	}

	//TODO Should completely rework this algorithm. Multiple languages in one object lead to parallel collections and it is absolutely not nice!
	public int getIndex(Languages lang, IDataUnitDoc doc) {
		if (this.documentIDs.containsKey(lang) && doc != null) {
			return this.documentIDs.get(lang).indexOf(doc.getID());
		}
		return IDataUnitDoc.DEFAULT_ID;
	}
	
	@Override
	public boolean isEmpty() {
		return this.data.isEmpty() || this.documents.isEmpty() || this.vocabularly.isEmpty();
	}

	public ITermsVector createVector(Languages lang, List<String> keywords, double defaultWeight) {
		if (keywords != null && this.getLanguages().contains(lang)) {
			Map<String, Double> weights = new HashMap<>();
			for (String keyword : keywords) {
				weights.put(keyword, defaultWeight);
			}
			return new TermsVectorApacheCommons(ITermsVector.DEFAULT_ID, this.vocabularly.get(lang), weights);
		}
		return null;
	}
}