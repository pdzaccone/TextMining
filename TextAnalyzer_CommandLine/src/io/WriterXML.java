package io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class WriterXML implements IWriterXML {

	private static enum ElementTypes {
		start,
		data,
		end
	}
	
	private static final String indent = "    ";

	private int currentDepth;
	private XMLStreamWriter writer;
	private ElementTypes previousElement;
	
	public WriterXML() {
		currentDepth = 0;
		writer = null;
		previousElement = ElementTypes.start;
	}
	
	private static void writeIndent(XMLStreamWriter writer, int depth) throws XMLStreamException
	{
		writer.writeCharacters(System.getProperty("line.separator"));
		for (int i = 0; i < depth; i++)
			writer.writeCharacters(indent);
	}

	@Override
	public boolean writeToFile(String filename, ISaveableXML data, boolean deleteIfExists) {
		boolean Ok = true;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		
		try {
			if (deleteIfExists) {
				if (Files.exists(Paths.get(filename))) {
					Files.delete(Paths.get(filename));
				}
			}
		} catch (IOException e) {
			return false;
		}
		
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(filename)))
		{
			writer = factory.createXMLStreamWriter(os, StandardCharsets.UTF_8.name());
			writer.writeStartDocument();
			if (data != null) {
				Ok = data.writeToXML(this);
			}
			if (Ok) {
				writer.writeEndDocument();
			}
		}
		catch (Exception e) {
			Ok = false;
		}
		return Ok;
	}

	@Override
	public boolean writeToFile(String filename, Collection<ISaveableXML> data, String rootTag, boolean deleteIfExists) {
		boolean Ok = true;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		
		try {
			if (deleteIfExists) {
				if (Files.exists(Paths.get(filename))) {
					Files.delete(Paths.get(filename));
				}
			}
		} catch (IOException e) {
			return false;
		}
		
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(filename)))
		{
			writer = factory.createXMLStreamWriter(os, StandardCharsets.UTF_8.name());
			writer.writeStartDocument();
			if (data != null) {
				writeStartElement(rootTag);
				for (ISaveableXML dataBlock : data) {
					Ok = dataBlock.writeToXML(this);
					if (!Ok) {
						break;
					}
				}
				writeEndElement();
			}
			if (Ok) {
				writer.writeEndDocument();
			}
		}
		catch (Exception e) {
			Ok = false;
		}
		return Ok;
	}

	@Override
	public void writeStartElement(String tag) throws XMLException {
		Objects.requireNonNull(tag);
		try {
			if (previousElement != ElementTypes.end) {
				currentDepth++;
			}
			WriterXML.writeIndent(writer, currentDepth);
			writer.writeStartElement(tag);
			previousElement = ElementTypes.start;
		} catch (XMLStreamException e) {
			throw new XMLException(e);
		}
	}

	@Override
	public void writeData(String data) throws XMLException {
		Objects.requireNonNull(data);
		try {
			WriterXML.writeIndent(writer, ++currentDepth);
			writer.writeCharacters(data);
			previousElement = ElementTypes.data;
		} catch (XMLStreamException e) {
			throw new XMLException(e);
		}
	}

	@Override
	public void writeEndElement() throws XMLException {
		try {
			if (previousElement != ElementTypes.start) {
				WriterXML.writeIndent(writer, --currentDepth);
			} else {
				WriterXML.writeIndent(writer, currentDepth);
			}
			writer.writeEndElement();
			previousElement = ElementTypes.end;
		} catch (XMLStreamException e) {
			throw new XMLException(e);
		}
	}

	@Override
	public void writeAttribute(String tagText, String description) throws XMLException {
		try {
			writer.writeAttribute(tagText, description);
		} catch (XMLStreamException e) {
			throw new XMLException(e);
		}
	}
}