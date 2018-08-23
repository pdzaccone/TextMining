package analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import functions.FunctionIDF;
import functions.FunctionTF;
import io.IWriterXML;
import io.XMLException;
import utils.Languages;
import utils.RegexHelper;
import utils.WeightedMap;
import utils.WeightedObject;

public class WordTable implements IAnalysisResult, IMultilingual {

	private static final List<String> listBadWords = new ArrayList<>();
	
	private static final AnalysisTypes type = AnalysisTypes.wordTable;

	private static final String separatorTerms = ";";
	private static final String separatorParams = "-";

	private static final long thresholdSaveXML = 0;

	static {	
		Collections.addAll(listBadWords, "und", "du", "der", "die", "zu", "sind", "hast", "sie", "im", "von", "für", "zur", "zu", "mit", 
				"and", "you", "to", "a", "the", "we", "das", "des", "of", "our", "bis", "vom", "wir", "uns", "bei", "for", "sich", "unsere",
				"unser", "bei", "this", "bist", "will", "haben", "when", "would", "if", "sowie", "über", "ein", "eine", "vor",
				"oder", "durch", "ihr", "ihre", "ab", "only", "den", "am", "als", "not", "einer", "dich", "deinen", "deine", "ist", "damit",
				"unter", "deren", "um", "einem", "unserer", "be", "those", "is", "who", "there", "as", "an", "are", "your", "they",
				"what", "that", "by", "or", "on", "it", "into", "these", "then", "out", "in", "which", "should", "much", "more", "up",
				"with", "too", "through", "have", "can", "has", "therefore", "their", "all", "but", "most", "from", "about", "than", "dir", "wie",
				"unseren", "ihnen");
	}
	
	/**
	 * At the moment this functionality is not used, because the algorithm cannot distinguish between 
	 * new and previously generated data. Thus temporarily switched off
	 * @param reader
	 * @return
	 */
	public static IAnalysisResult createFromXML(XMLEventReader reader) {
		WordTable result = null;
		boolean Ok = true, goOn = true;
		
		Languages currentLang = Languages.unknown;
		String anData = null;
		Map<Languages, String> tempData = new HashMap<>();
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						if (event.asStartElement().getName().getLocalPart().equalsIgnoreCase(type.getTagText())) {
							event = reader.nextEvent();
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				                Attribute attribute = iterator.next();
				                if (XMLTags.language.getTagText().equalsIgnoreCase(attribute.getName().toString())) {
				                	if (Languages.contains(attribute.getValue())) {
						                currentLang = Languages.fromString(attribute.getValue());
				                	}
				                }
				            }
						} else {
							goOn = false;
						}
						break;

					case XMLStreamConstants.CHARACTERS:
						anData = event.asCharacters().getData();
						break;

