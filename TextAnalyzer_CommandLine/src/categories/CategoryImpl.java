package categories;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import analysis.IAnalysisResult;
import analysis.IMultilingual;
import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import io.IWriterXML;
import io.XMLException;
import utils.Languages;

public class CategoryImpl implements ICategory, IMultilingual {
	
	public static final String typeTag = "categoryBase";

	public static ICategory createFromXML(XMLEventReader reader) {
		ICategory result = null;
		boolean readingKeywords = false;
		boolean Ok = true, goOn = true;
		String currentLang = "";
		String catName = "";
		Map<String, String> catKeywords = new HashMap<>();
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						String name = event.asStartElement().getName().getLocalPart();
						if (name.equalsIgnoreCase(ICategory.XMLTags.category.getTagText())) {
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (ICategory.XMLTags.name.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
					                currentLang = attribute.getValue();
				                }
				            }
						} else if (name.equalsIgnoreCase(ICategory.XMLTags.keywords.getTagText())) {
							readingKeywords = true;
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (ICategory.XMLTags.language.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
					                currentLang = attribute.getValue();
				                }
				            }
						}
						break;

					case XMLStreamConstants.CHARACTERS:
						String str = event.asCharacters().getData();
						if (readingKeywords) {
							catKeywords.put(currentLang, str);
							readingKeywords = false;
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
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
		if (Ok)
			result = new CategoryImpl();
			result.setName(catName);
			for (String lang : catKeywords.keySet()) {
				String[] keywords = catKeywords.get(lang).split(ICategory.keywordsSeparator);
				result.addKeywords(lang, Arrays.asList(keywords));
			}
		return result;
	}
	
	private String name;
	private Map<Languages, List<String>> keywords;
	
	public CategoryImpl() {
		init("");
	}

	public CategoryImpl(String catName) {
		init(catName);
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addKeywords(String lang, List<String> input) {
		Languages language = Languages.fromString(lang);
		if (this.keywords.containsKey(language)) {
			this.keywords.get(language).addAll(input);
		} else {
			this.keywords.put(language, input);
		}
	}

	@Override
	public boolean writeToXML(IWriterXML writer) {
		boolean Ok = true;
		try {
			writer.writeStartElement(ICategory.XMLTags.category.getTagText());
			writer.writeAttribute(ICategory.XMLTags.categoryType.getTagText(), typeTag);
			writer.writeAttribute(ICategory.XMLTags.name.getTagText(), this.name);
			for (Languages lang : this.keywords.keySet()) {
				writer.writeStartElement(ICategory.XMLTags.keywords.getTagText());
				writer.writeAttribute(ICategory.XMLTags.language.getTagText(), lang.getTagText());
				writer.writeData(generateString(lang));
				writer.writeEndElement();
			}
			writer.writeEndElement();
		} catch (XMLException e) {
			Ok = false;
		}
		return Ok;
	}

	@Override
	public Set<Languages> getLanguages() {
		return this.keywords.keySet();
	}

	private String generateString(Languages lang) {
		StringBuilder sb = new StringBuilder();
		for (String s : this.keywords.get(lang)) {
			sb.append(s + ICategory.keywordsSeparator);
		}
		return sb.toString();
	}

	private void init(String categoryName) {
		this.name = categoryName;
		this.keywords = new HashMap<>();
	}
}