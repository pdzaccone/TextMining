package analyzers;

import javax.xml.stream.XMLEventReader;

import analysis.EmptyAnalysis;
import analysis.IAnalysisResult;
import analysis.MetadataModification;

/**
 * This enumeration holds all supported analysis types and provides a number of functions for them
 * @author Pdz
 *
 */
public enum AnalysisTypes {
	/**
	 * Helper-type
	 */
	none("", false),
	/**
	 * Helper-type
	 */
	all("all", false),
	/**
	 * Language data
	 */
	language("lang", true) {
		@Override
		public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
			return MetadataModification.createFromXML(reader, this);
		}
	},
	/**
	 * Word table
	 */
	wordTable("wordTable", false),
	/**
	 * Weights
	 */
	weights("weights", false),
	/**
	 * Document ID
	 */
	documentID("ID", true) {
		@Override
		public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
			return MetadataModification.createFromXML(reader, this);
		}
	},
	/**
	 * Document creation date (as found in the original document)
	 */
	documentDateStart("dateStart", true) {
		@Override
		public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
			return MetadataModification.createFromXML(reader, this);
		}
	},
	/**
	 * Vector with weights
	 */
	weightVector("weightVector", false),
	/**
	 * Matrix with weights
	 */
	weightMatrix("weightMatrix", false),
	/**
	 * Category
	 */
	category("category", true) {
		@Override
		public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
			return MetadataModification.createFromXML(reader, this);
		}
	},
	/**
	 * Category with words
	 */
	categoryWords("categoryWords", false);
	
	/**
	 * Text used in tags
	 */
	private final String tagText;
	/**
	 * Whether or not the {@link IAnalysisResult} of this type should be stored to the XML-file
	 */
	private final boolean canBeWrittenToXML;

	/**
	 * Parses the provided text string to identify the corresponding {@link AnalysisTypes}
	 * @param name String (tag) to parse
	 * @return Resulting analysis type or {@link AnalysisTypes#none}
	 */
	public static AnalysisTypes fromString(String name) {
		AnalysisTypes result = AnalysisTypes.none;
		for (AnalysisTypes type : values()) {
			if (type != none && type.getTagText().equalsIgnoreCase(name)) {
				result = type;
				break;
			}
		}
		return result;
	}

	/**
	 * Constructor with parameters
	 * @param text Tag name
	 * @param writeOk Whether this analysis can be saved to an XML-file  
	 */
	private AnalysisTypes(String text, boolean writeOk) {
		this.tagText = text;
		this.canBeWrittenToXML = writeOk;
	}
	
	/**
	 * Gets tag text
	 * @return Tag text
	 */
	public String getTagText() {
		return this.tagText;
	}

	/**
	 * Whether this analysis type is real or a "helper-type"
	 * @return True - real analysis type
	 */
	public boolean isValid() {
		return this != none && this != all;
	}

	/**
	 * Default method for reading analysis data from the XML-file. Most of {@link AnalysisTypes} 
	 * have individual implementations of this method
	 * @param reader Initialized XML-reader
	 * @return Empty analysis result
	 */
	public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
		return new EmptyAnalysis();
	}

	/**
	 * Whether the corresponding {@link IAnalysisResult} can be saved to an XML-file
	 * @return
	 */
	public boolean canBeWrittenToXML() {
		return this.canBeWrittenToXML;
	}
}
