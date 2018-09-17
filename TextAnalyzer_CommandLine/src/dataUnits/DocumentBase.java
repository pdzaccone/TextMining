package dataUnits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

import analysis.IAnalysisResult;
import analysis.MetadataModification;
import analyzers.AnalysisTypes;
import crawlers.ICrawler;
import io.IReadableXML;
import io.IWriterXML;
import io.XMLException;
import utils.Languages;
import utils.ListMap;
import utils.PairDataUnitAnalysis;
import utils.WeightedObject;

/**
 * Base class for {@link IDataUnit} of type document
 * @author Pdz
 *
 */
public class DocumentBase implements IDataUnitDoc {
	
	/**
	 * Tag to recognize this specific version of the document
	 */
	public static final String typeTag = "docBase";

	/**
	 * Reads document from an XML-file
	 * @param reader Initialized XML reader, with cursor at the beginning of the document entry
	 * @return Resulting document or null
	 */
	public static IReadableXML createFromXML(XMLEventReader reader) {
		boolean Ok = true, goOn = true;
		ListMap<AnalysisTypes, IAnalysisResult> anData = new ListMap<>();
		Map<String, IDataUnitElemental> docData = new HashMap<>();
		
		boolean readingAnalysis = false, readingData = false;

		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						String name = event.asStartElement().getName().getLocalPart();
						if (name.equalsIgnoreCase(XMLTags.singleDoc.getTagText())) {
							event = reader.nextEvent();
						} else if (name.equalsIgnoreCase(XMLTags.data.getTagText())) {
							readingData = true;
							event = reader.nextEvent();
						} else if (name.equalsIgnoreCase(XMLTags.analysisData.getTagText())) {
							readingAnalysis = true;
							event = reader.nextEvent();
						} else {
							if (readingAnalysis) {
								IAnalysisResult anRes = IAnalysisResult.readXML(reader);
								if (anRes != null && !anRes.isEmpty()) {
									anData.put(anRes.getType(), anRes);
								}
							} else if (readingData) {
								IDataUnitElemental doc = (IDataUnitElemental) IDataUnitElemental.createFromXML(reader);
								if (doc != null && !doc.isEmpty()) {
									docData.put(doc.getKey(), doc);
								}
							}
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						String endName = event.asEndElement().getName().getLocalPart();
						if (endName.equalsIgnoreCase(XMLTags.singleDoc.getTagText())) {
							goOn = false;
						} else if (endName.equalsIgnoreCase(XMLTags.data.getTagText())) {
							readingData = false;
							event = reader.nextEvent();
						} else if (endName.equalsIgnoreCase(XMLTags.analysisData.getTagText())) {
							readingAnalysis = false;
							event = reader.nextEvent();
						}
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
		if (Ok && !docData.isEmpty()) {
			DocumentBase document = new DocumentBase();
			for (String key : docData.keySet()) {
				document.addData(key, docData.get(key));
			}
			document.addAllAnalysis(anData);
			return document;
		}
		return null;
	}

	/**
	 * Internal storage with elemental data blocks
	 */
	private Map<String, IDataUnitElemental> data;
	
	/**
	 * Internal storage with analysis data
	 */
	private ListMap<AnalysisTypes, IAnalysisResult> analysis;
	
	/**
	 * Document ID
	 */
	private int ID;

	/**
	 * Constructor without parameters
	 */
	public DocumentBase() {
		data = new HashMap<String, IDataUnitElemental>();
		analysis = new ListMap<AnalysisTypes, IAnalysisResult>();
		this.ID = IDataUnitDoc.DEFAULT_ID;
	}

	/**
	 * Constructor with parameters
	 * @param id New ID
	 */
	public DocumentBase(int id) {
		data = new HashMap<String, IDataUnitElemental>();
		analysis = new ListMap<AnalysisTypes, IAnalysisResult>();
		this.ID = id;
	}

	/**
	 * Constructor with parameters 
	 * @param src Source data - a set of elemental data blocks
	 */
	public DocumentBase(Map<String, IDataUnitElemental> src) {
		Objects.requireNonNull(src);
		data = src;
		analysis = new ListMap<AnalysisTypes, IAnalysisResult>();
	}

	@Override
	public IDataUnitDoc applyCrawler(ICrawler crawler) {
		IDataUnitDoc result = crawler.generateDataUnitDocLevel(this);
		result.addAllAnalysis(analysis);
		
		for (String key : this.data.keySet()) {
			PairDataUnitAnalysis crawlRes = crawler.crawl(this.data.get(key));
			result.addData(key, (IDataUnitElemental) crawlRes.getDataUnit());
			crawlRes.updateDataUnit(result);
		}
		return result;
	}

	@Override
	public void addData(String tag, IDataUnitElemental input) {
		data.put(tag, input);
	}

	@Override
	public Set<String> getAllTags() {
		return this.data.keySet();
	}

	@Override
	public IDataUnitElemental getData(String tag) {
		if (this.data.containsKey(tag)) {
			return this.data.get(tag);
		}
		return new DataUnitElementalEmpty();
	}

	@Override
	public boolean writeToXML(IWriterXML writer) {
		boolean Ok = true;
		try {
			writer.writeStartElement(XMLTags.singleDoc.getTagText());
			writer.writeAttribute(XMLTags.docType.getTagText(), DocumentBase.typeTag);
			writeData(writer);
			if (!this.analysis.isEmpty()) {
				writer.writeStartElement(XMLTags.analysisData.getTagText());
				for (AnalysisTypes type : this.analysis.keySet()) {
					if (type.canBeWrittenToXML()) {
						writer.writeStartElement(type.getTagText());
						for (IAnalysisResult res : this.analysis.get(type)) {
							if (!res.writeToXML(writer)) {
								Ok = false;
								break;
							}
						}
						writer.writeEndElement();
					}
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();
		} catch (XMLException e) {
			Ok = false;
		}
		return Ok;
	}
	
	/**
	 * Helper method for writing internal data to the XML file
	 * @param writer Initialized XML writer
	 * @return True, if data were saved successfully, otherwise false
	 */
	private boolean writeData(IWriterXML writer) {
		boolean Ok = true;
		try {
			if (!this.data.isEmpty()) {
				writer.writeStartElement(XMLTags.data.getTagText());
				for (String tag : this.data.keySet()) {
					if (!this.data.get(tag).writeToXML(writer)) {
						Ok = false;
						break;
					}
				}
				writer.writeEndElement();
			}
		} catch (XMLException e) {
			Ok = false;
		}
		return Ok;
	}

	@Override
	public List<IAnalysisResult> getAnalysisResults(AnalysisTypes type) {
		return analysis.get(type);
	}

	@Override
	public List<IAnalysisResult> getAllAnalysisResults() {
		return this.analysis.getAll();
	}

	@Override
	public void addAnalysis(AnalysisTypes type, IAnalysisResult input) {
		this.analysis.put(type, input);
	}

	@Override
	public void addAllAnalysis(ListMap<AnalysisTypes, IAnalysisResult> input) {
		this.analysis.putAll(input);
	}

	@Override
	public void addAllAnalysis(List<IAnalysisResult> input) {
		for (IAnalysisResult anRes : input) {
			this.analysis.put(anRes.getType(), anRes);
		}
	}

	@Override
	public void resetAnalysis(AnalysisTypes type) {
		this.analysis.removeKey(type);
	}

	@Override
	public int getID() {
		return this.ID;
	}

	@Override
	public void setID(int id) {
		this.ID = id;
	}
	
	@Override
	public boolean analysisIsFinalized(AnalysisTypes type) {
		return this.analysis.keySet().contains(type) 
				&& this.analysis.get(type).size() == 1 
				&& this.analysis.get(type).get(0).isFinal();
	}

	@Override
	public List<String> getCategoriesMap() {
		List<String> result = new ArrayList<>();
		List<IAnalysisResult> anRes = getAnalysisResults(AnalysisTypes.category);
		if (anRes.size() == 1) {
			TreeSet<WeightedObject> cats = ((MetadataModification)anRes.get(0)).getData();
			for (Iterator<WeightedObject> iter = cats.iterator(); iter.hasNext(); ) {
				result.add(iter.next().getData());
			}
		}
		return result;
	}

	@Override
	public Languages getMainLanguage() {
		if (this.getAnalysisResults(AnalysisTypes.language) == null) {
			return Languages.unknown;
		} else {
			return Languages.fromString(((MetadataModification)this.getAnalysisResults(AnalysisTypes.language).get(0)).getData().last().getData());
		}
	}

	@Override
	public boolean isEmpty() {
		return (this.data.isEmpty() || (this.data.size() == 1 && this.data.containsKey(RawDataTags.link.getTagText())) && this.analysis.isEmpty());
	}
}