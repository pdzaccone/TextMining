package dataUnits;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import crawlers.ICrawler;
import io.IReadableXML;

public interface IDataUnitDoc extends IDataUnit {
	
	public static IReadableXML createFromXML(XMLEventReader reader) {
		boolean Ok = true, goOn = true;

		IDataUnitDoc result = null;
		String docType = null;
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						if (event.asStartElement().getName().getLocalPart().equalsIgnoreCase(XMLTags.singleDoc.getTagText())) {
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (XMLTags.docType.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
					                docType = attribute.getValue();
					                break;
				                }
				            }
				            switch (docType) {
				            	case DocumentBase.typeTag:
				            		result = (IDataUnitDoc) DocumentBase.createFromXML(reader);
				            		if (result == null) {
				            			Ok = false;
				            		}
				            		break;
				            }
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						goOn = false;
						event = reader.nextEvent();
						break;
						
					default:
						event = reader.nextEvent();
						break;
				}
			}
		}
		catch (Exception e) {
			Ok = false;
		}
		if (!Ok) {
			result = null;
		}
		return result;
	}
	
	public void addData(String tag, IDataUnitElemental input);
	public IDataUnitDoc applyCrawler(ICrawler converter);
	public Set<String> getAllTags();
	public IDataUnitElemental getData(String tag);

	public int getID();
	public void setID(int id);

	public List<String> getCategoriesMap();
}
