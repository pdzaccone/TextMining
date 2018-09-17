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
import utils.RegexHelper;
import utils.WeightedObject;

/**
 * One of variety of {@link IAnalysisResult} classes, storing its data in a single XML-entry
 * @author Pdz
 *
 */
public class MetadataModification implements IAnalysisResult {

	/**
	 * Separates keys and values
	 */
	private static final String separatorKeyValue = " - ";
	
	/**
	 * Marks weights percentage
	 */
	private static final String separatorPercent = "%";
	
	/**
	 * Separates multiple key-value pairs
	 */
	private static final String separatorObject = "; ";

	/**
	 * This method reads {@link MetadataModification} data from the file using the provided {@link XMLEventReader}
	 * @param reader XML-reader, with its "cursor" positioned at the beginning of this object
	 * @param type Analysis type, used to better identify the analysis data being read
	 * @return Resulting {@link IAnalysisResult} object or null
	 */
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
	
	/**
	 * Creates a description string from a provided sorted set of weighted data
	 * @param input Weighted data to "print"
	 * @return Resulting string description
	 */
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
	
	/**
	 * Creates a sorted set of weighted data from the provided textual description
	 * @param input Input string to parse
	 * @return Resulting set of weighted objects
	 */
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
							
							result.add(new WeightedObject(finalSplit[0], (double)RegexHelper.toNumeric(finalSplit[1]) / 100));
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
	 * Sorted set with weighted data objects
	 */
	private TreeSet<WeightedObject> data;
	
	/**
	 * Type of {@link IAnalysisResult} object
	 */
	private AnalysisTypes type;
	
	/**
	 * Whether the {@link IAnalysisResult} is final, i.e. "complete" for this session
	 */
	private boolean markedFinal;

	/**
	 * Default constructor
	 */
	public MetadataModification() {
		this.type = AnalysisTypes.none;
		this.data = new TreeSet<>();
		this.markedFinal = false;
	}

	/**
	 * Constructor with parameters
	 * @param type Type of analysis result
	 * @param value Weighted data
	 */
	public MetadataModification(AnalysisTypes type, WeightedObject value) {
		this.type = type;
		this.data = new TreeSet<>();
		this.data.add(value);
		this.markedFinal = false;
	}

	/**
	 * Constructor with parameters
	 * @param type Type of analysis result
	 * @param value Sorted set with weighted data
	 */
	public MetadataModification(AnalysisTypes type, TreeSet<WeightedObject> value) {
		this.type = type;
		this.data = new TreeSet<>();
		this.data.addAll(value);
		this.markedFinal = false;
	}

	/**
	 * Gets internal data as sorted set
	 * @return Internal data set
	 */
	public TreeSet<WeightedObject> getData() {
		return this.data;
	}
	 
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

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty();
	}
}