package analysis;

import java.util.HashMap;
import java.util.Iterator;
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
import filters.IWeightsFilter;
import io.IWriterXML;
import utils.Languages;
import utils.WeightedObject;

public class WeightsTable implements IAnalysisResult, IWeightedAnalysis {
	
//	private static final long thresholdSaveXML = 50;
	private static final AnalysisTypes type = AnalysisTypes.weights;

	private static final String separatorTerms = ";";
	private static final String separatorParams = "-";

	/**
	 * At the moment this functionality is not used, because the algorithm cannot distinguish between 
	 * new and previously generated data. Thus temporarily switched off
	 * @param reader
	 * @return
	 */
	public static IAnalysisResult createFromXML(XMLEventReader reader) {
		WeightsTable result = null;
		boolean Ok = false, goOn = true;
		
		Languages currentLang = Languages.unknown;
		String anData = null;
		Map<Languages, String> tempData = new HashMap<>();
		
		try {
			boolean hasAttrib = false;
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						if (event.asStartElement().getName().getLocalPart().equalsIgnoreCase(type.getTagText())) {
							event = reader.nextEvent();
							Iterator<Attribute> iterator = event.asStartElement().getAttributes();
				            while (iterator.hasNext())
				            {
				            	hasAttrib = true;
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
						String tmp = event.asCharacters().getData().trim();
						if (!tmp.isEmpty()) {
							anData = tmp;
						}
						event = reader.nextEvent();
						break;

					case XMLStreamConstants.END_ELEMENT:
						String endName = event.asEndElement().getName().getLocalPart();
						if (endName.equalsIgnoreCase(type.getTagText())) {
							if (!hasAttrib) {
								goOn = false;
							} else {
								if (anData != null) {
									tempData.put(currentLang, anData);
									Ok = true;
									currentLang = Languages.unknown;
									anData = null;
								}
								hasAttrib = false;
							}
						}
						event = reader.nextEvent();
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
			result = new WeightsTable();
			for (Languages lang : tempData.keySet()) {
				Map<String, Double> converted = convertFromString(tempData.get(lang));
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

//	private static String convertToString(Map<String, Double> input) {
//		StringBuilder sb = new StringBuilder();
//		TreeSet<WeightedObject> tree = new TreeSet<>();
//		input.entrySet().stream().forEach(val -> tree.add(new WeightedObject(val.getKey(), val.getValue())));
//		Iterator<WeightedObject> it = tree.descendingIterator();
//		while (it.hasNext()) {
//			WeightedObject obj = it.next();
//			sb.append(String.format("%s %s %.2f%s ", obj.getData(), separatorParams, obj.getWeight(), separatorTerms));
//		}
//		return sb.toString();
//	}

	private static Map<String, Double> convertFromString(String input) {
		Map<String, Double> result = new HashMap<>();
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
							result.put(finalSplit[0], Double.parseDouble(finalSplit[1].trim().replace(",", ".")));
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

//	private Supplier<TreeSet<Double>> supplierDouble =
//		    () -> new TreeSet<Double>();
//
//	private Comparator<WeightedObject> comparator =
//		    Comparator.comparingDouble(WeightedObject::getWeight);
//	
//	private Supplier<TreeSet<WeightedObject>> supplierWeightedObject =
//		    () -> new TreeSet<WeightedObject>(comparator);

	private Map<Languages, Map<String, Double>> data;
	private boolean markedFinal;
	
	public WeightsTable() {
		this.markedFinal = false;
		this.data = new HashMap<>();
	}
	
	public WeightsTable(WeightsTable input) {
		this.markedFinal = input.isFinal();
		this.data = new HashMap<>(input.data);
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof MetadataModification) {
//			return this.data.equals(((WeightsTable)obj).data);
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
		return type;
	}

	@Override
	public void update(IDataUnit obj, boolean shouldOverwrite) {
		Objects.requireNonNull(obj);
		if (!shouldOverwrite && obj.analysisIsFinalized(getType())) {
			return;
		}
		if ((this.isFinal() || obj.analysisIsFinalized(AnalysisTypes.weights)) && getType() == AnalysisTypes.weights) {
			obj.resetAnalysis(AnalysisTypes.weights);
		}
		if (!obj.analysisIsFinalized(getType())) {
			obj.addAnalysis(getType(), this);
		}
		obj.resetAnalysis(AnalysisTypes.wordTable);
	}	

	@Override
	public IAnalysisResult calculateTFIDF(IWeightedAnalysis corpus) {
		WeightsTable result = new WeightsTable();
		double min = Double.MAX_VALUE;
		String minW = "";
		double max = Double.MIN_VALUE;
		String maxW = "";
		if (corpus instanceof WeightsTable) {
			for (Languages lang : this.data.keySet()) {
				for (String term : this.getDataForLanguage(lang).keySet()) {
					double tfidf = this.getDataForLanguage(lang).get(term) * ((WeightsTable)corpus).getDataForLanguage(lang).get(term);
					if (tfidf < min) {
						min = tfidf;
						minW = term;
					}
					if (tfidf > max) {
						max = tfidf;
						maxW = term;
					}
					result.add(lang, term, tfidf);
				}
			}
		}
		return result;
	}

	/**
	 * Temporarily not in use (see {@link WeightsTable.createFromXML})
	 */
	@Override
	public boolean writeToXML(IWriterXML writer) {
		boolean Ok = true;
//		try {
//			long count = this.data.entrySet().stream().mapToInt(val -> val.getValue().size()).sum();
//			if (count < thresholdSaveXML) {
//				for (Languages lang : this.data.keySet()) {
//					writer.writeStartElement(getType().getTagText());
//					writer.writeAttribute(XMLTags.language.getTagText(), lang.getTagText());
//					writer.writeData(convertToString(this.data.get(lang)));
//					writer.writeEndElement();
//				}
//			}
//		} catch (XMLException e) {
//			Ok = false;
//		}
		return Ok;
	}

	@Override
	public Set<Languages> getLanguages() {
		return data.keySet();
	}

	@Override
	public Map<String, ? extends Number> getWeights(Languages lang) {
		if (this.data.containsKey(lang)) {
			return this.data.get(lang);
		}
		return new HashMap<>();
	}

	@Override
	public IAnalysisResult filter(IWeightsFilter filter) {
		WeightsTable result = new WeightsTable();
		for (Languages lang : this.data.keySet()) {
			double threshold = filter.calculateMinimumAllowedValue(this.data.get(lang).entrySet()
					.stream().map(val -> val.getValue())
					.collect(Collectors.toList()));
			Map<String, Double> resForLang = new HashMap<>();
			this.data.get(lang).entrySet()
					.stream().filter(val -> val.getValue() >= threshold).forEach(val -> resForLang.put(val.getKey(), val.getValue()));
			result.add(lang, resForLang);
		}
		return result;
//		for (Languages lang : this.data.keySet()) {
//			double threshold = filter.calculateMinimumAllowedValue(this.data.get(lang)
//					.stream().map(val -> val.getWeight())
//					.collect(Collectors.toCollection(supplierDouble)));
//			this.data.put(lang, this.data.get(lang).stream().filter(val -> val.getWeight() >= threshold)
//					.collect(Collectors.toCollection(supplierWeightedObject)));
//		}
//		return this;
	}

	private void add(Languages lang, Map<String, Double> resForLang) {
		this.data.put(lang, resForLang);
	}

	protected Map<String, Double> getDataForLanguage(Languages lang) {
		if (this.data.containsKey(lang)) {
			return this.data.get(lang);
		}
		return new HashMap<>();
	}

	public void add(Languages lang, String term, double weight) {
		Map<String, Double> map = null;
		if (!this.data.containsKey(lang)) {
			map = new HashMap<>();
		} else {
			map = this.data.get(lang);
		}
		map.put(term, weight);
		this.data.put(lang, map);
	}

	@Override
	public void markAsFinal() {
		this.markedFinal = true;
	}

	@Override
	public boolean isFinal() {
		return markedFinal;
	}

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty();
	}
}
