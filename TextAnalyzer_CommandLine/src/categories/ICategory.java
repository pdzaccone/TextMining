package categories;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import io.IReadableXML;
import io.ISaveableXML;

public interface ICategory extends ISaveableXML, IReadableXML {

	public static enum XMLTags {
		category("category"),
		categoryType("catType"),
		name("name"),
		language("lang"),
		keywords("keywords");
		
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
				            }
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						goOn = false;
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

	public void setName(String name);
	public void addKeywords(String lang, List<String> asList);
}
