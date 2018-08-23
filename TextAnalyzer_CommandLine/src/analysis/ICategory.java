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

public interface ICategory extends IAnalysisResult, IMultilingual, ISaveableXML, IReadableXML {

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
	
	public static final String keywordsSeparator = ", ";
	public static final String noCategory = "none";

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
	public String getName();
	public void setName(String name);
	public void addKeyword(Languages lang, String input);
	public void addKeywords(String lang, List<String> asList);
	public List<String> getKeywords(Languages lang);
	public ITermsVector getVector(Languages lang);
	public void setVector(Languages lang, ITermsVector vector);
}
