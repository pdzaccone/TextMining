package io;

import java.util.Collection;

public interface IWriterXML {
	public boolean writeToFile(String filename, ISaveableXML data, boolean deleteIfExists);
	public boolean writeToFile(String filename, Collection<ISaveableXML> data, String rootTag, boolean deleteIfExists);
	public void writeStartElement(String tag) throws XMLException;
	public void writeData(String data) throws XMLException;
	public void writeEndElement() throws XMLException;
	public void writeAttribute(String tagText, String description) throws XMLException;
}
