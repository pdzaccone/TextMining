package analyzers;

import javax.xml.stream.XMLEventReader;

import analysis.EmptyAnalysis;
import analysis.IAnalysisResult;
import analysis.MetadataModification;
import analysis.WeightsTable;
import analysis.WordTable;

public enum AnalysisTypes {
	none("", false),
	all("all", false),
	language("lang", true) {
		@Override
		public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
			return MetadataModification.createFromXML(reader, this);
		}
	},
	wordTable("wordTable", false),
	weights("weights", false),
	documentID("ID", true) {
		@Override
		public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
			return MetadataModification.createFromXML(reader, this);
		}
	},
	documentDateStart("dateStart", true) {
		@Override
		public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
			return MetadataModification.createFromXML(reader, this);
		}
	},
	weightVector("weightVector", false),
	weightMatrix("weightMatrix", false),
	category("category", true) {
		@Override
		public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
			return MetadataModification.createFromXML(reader, this);
		}
	};
	
	private final String tagText;
	private final boolean canBeWrittenToXML;

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

	private AnalysisTypes(String text, boolean writeOk) {
		this.tagText = text;
		this.canBeWrittenToXML = writeOk;
	}
	
	public String getTagText() {
		return this.tagText;
	}

	public boolean isValid() {
		return this != none && this != all;
	}

	public IAnalysisResult createAnalysisFromXML(XMLEventReader reader) {
		return new EmptyAnalysis();
	}

	public boolean writingToXMLSupported() {
		return this.canBeWrittenToXML;
	}
}
