package io;

import java.util.Collection;

/**
 * This interface defines an XML-writer
 * @author Pdz
 *
 */
public interface IWriterXML {
	
	/**
	 * Saves a provided {@link ISaveableXML} object to file
	 * @param filename File to save data to
	 * @param data Object to save
	 * @param deleteIfExists Whether to overwrite already existing file with the same name
	 * @return True, if data were saved successfully, otherwise false
	 */
	public boolean writeToFile(String filename, ISaveableXML data, boolean deleteIfExists);
	
	/**
	 * Saves multiple {@link ISaveableXML} objects to file
	 * @param filename File to save data to
	 * @param data Objects to save
	 * @param rootTag Tag under which the objects collection should be saved
	 * @param deleteIfExists Whether to overwrite already existing file with the same name
	 * @return True, if data were saved successfully, otherwise false
	 */
	public boolean writeToFile(String filename, Collection<ISaveableXML> data, String rootTag, boolean deleteIfExists);
	
	/**
	 * Opens tag and writes start element.
	 * <p> This functionality is implemented in a separate method to ensure that the needed tabulation is added. 
	 * It makes the resulting XML-file much easier to read
	 * @param tag Tag
	 * @throws XMLException
	 */
	public void writeStartElement(String tag) throws XMLException;

	/**
	 * Writes data into the current element.
	 * <p> This functionality is implemented in a separate method to ensure that the needed tabulation is added. 
	 * It makes the resulting XML-file much easier to read
	 * @param data Data to save
	 * @throws XMLException
	 */
	public void writeData(String data) throws XMLException;

	/**
	 * Writes end element.
	 * <p> This functionality is implemented in a separate method to ensure that the needed tabulation is added. 
	 * It makes the resulting XML-file much easier to read
	 * @throws XMLException
	 */
	public void writeEndElement() throws XMLException;
	
	/**
	 * Adds an attribute to the current element
	 * @param tagText Attribute tag
	 * @param description Attribute value
	 * @throws XMLException
	 */
	public void writeAttribute(String tagText, String description) throws XMLException;
}
