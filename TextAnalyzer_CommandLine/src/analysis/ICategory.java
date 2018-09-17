package analysis;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import io.IReadableXML;
import io.ISaveableXML;
import linearAlgebra.ITermsVector;
import utils.Languages;

/**
 * This is a base interface for objects of type "category" 
 * @author Pdz
 *
 */
public interface ICategory extends IAnalysisResult, IMultilingual, ISaveableXML, IReadableXML {

	/**
	 * This enumeration defines all tags that are used when writing / reading category data to / from an XML file 
	 * @author Pdz
	 *
	 */
	public static enum XMLTags {
		category("category"),
		categoryType("catType"),
		name("name"),
		language("lang"),
		keywords("keywords"),
		categories("categories");
		
		private final String text;
		
		private XMLTags(String descr) {
			this.text = descr;
		}

		public final String getTagText() {
			return text;
		}
	}
	
	/**
	 * String constant, used to identify individual keywords within a category
	 */
	public static final String keywordsSeparator = ", ";

	/**
	 * This is a general method, responsible for reading objects of type {@link ICategory}
	 * @param reader An initialized reader-class
	 * @return Resulting object of type {@link IReadableXML} or null
	 */
	public static IReadableXML createFromXML(XMLEventReader reader) {
		boolean Ok = true, goOn = true;

		ICategory result = null;
		String categoryType = null;
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						if (event.asStartElement().getName().getLocalPart().equalsIgnoreCase(XMLTags.category.getTagText())) {
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (XMLTags.categoryType.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
					                categoryType = attribute.getValue();
					                break;
				                }
				            }
				            switch (categoryType) {
				            	case CategoryImpl.typeTag:
				            		result = CategoryImpl.createFromXML(reader);
				            		break;
				            		
			            		default:
									event = reader.nextEvent();
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

	public void update(ICategory category);
	
	/**
	 * Gets a category name
	 * @return Resulting name
	 */
	public String getName();
	
	/**
	 * Sets a category name
	 * @param name New name
	 */
	public void setName(String name);
	
	/**
	 * Adds a keyword to a category
	 * @param lang Keyword language
	 * @param input Keyword itself
	 */
	public void addKeyword(Languages lang, String input);
	
	/**
	 * Adds multiple keywords to a category
	 * @param lang Keywords language
	 * @param asList List of keywords
	 */
	public void addKeywords(String lang, List<String> asList);
	
	/**
	 * Gets all keywords for a specified language
	 * @param lang Language
	 * @return Resulting list of keywords or an empty list if no keywords could be found
	 */
	public List<String> getKeywords(Languages lang);
	
	/**
	 * Gets a list of keywords for a specified language in form of vector 
	 * @param lang Language
	 * @return Resulting vector or null
	 */
	public ITermsVector getVector(Languages lang);
	
	/**
	 * Sets keywords in a form of vector
	 * @param lang Keywords' language
	 * @param vector Vector with keywords
	 */
	public void setVector(Languages lang, ITermsVector vector);
}
