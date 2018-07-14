package dataUnits;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import crawlers.ICrawler;
import io.IReadableXML;

public interface IDataUnitCorpus extends IDataUnit {
	
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
	
	public void addDocument(IDataUnitDoc input);
	public void addCorpus(IDataUnitCorpus obj);	
	public Collection<IDataUnitDoc> getDocuments();
	public IDataUnitCorpus applyCrawler(ICrawler converter);
	public int size();
}
