package analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.stream.XMLEventReader;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import io.IWriterXML;
import linearAlgebra.ITermsVector;
import utils.Languages;

/**
 * This class stores terms data for a single document 
 * @author Pdz
 *
 */
public class WordsVector implements IAnalysisResult, IMultilingual {

	/**
	 * Type of the {@link IAnalysisResult}
	 */
	private static final AnalysisTypes type = AnalysisTypes.weightVector;

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
	 * ID of the corresponding document
	 */
	private final int docID;
	
	/**
	 * Whether this {@link IAnalysisResult} is complete for this session
	 */
	private boolean markedFinal;

	/**
	 * Internal data
	 */
	private Map<Languages, ITermsVector> data;
	
	/**
	 * Constructor
	 * @param docID Document ID
	 */
	public WordsVector(int docID) {
		this.docID = docID;
		this.data = new HashMap<>();
		this.markedFinal = false;
	}
	
	/**
	 * Copy constructor
	 * @param input {@link WordsVector} object to copy
	 */
	public WordsVector(WordsVector input) {
		this.docID = input.getID();
		this.data = new HashMap<>(input.data);
		this.markedFinal = input.isFinal();
	}
	
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

	/**
	 * Gets document ID
	 * @return
	 */
	public int getID() {
		return this.docID;
	}

	/**
	 * Adds more terms for a specified language
	 * @param language Language
	 * @param dataVector New terms as a vector
	 */
	public void add(Languages language, ITermsVector dataVector) {
		this.data.put(language, dataVector);
	}

	/**
	 * Gets terms-vector for a specified language
	 * @param language Language
	 * @return Terms-vector
	 */
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

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty();
	}
}