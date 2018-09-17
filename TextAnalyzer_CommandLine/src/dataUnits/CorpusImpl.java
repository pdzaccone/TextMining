package dataUnits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import analysis.IAnalysisResult;
import analyzers.AnalysisTypes;
import crawlers.ICrawler;
import io.IReadableXML;
import io.IWriterXML;
import io.XMLException;
import utils.ListMap;
import utils.PairDataUnitAnalysis;

public class CorpusImpl implements IDataUnitCorpus {

	public static final String typeTag = "corpusBase";

	public static IReadableXML createFromXML(XMLEventReader reader) {
		boolean Ok = true, goOn = true;
		CorpusImpl result = null;
		ListMap<AnalysisTypes, IAnalysisResult> anData = new ListMap<>();
		List<IDataUnitDoc> docData = new ArrayList<>();
		
		boolean readingAnalysis = false, readingData = false;

		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						String name = event.asStartElement().getName().getLocalPart();
						if (name.equalsIgnoreCase(XMLTags.corpus.getTagText())) {
							event = reader.nextEvent();
						} else if (name.equalsIgnoreCase(XMLTags.analysisData.getTagText())) {
							readingAnalysis = true;
							event = reader.nextEvent();
						} else if (name.equalsIgnoreCase(XMLTags.documents.getTagText())) {
							readingAnalysis = false;
							readingData = true;
							event = reader.nextEvent();
						} else {
							if (readingAnalysis) {
								IAnalysisResult anRes = IAnalysisResult.readXML(reader);
								if (anRes != null && !anRes.isEmpty()) {
									anData.put(anRes.getType(), anRes);
								}
							} else if (readingData) {
								IDataUnitDoc doc = (IDataUnitDoc) IDataUnitDoc.createFromXML(reader);
								if (doc != null && !doc.isEmpty()) {
									docData.add(doc);
								}
							}
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						String endName = event.asEndElement().getName().getLocalPart();
						if (endName.equalsIgnoreCase(XMLTags.analysisData.getTagText())) {
							readingAnalysis = false;
							event = reader.nextEvent();
						} else if (endName.equalsIgnoreCase(XMLTags.documents.getTagText())) {
							readingData = false;
							event = reader.nextEvent();
						} else if (endName.equalsIgnoreCase(XMLTags.corpus.getTagText())) {
							goOn = false;
						}
						break;
						
					default:
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
			result = new CorpusImpl();
			if (docData.isEmpty()) {
				Ok = false;
			} else {
				for (IDataUnitDoc doc : docData) {
					result.addDocument(doc);
				}
			}
			if (Ok) {
				result.addAllAnalysis(anData);
			}
		}
		if (!Ok) {
			result = null;
		}
		return result;
	}

	private List<IDataUnitDoc> data;
	private ListMap<AnalysisTypes, IAnalysisResult> analysis;
	
	public CorpusImpl() {
		data = new ArrayList<IDataUnitDoc>();
		analysis = new ListMap<AnalysisTypes, IAnalysisResult>();
	}

	public CorpusImpl(ListMap<AnalysisTypes, String> metadata) {
		this.data = new ArrayList<IDataUnitDoc>();
		this.analysis = new ListMap<AnalysisTypes, IAnalysisResult>();
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof CorpusImpl) {
//			return this.data.equals(((CorpusImpl)obj).data) 
//					&& this.analysis.equals(((CorpusImpl)obj).analysis);
//		}
//		return false;
//	}
//	
//	@Override
//    public int hashCode() {
//		return 31 + (this.analysis == null ? 0 : this.analysis.hashCode()) 
//				+ (this.data == null ? 0 : this.data.hashCode());
//    }

	@Override
	public void addDocument(IDataUnitDoc input) {
		//TODO Should remake a concept of indices - it is too unsafe now - duplicates 
		//TODO are created if loading data from 2 files consequently
		if (input != null) {
			data.add(input);
			if (input.getID() == IDataUnitDoc.DEFAULT_ID) {
				input.setID(data.size() - 1);
			}
		}
	}

	@Override
	public void addCorpus(IDataUnitCorpus input) {
		if (input != null) {
			this.data.addAll(input.getDocuments());
			this.addAllAnalysis(input.getAnalysisResults(AnalysisTypes.all));
		}
	}

	@Override
	public IDataUnitCorpus applyCrawler(ICrawler crawler) {
		IDataUnitCorpus resCorp = new CorpusImpl();
		resCorp.addAllAnalysis(this.analysis);
		
		for (IDataUnitDoc doc : this.data) {
			PairDataUnitAnalysis resCrawl = crawler.crawl(doc);
			resCorp.addDocument((IDataUnitDoc) resCrawl.getDataUnit());
			resCrawl.updateDataUnit(resCorp);
		}
		return resCorp;
	}

	@Override
	public boolean writeToXML(IWriterXML writer) {
		boolean Ok = true;
		try {
			writer.writeStartElement(XMLTags.corpus.getTagText());
			writer.writeAttribute(XMLTags.corpusType.getTagText(), CorpusImpl.typeTag);
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
			writeData(writer);
			writer.writeEndElement();
		} catch (XMLException e) {
			Ok = false;
		}
		return Ok;
	}

	private boolean writeData(IWriterXML writer) {
		boolean Ok = true;
		try {
			if (!this.data.isEmpty()) {
				writer.writeStartElement(XMLTags.documents.getTagText());
				for (IDataUnitDoc doc : this.data) {
					if (!doc.writeToXML(writer)) {
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
		if (type == AnalysisTypes.all) {
			return analysis.getAll();
		}
		return analysis.get(type);
	}

	@Override
	public List<IAnalysisResult> getAllAnalysisResults() {
		return this.analysis.getAll();
	}

	@Override
	public Collection<IDataUnitDoc> getDocuments() {
		return this.data;
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
	public int size() {
		return this.data.size();
	}
	
	@Override
	public boolean analysisIsFinalized(AnalysisTypes type) {
		return this.analysis.keySet().contains(type) 
				&& this.analysis.get(type).size() == 1 
				&& this.analysis.get(type).get(0).isFinal();
	}

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty() && this.analysis.isEmpty();
	}
}