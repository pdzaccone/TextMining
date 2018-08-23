package dataUnits;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import analysis.IAnalysisResult;
import analyzers.AnalysisTypes;
import io.IReadableXML;
import io.IWriterXML;
import io.XMLException;
import utils.ListMap;

public class DataUnitElementalBase implements IDataUnitElemental {

	public static final String typeTag = "elemBase";
	
	public static IReadableXML createFromXML(XMLEventReader reader) {
		boolean Ok = true, goOn = true;
		DataUnitElementalBase result = null;
		ListMap<AnalysisTypes, IAnalysisResult> anData = new ListMap<>();
		String key = null, data = "";
		
		boolean readingAnalysis = false, readingData = false;

		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						String name = event.asStartElement().getName().getLocalPart();
						if (name.equalsIgnoreCase(XMLTags.elementaryDoc.getTagText())) {
							event = reader.nextEvent();
						} else if (name.equalsIgnoreCase(XMLTags.analysisData.getTagText())) {
							readingAnalysis = true;
							readingData = false;
							event = reader.nextEvent();
						} else if (name.equalsIgnoreCase(XMLTags.data.getTagText())) {
							readingAnalysis = false;
							readingData = true;
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (XMLTags.elementaryTag.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
					                key = attribute.getValue();
				                }
				            }
							event = reader.nextEvent();
						} else {
							if (readingAnalysis) {
								IAnalysisResult anRes = IAnalysisResult.readXML(reader);
								if (anRes != null && !anRes.isEmpty()) {
									anData.put(anRes.getType(), anRes);
								}
							}
						}
						break;

					case XMLStreamConstants.CHARACTERS:
						if (readingData) {
							data = data + event.asCharacters().getData().trim();
						}
						event = reader.nextEvent();
						break;
						
					case XMLStreamConstants.END_ELEMENT:
						String endName = event.asEndElement().getName().getLocalPart();
						if (endName.equalsIgnoreCase(XMLTags.elementaryDoc.getTagText())) {
							goOn = false;
						} else if (endName.equalsIgnoreCase(XMLTags.analysisData.getTagText())) {
							readingAnalysis = false;
							event = reader.nextEvent();
						} else if (endName.equalsIgnoreCase(XMLTags.data.getTagText())) {
							readingData = false;
							event = reader.nextEvent();
						}
						break;
				}
			}
		}
		catch (Exception e) {
			Ok = false;
		}
		if (Ok && !data.isEmpty() && !anData.isEmpty()) {
			result = new DataUnitElementalBase(key, data);
			result.addAllAnalysis(anData);
		}
		return result;
	}

	private String key;
	private String data;
	private ListMap<AnalysisTypes, IAnalysisResult> analysis;
	
	public DataUnitElementalBase(String key, String value) {
		this.key = key.trim();
		this.data = value.trim();
		this.analysis = new ListMap<>();
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof DataUnitElementalBase) {
//			return this.data.equals(((DataUnitElementalBase)obj).data)
//					&& this.key.equals(((DataUnitElementalBase)obj).key)
//					&& this.analysis.equals(((DataUnitElementalBase)obj).analysis);
//		}
//		return false;
//	}
//	
//	@Override
//    public int hashCode() {
//		return 31 + (this.analysis == null ? 0 : this.analysis.hashCode()) 
//				+ (this.data == null ? 0 : this.data.hashCode())
//				+ (this.key == null ? 0 : this.key.hashCode());
//    }

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return data;
	}

	@Override
	public boolean writeToXML(IWriterXML writer) {
		boolean Ok = true;
		try {
			writer.writeStartElement(XMLTags.elementaryDoc.getTagText());
			writer.writeAttribute(XMLTags.elementaryType.getTagText(), typeTag);
			if (!this.analysis.isEmpty()) {
				writer.writeStartElement(XMLTags.analysisData.getTagText());
				for (AnalysisTypes type : this.analysis.keySet()) {
					if (type.writingToXMLSupported()) {
						writer.writeStartElement(type.getTagText());
						for (IAnalysisResult res : this.analysis.get(type)) {
							if (!res.writeToXML(writer)) {
								Ok = false;
								break;
							}
						}
						writer.writeEndElement();
					}
				}
				writer.writeEndElement();
			}
			writer.writeStartElement(XMLTags.data.getTagText());
			writer.writeAttribute(XMLTags.elementaryTag.getTagText(), getKey());
			writer.writeData(data);
			writer.writeEndElement();
			writer.writeEndElement();
		} catch (XMLException e) {
			Ok = false;
		}
		return Ok;
	}

	@Override
	public void addAnalysis(AnalysisTypes type, IAnalysisResult input) {
		this.analysis.put(type, input);
	}

	@Override
	public void addAllAnalysis(ListMap<AnalysisTypes, IAnalysisResult> input) {
		this.analysis.putAll(input);
	}

	@Override
	public void addAllAnalysis(List<IAnalysisResult> input) {
		for (IAnalysisResult anRes : input) {
			this.analysis.put(anRes.getType(), anRes);
		}
	}

	@Override
	public List<IAnalysisResult> getAnalysisResults(AnalysisTypes type) {
		return analysis.get(type);
	}

	@Override
	public List<IAnalysisResult> getAllAnalysisResults() {
		return this.analysis.getAll();
	}
	
	@Override
	public void resetAnalysis(AnalysisTypes type) {
		this.analysis.removeType(type);
	}

	@Override
	public boolean analysisIsFinalized(AnalysisTypes type) {
		return this.analysis.keySet().contains(type) 
				&& this.analysis.get(type).size() == 1 
				&& this.analysis.get(type).get(0).isFinal();
	}

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty() && this.analysis.isEmpty();
	}
}
