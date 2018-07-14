package analysis;

import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import io.IWriterXML;
import io.XMLException;
import utils.WeightedObject;

public class MetadataModification implements IAnalysisResult {

	private static final String separatorKeyValue = " - ";
	private static final String separatorPercent = "%";
	private static final String separatorObject = "; ";

	public static IAnalysisResult createFromXML(XMLEventReader reader, AnalysisTypes type) {
		MetadataModification result = null;
		boolean Ok = true, goOn = true;
		
		String anData = "";
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				switch (event.getEventType()) {
					case XMLStreamConstants.CHARACTERS:
						anData = event.asCharacters().getData().trim();
						break;

					case XMLStreamConstants.END_ELEMENT:
						goOn = false;
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
			result = new MetadataModification(type, convertFromString(anData));
			result.markAsFinal();
		}
		return result;
	}
	
	private static String convertToString(TreeSet<WeightedObject> input) {
		StringBuilder sb = new StringBuilder();
		if (input.size() == 1) {
			return input.iterator().next().getData();
		}
		Iterator<WeightedObject> it = input.descendingIterator();
		while (it.hasNext()) {
			WeightedObject obj = it.next();
			sb.append(obj.getData() + separatorKeyValue + Math.round(100 * obj.getWeight()) + separatorPercent + separatorObject);
		}
		return sb.toString();
	}
	
	private static TreeSet<WeightedObject> convertFromString(String input) {
		TreeSet<WeightedObject> result = new TreeSet<>();
		boolean Ok = true;
		if (input == null || input.isEmpty()) {
			Ok = false;
		} else {
			try {
				if (!input.contains(separatorObject)) {
					result.add(new WeightedObject(input, 1));
				} else {
					String[] strs = input.split(separatorObject);
					for (String singleObj : strs) {
						String[] finalSplit = singleObj.split(separatorKeyValue);
						if (finalSplit != null && finalSplit.length == 2) {
							result.add(new WeightedObject(finalSplit[0], Double.parseDouble(finalSplit[1]) / 100));
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

	private TreeSet<WeightedObject> data;
	private AnalysisTypes type;
	private boolean markedFinal;

	public MetadataModification() {
		this.type = AnalysisTypes.none;
		this.data = new TreeSet<>();
		this.markedFinal = false;
	}

	public MetadataModification(AnalysisTypes type, WeightedObject value) {
		this.type = type;
		this.data = new TreeSet<>();
		this.data.add(value);
		this.markedFinal = false;
	}

	public MetadataModification(AnalysisTypes type, TreeSet<WeightedObject> value) {
		this.type = type;
		this.data = new TreeSet<>();
		this.data.addAll(value);
		this.markedFinal = false;
	}

	public TreeSet<WeightedObject> getData() {
		return this.data;
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof MetadataModification) {
//			return this.data.equals(((MetadataModification)obj).data) && this.type.equals(((MetadataModification)obj).type);
//		}
//		return false;
//	}
//	
//	@Override
//    public int hashCode() {
//		return 31 + ((this.type == null) ? 0 : this.type.hashCode()) + (this.data == null ? 0 : this.data.hashCode());
//    }
	 
	@Override
	public AnalysisTypes getType() {
		return this.type;
	}

	@Override
	public void update(IDataUnit obj, boolean shouldOverwrite) {
		Objects.requireNonNull(obj);
		if (!shouldOverwrite && obj.analysisIsFinalized(getType()))
			return;
		if (obj.analysisIsFinalized(getType()) || this.isFinal()) {
			obj.resetAnalysis(getType());
		}
		if (!obj.analysisIsFinalized(getType())) {
			obj.addAnalysis(getType(), this);
		}
	}	

	@Override
	public boolean writeToXML(IWriterXML writer) {
		boolean Ok = true;
		try {
			writer.writeData(convertToString(data));
		} catch (XMLException e) {
			Ok = false;
		}
		return Ok;
	}

	@Override
	public void markAsFinal() {
		this.markedFinal = true;
	}

	@Override
	public boolean isFinal() {
		return markedFinal;
	}
}