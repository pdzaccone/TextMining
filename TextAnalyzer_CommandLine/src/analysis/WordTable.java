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
import functions.IFunctionIDF;
import functions.IFunctionTF;
import io.IWriterXML;
import io.XMLException;
import utils.Languages;
import utils.RegexHelper;
import utils.WeightedMap;
import utils.WeightedObject;

/**
 * This class is used during the TF-IDF calculation process
 * @author Pdz
 *
 */
public class WordTable implements IAnalysisResult, IMultilingual {

	/**
	 * This is a hard-coded list with "bad" words - terms that should be removed from the analysis at the very beginning
	 */
	private static final List<String> listBadWords = new ArrayList<>();
	
	/**
	 * Type of the {@link IAnalysisResult}
	 */
	private static final AnalysisTypes type = AnalysisTypes.wordTable;

	/**
	 * String constant for separating individual terms
	 */
	private static final String separatorTerms = ";";
	
	/**
	 * String constant for separating terms and their weights
	 */
	private static final String separatorParams = "-";

	/**
	 * Threshold parameter, controlling whether this {@link IAnalysisResult} should be saved to the XML file or not
	 */
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
	 * This method saved {@link WordTable} to the XML file
	 * <p>
	 * At the moment this functionality is not used
	 * @param reader Initialized XML-reader
	 * @return Resulting {@link IAnalysisResult} or null
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

	/**
	 * Converts provided {@link WeightsMap} object to string (to later save it to XML file)
	 * @param input {@link WeightedMap} to convert
	 * @return Resulting string
	 */
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

	/**
	 * Parses provided string to create a {@link WeightedMap} object
	 * @param input String to parse
	 * @return Resulting object or null
	 */
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
	
	/**
	 * Internal data
	 */
	private Map<Languages, WeightedMap> data;

	/**
	 * Whether this {@link IAnalysisResult} is complete for this session
	 */
	private boolean markedFinal;
	
	/**
	 * Constructor
	 */
	public WordTable() {
		this.data = new HashMap<>();
		this.markedFinal = false;
	}
	
	/**
	 * Copy constructor
	 * @param table A {@link WordTable} object to copy
	 */
	public WordTable(WordTable table) {
		this.data = new HashMap<>(table.data);
		this.markedFinal = table.isFinal();
	}
	
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
	
	/**
	 * Gets data for a specified language
	 * @param language Language
	 * @return Resulting weighted map (or an empty one)
	 */
	protected WeightedMap getData(Languages language) {
		if (this.data.containsKey(language)) {
			return this.data.get(language);
		}
		return new WeightedMap();
	}

	/**
	 * Uses gathered data to calculate TF, using the provided {@link IFunctionTF} function
	 * @param func Function to calculate TF
	 * @return Resulting {@link WeightsTable}
	 */
	public WeightsTable calculateTF(IFunctionTF func) {
		WeightsTable result = new WeightsTable();
		for (Languages lang : this.data.keySet()) {
			for (String key : getData(lang).keySet()) {
				result.add(lang, key, func.calculate(key, getData(lang)));
			}
		}
		return result;
	}
	
	/**
	 * Uses gathered data to calculate IDF, using the provided {@link IFunctionTF} function
	 * @param func Function to calculate IDF
	 * @param input List of {@link WordTable} objects for all documents in a document corpus
	 * @return Resulting {@link WeightsTable}
	 */
	public WeightsTable calculateIDF(IFunctionIDF func, List<WordTable> input) {
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

	/**
	 * Adds string of terms to the {@link WordTable} object as it is stored in original data
	 * @param lang Language
	 * @param terms String of terms, separated by {@link RegexHelper#patternWords}
	 */
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

	/**
	 * Adds individual term to the internal storage
	 * @param lang Language
	 * @param term Term
	 */
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

	/**
	 * Adds data to the current {@link WordTable}
	 * @param input Data being added
	 */
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
	
	/**
	 * Adds new terms to the internal data, replaces existing weights with the new ones
	 * @param lang Language
	 * @param input Map with weighted terms
	 */
	private void add(Languages lang, WeightedMap input) {
		if (input != null) {
			this.data.put(lang, input);
		}
	}

	/**
	 * Temporarily not in use (see {@link WordTable#createFromXML})
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
