package analysis;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import io.IReadableXML;
import io.ISaveableXML;

public interface IAnalysisResult extends ISaveableXML, IReadableXML {
	
	public static enum XMLTags {
		language("lang"), 
		weights("weights");
				
		private final String tagText;
		
		private XMLTags(String text) {
			tagText = text;
		}

		public String getTagText() {
			return this.tagText;
		}
	}

	public static IAnalysisResult readXML(XMLEventReader reader) {
		IAnalysisResult result = null;
		boolean Ok = true, goOn = true;
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						AnalysisTypes type = AnalysisTypes.fromString(event.asStartElement().getName().getLocalPart());
						if (type.isValid()) {
							result = type.createAnalysisFromXML(reader); 
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						goOn = false;
						break;
						
					default:
						event = reader.nextEvent();
						break;
				}
			}
		}
		catch (XMLStreamException e) {
			Ok = false;
		}
		catch (Exception e) {
			Ok = false;
		}
		if (!Ok || result instanceof EmptyAnalysis) {
			result = null;
		}
		return result;
	}

	public AnalysisTypes getType();
	public void update(IDataUnit obj, boolean shouldOverwrite);
	public void markAsFinal();
	public boolean isFinal();
	public boolean isEmpty();
}