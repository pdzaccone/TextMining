package dataUnits;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import crawlers.ICrawler;
import io.IReadableXML;
import utils.Languages;

/**
 * Base interface for all documents
 * @author Pdz
 *
 */
public interface IDataUnitDoc extends IDataUnit {
	
	/**
	 * Default document ID, used for all "temporary" documents
	 */
	public static final int DEFAULT_ID = -1;

	/**
	 * Loads document data from an XML file
	 * @param reader Initialized XML-reader, with cursor at the beginning of a document
	 * @return Resulting document or null
	 */
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
	
	/**
	 * Adds new elemental data block to the document
	 * @param tag Tag of the elemental data block
	 * @param input Data block itself
	 */
	public void addData(String tag, IDataUnitElemental input);
	
	/**
	 * Analyzes document with help of a provided crawler
	 * @param converter Crawler to use
	 * @return Resulting (analyzed) document
	 */
	public IDataUnitDoc applyCrawler(ICrawler converter);
	
	/**
	 * Gets all tags that the document has (each tag identifies 1 internal elemental block)
	 * @return Set of all tags
	 */
	public Set<String> getAllTags();
	
	/**
	 * Gets elemental data block with a specified tag
	 * @param tag Tag
	 * @return Resulting elemental data block
	 */
	public IDataUnitElemental getData(String tag);
	
	/**
	 * Gets document ID
	 * @return ID
	 */
	public int getID();
	
	/**
	 * Sets new document ID
	 * @param id ID
	 */
	public void setID(int id);
	
	//TODO Get rid of both these methods - they do not fit into architecture
	/**
	 * Gets all categories for this document
	 * <p> This method should be removed as it does not fit into the architecture
	 * @return List with categories
	 */
	public List<String> getCategoriesMap();
	
	/**
	 * Gets main document language
	 * <p> This method should be removed as it does not fit into the architecture
	 * @return Main document language
	 */
	public Languages getMainLanguage();
}
