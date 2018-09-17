package dataUnits;

import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import io.IReadableXML;

/**
 * Base interface for the elemental data block
 * @author Pdz
 *
 */
public interface IDataUnitElemental extends IDataUnit {
	
	/**
	 * Reads elemental data block from an XML-file
	 * @param reader Initialized XML-reader, with cursor set at the beginning of the data block
	 * @return Resulting elemental data block or null
	 */
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

	/**
	 * Gets tag, identifying this block within the document
	 * @return Tag value
	 */
	public String getKey();
	
	/**
	 * Gets contents of the data block 
	 * @return Contents
	 */
	public String getValue();	
}