					case XMLStreamConstants.END_ELEMENT:
						if (anData != null) {
							tempData.put(currentLang, anData);
							currentLang = Languages.unknown;
							anData = null;
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
			result = new WordTable();
			for (Languages lang : tempData.keySet()) {
				WeightedMap converted = convertFromString(tempData.get(lang));
				if (converted == null) {
					Ok = false;
					break;
				}
				result.add(lang, converted);
			}
			result.markAsFinal();
		}
		if (!Ok) {
			result = null;
		}
		return result;
	}

	private static String convertToString(WeightedMap input) {
		StringBuilder sb = new StringBuilder();
		TreeSet<WeightedObject> tree = new TreeSet<>();
		for (String term : input.keySet()) {
			tree.add(new WeightedObject(term, input.get(term)));
		}
		Iterator<WeightedObject> it = tree.descendingIterator();
		while (it.hasNext()) {
			WeightedObject obj = it.next();
			sb.append(String.format("%s %s %d%s ", obj.getData(), WordTable.separatorParams, obj.getWeight(), WordTable.separatorTerms));
		}
		return sb.toString();
	}

	private static WeightedMap convertFromString(String input) {
		WeightedMap result = new WeightedMap();
		boolean Ok = true;
		if (input == null || input.isEmpty()) {
			Ok = false;
		} else {
			String[] strs = input.split(separatorTerms);
			try {
				if (strs != null) {
					for (String singleObj : strs) {
						String[] finalSplit = singleObj.split(separatorParams);
						if (finalSplit != null && finalSplit.length == 2) {
							result.add(finalSplit[0], Integer.parseInt(finalSplit[1]));
						}
					}
				}
			} catch (NumberFormatException e) {
				Ok = false;
			}
		}
		if (!Ok) {
			result = null;
		}
		return result;
	}
	
	private Map<Languages, WeightedMap> data;
	private boolean markedFinal;
	
	public WordTable() {
		this.data = new HashMap<>();
		this.markedFinal = false;
	}
	
	public WordTable(WordTable table) {
		this.data = new HashMap<>(table.data);
		this.markedFinal = table.isFinal();
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof WordTable) {
//			return this.data.equals(((WordTable)obj).data);
//		}
//		return false;
//	}
//	
//	@Override
//    public int hashCode() {
//		return 31 + (this.data == null ? 0 : this.data.hashCode());
//    }
	
	@Override
	public AnalysisTypes getType() {
		return AnalysisTypes.wordTable;
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
	
	protected WeightedMap getData(Languages language) {
		if (this.data.containsKey(language)) {
			return this.data.get(language);
		}
		return new WeightedMap();
	}
	
	public WeightsTable calculateTF(FunctionTF func) {
		WeightsTable result = new WeightsTable();
		for (Languages lang : this.data.keySet()) {
			for (String key : getData(lang).keySet()) {
				result.add(lang, key, func.calculate(key, getData(lang)));
			}
		}
		return result;
	}
	
	public WeightsTable calculateIDF(FunctionIDF func, List<WordTable> input) {
		WeightsTable result = new WeightsTable();
		for (Languages lang : this.data.keySet()) {
			List<WeightedMap> filteredInput = input.stream().map(val -> val.getData(lang))
					.filter(val -> val != null && !val.isEmpty()).collect(Collectors.toList());
			for (String key : getData(lang).keySet()) {
				result.add(lang, key, func.calculate(key, filteredInput));
			}
		}
		return result;
	}

	public void addRaw(Languages lang, String terms) {
		for (String s : RegexHelper.split(RegexHelper.patternWords, terms)) {
			if (s.isEmpty() || s.length() == 1) {
				continue;
			}
			if (!listBadWords.contains(s)) {
				add(lang, s);
			}
		}
	}

	public void add(Languages lang, String term) {
		WeightedMap newMap = null;
		if (!this.data.containsKey(lang)) {
			newMap = new WeightedMap();
		} else {
			newMap = this.data.get(lang);
		}
		newMap.add(term, 1);
		this.data.put(lang, newMap);
	}

	public void add(WordTable input) {
		for (Languages lang : Languages.values()) {
			WeightedMap dataForLanguage = input.getData(lang);
			if (!dataForLanguage.isEmpty()) {
				if (this.data.containsKey(lang)) {
					this.getData(lang).add(dataForLanguage);
				}
				else {
					this.data.put(lang, dataForLanguage);
				}
			}
		}
	}
	
	/* 
	 * Replaces existing values with new ones
	 */
	private void add(Languages lang, WeightedMap input) {
		if (input != null) {
			this.data.put(lang, input);
		}
	}

	/**
	 * Temporarily not in use (see {@link WordTable.createFromXML})
	 */
	@Override
	public boolean writeToXML(IWriterXML writer) {
		boolean Ok = true;
		try {
			long count = this.data.entrySet().stream().mapToInt(val -> val.getValue().size()).sum();
			if (count < thresholdSaveXML) {
				for (Languages lang : this.data.keySet()) {
					writer.writeStartElement(getType().getTagText());
					writer.writeAttribute(XMLTags.language.getTagText(), lang.getTagText());
					writer.writeData(convertToString(this.data.get(lang)));
					writer.writeEndElement();
				}
			}
		} catch (XMLException e) {
			Ok = false;
		}
		return Ok;
	}

	@Override
	public Set<Languages> getLanguages() {
		return this.data.keySet();
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
		return this.data.isEmpty();
	}
}
