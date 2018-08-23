package analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import io.IWriterXML;
import io.XMLException;
import linearAlgebra.ITermsVector;
import utils.Languages;
import utils.ListMap;

public class CategoryImpl implements ICategory {
	
	public static final AnalysisTypes type = AnalysisTypes.categoryWords;
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
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						String name = event.asStartElement().getName().getLocalPart();
						if (name.equalsIgnoreCase(ICategory.XMLTags.category.getTagText())) {
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (ICategory.XMLTags.name.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
					                catName = attribute.getValue();
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
						event = reader.nextEvent();
						break;

					case XMLStreamConstants.CHARACTERS:
						String str = event.asCharacters().getData();
						if (readingKeywords) {
							catKeywords.put(currentLang, str);
						}
						event = reader.nextEvent();
						break;

					case XMLStreamConstants.END_ELEMENT:
						if (readingKeywords) {
							readingKeywords = false;
							event = reader.nextEvent();
						} else {
							goOn = false;
						}
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
		if (Ok) {
			result = new CategoryImpl();
			result.setName(catName);
			for (String lang : catKeywords.keySet()) {
				String[] keywords = catKeywords.get(lang).split(ICategory.keywordsSeparator);
				List<String> finalKeywords = new ArrayList<>();
				for (String keyword : keywords) {
					keyword = keyword.trim();
					if (!keyword.isEmpty()) {
						finalKeywords.add(keyword);
					}
				}
				result.addKeywords(lang, finalKeywords);
			}
		}
		return result;
	}
	
	private String name;
	private ListMap<Languages, String> keywords;
	private Map<Languages, ITermsVector> vectors;
	private boolean markedFinal; 
	
	public CategoryImpl() {
		init("");
	}

	public CategoryImpl(String catName) {
		init(catName);
	}
	
	private void init(String categoryName) {
		this.name = categoryName;
		this.keywords = new ListMap<>();
		this.vectors = new HashMap<>();
		this.markedFinal = false;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addKeyword(Languages lang, String input) {
		if (this.keywords.keySet().contains(lang)) {
			this.keywords.get(lang).add(input);
		} else {
			this.keywords.put(lang, input);
		}
	}

	@Override
	public void addKeywords(String lang, List<String> input) {
		Languages language = Languages.fromString(lang);
		if (this.keywords.keySet().contains(language)) {
			this.keywords.get(language).addAll(input);
		} else {
			this.keywords.put(language, input);
		}
	}

	@Override
	public List<String> getKeywords(Languages lang) {
		if (!this.keywords.keySet().contains(lang)) {
			return new ArrayList<>();
		}
		return this.keywords.get(lang);
	}

	@Override
	public ITermsVector getVector(Languages lang) {
		if (this.vectors.containsKey(lang)) {
			return this.vectors.get(lang);
		}
		return null;
	}

	@Override
	public void setVector(Languages lang, ITermsVector vector) {
		if (this.getLanguages().contains(lang)) {
			this.vectors.put(lang, vector);
		}
	}

	@Override
	public void update(ICategory category) {
		this.keywords.clear();
		for (Languages lang : category.getLanguages()) {
			this.keywords.put(lang, category.getKeywords(lang));
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

	@Override
	public AnalysisTypes getType() {
		return type;
	}

	@Override
	public void update(IDataUnit obj, boolean shouldOverwrite) {
		Objects.requireNonNull(obj);
		if (!shouldOverwrite && obj.analysisIsFinalized(getType())) {
			return;
		}
		if (this.isFinal() || obj.analysisIsFinalized(getType())) {
			obj.resetAnalysis(getType());
		}
		if (!obj.analysisIsFinalized(getType())) {
			obj.addAnalysis(getType(), this);
		}
	}

	@Override
	public void markAsFinal() {
		this.markedFinal = true;
	}

	@Override
	public boolean isFinal() {
		return this.markedFinal;
	}

	@Override
	public boolean isEmpty() {
		return this.keywords.isEmpty();
	}

	@Override
	public String getName() {
		return this.name;
	}
}