package io;

/**
 * This interfaces provides the functionality for saving an object to the XML file
 * @author Pdz
 *
 */
public interface ISaveableXML {
	
	/**
	 * Saves object to an XML file
	 * @param writer Initialized XML-writer
	 * @return True if data were saved successfully, otherwise false 
	 */
	public boolean writeToXML(IWriterXML writer);
}