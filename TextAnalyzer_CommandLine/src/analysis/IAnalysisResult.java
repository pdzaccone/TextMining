package analysis;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import io.IReadableXML;
import io.ISaveableXML;

/**
 * This is a base interface for all "analysis results"
 * @author Pdz
 *
 */
public interface IAnalysisResult extends ISaveableXML, IReadableXML {
	
	/**
	 * Another enumeration with XML tags, used in analysis results
	 * @author Pdz
	 *
	 */
	public static enum XMLTags {
		language("lang"), 
		weights("weights");
				
		private final String tagText;
		
		private XMLTags(String text) {
			tagText = text;
		}

		public String getTagText() {
			return this.tagText;
		}
	}

	/**
	 * This is a general method, responsible for reading objects of type {@link IAnalysisResult}
	 * @param reader An initialized reader-class
	 * @return Resulting analysis result or null
	 */
	public static IAnalysisResult readXML(XMLEventReader reader) {
		IAnalysisResult result = null;
		boolean Ok = true, goOn = true;
		
		try {
			while (goOn && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						AnalysisTypes type = AnalysisTypes.fromString(event.asStartElement().getName().getLocalPart());
						if (type.isValid()) {
							result = type.createAnalysisFromXML(reader); 
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						goOn = false;
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
		if (!Ok || result instanceof EmptyAnalysis) {
			result = null;
		}
		return result;
	}

	/**
	 * Gets type of {@link IAnalysisResult}
	 * @return Type
	 */
	public AnalysisTypes getType();
	
	/**
	 * Updates provided {@link IDataUnit} object with internal data of the analysis result
	 * @param obj Data unit to update
	 * @param shouldOverwrite The data unit being updated may already contain data of the same type. This parameter defines, 
	 * whether that data should be overwritten
	 */
	public void update(IDataUnit obj, boolean shouldOverwrite);
	
	/**
	 * Use this method to mark an {@link IAnalysisResult} object as final. Useful when analysis is composed of multiple parts -
	 *  in this case this action indicates that the last part has been processed. 
	 */
	public void markAsFinal();
	
	/**
	 * Checks whether the {@link IAnalysisResult} is final (see {@link IAnalysisResult#markAsFinal()} method for details)
	 * @return
	 */
	public boolean isFinal();

	/**
	 * Checks whether the analysis result contains data or not
	 * @return True - contains data, otherwise false
	 */
	public boolean isEmpty();
}