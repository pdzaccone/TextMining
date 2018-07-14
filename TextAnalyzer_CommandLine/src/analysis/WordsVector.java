package analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.stream.XMLEventReader;

import org.apache.commons.math3.linear.SparseRealVector;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import io.IWriterXML;
import linearAlgebra.ITermsVector;
import utils.Languages;

public class WordsVector implements IAnalysisResult, IMultilingual {

	private static final AnalysisTypes type = AnalysisTypes.weightVector;

	public static IAnalysisResult createFromXML(XMLEventReader reader, AnalysisTypes analysisTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	private final int docID;
	private boolean markedFinal;
//	private Map<Languages, SparseRealVector> data;
	private Map<Languages, ITermsVector> data;
	
	public WordsVector(int docID) {
		this.docID = docID;
		this.data = new HashMap<>();
		this.markedFinal = false;
	}
	
	public WordsVector(WordsVector input) {
		this.docID = input.getID();
		this.data = new HashMap<>(input.data);
		this.markedFinal = input.isFinal();
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof WordsMatrixDoc) {
//			return this.docID == ((WordsMatrixDoc)obj).docID && this.data.equals(((WordsMatrixDoc)obj).data);
//		}
//		return false;
//	}
//	
//	@Override
//    public int hashCode() {
//		return 31 + this.docID + (this.data == null ? 0 : this.data.hashCode());
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
	}

	@Override
	public Set<Languages> getLanguages() {
		return this.data.keySet();
	}

	public int getID() {
		return this.docID;
	}

//	public void add(Languages language, SparseRealVector dataVector) {
//		this.data.put(language, dataVector);
//	}
//
//	public SparseRealVector getData(Languages language) {
//		return this.data.get(language);
//	}

	public void add(Languages language, ITermsVector dataVector) {
		this.data.put(language, dataVector);
	}

	public ITermsVector getData(Languages language) {
		return this.data.get(language);
	}

	@Override
	public void markAsFinal() {
		this.markedFinal = true;
	}

	@Override
	public boolean isFinal() {
		return this.markedFinal;
	}
}