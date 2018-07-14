package dataUnits;

import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import dataUnits.IDataUnit.XMLTags;
import io.IReadableXML;

public interface IDataUnitElemental extends IDataUnit {
	public String getKey();
	public String getValue();
	
	public static IReadableXML createFromXML(XMLEventReader reader) {
		boolean Ok = true, goOn = true;

		IDataUnitElemental result = null;
		String elemType = null;
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						if (event.asStartElement().getName().getLocalPart().equalsIgnoreCase(XMLTags.elementaryDoc.getTagText())) {
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (XMLTags.elementaryType.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
					                elemType = attribute.getValue();
					                break;
				                }
				            }
				            switch (elemType) {
				            	case DataUnitElementalBase.typeTag:
				            		result = (IDataUnitElemental) DataUnitElementalBase.createFromXML(reader);
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
}
