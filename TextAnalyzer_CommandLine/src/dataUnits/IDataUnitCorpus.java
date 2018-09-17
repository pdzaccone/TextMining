package dataUnits;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import crawlers.ICrawler;
import io.IReadableXML;

/**
 * Interface with main methods for the document corpus 
 * @author Pdz
 *
 */
public interface IDataUnitCorpus extends IDataUnit {
	
	/**
	 * Loads document corpus from the XML file
	 * @param reader Initialized XML-reader with cursor at the beginning of the document corpus
	 * @return Resulting document corpus or null
	 */
	public static IReadableXML createFromXML(XMLEventReader reader) {
		boolean Ok = true, goOn = true;

		IDataUnitCorpus result = null;
		String corpusType = null;
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						if (event.asStartElement().getName().getLocalPart().equalsIgnoreCase(XMLTags.corpus.getTagText())) {
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (XMLTags.corpusType.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
					                corpusType = attribute.getValue();
					                break;
				                }
				            }
				            switch (corpusType) {
				            	case CorpusImpl.typeTag:
				            		result = (IDataUnitCorpus) CorpusImpl.createFromXML(reader);
				            		break;
				            }
						}
						event = reader.nextEvent();
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
		catch (Exception e) {
			Ok = false;
		}
		if (!Ok) {
			result = null;
		}
		return result;
	}
	
	/**
	 * Adds new document to the document corpus
	 * @param input Document to add
	 */
	public void addDocument(IDataUnitDoc input);
	
	/**
	 * Adds data from the provided document corpus to the current one
	 * @param obj Document corpus to insert
	 */
	public void addCorpus(IDataUnitCorpus obj);
	
	/**
	 * Gets a list of all documents
	 * @return List with all {@link IDataUnitDoc} objects
	 */
	public Collection<IDataUnitDoc> getDocuments();
	
	/**
	 * Starts data analysis by applying a crawler to the document corpus
	 * @param converter Crawler object to use
	 * @return Resulting (analyzed) document corpus
	 */
	public IDataUnitCorpus applyCrawler(ICrawler converter);
	
	/**
	 * Gets number of documents in a document corpus
	 * @return Number of documents
	 */
	public int size();
}
