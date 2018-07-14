package io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import categories.ICategory;
import dataUnits.IDataUnitCorpus;
import utils.ConfigurationData;

public class ReaderXML implements IReader {

	private List<IReadable> data;
	
	public ReaderXML() {
		data = new ArrayList<>();
	}
	
	@Override
	public boolean readFromFile(String filename) {
		boolean Ok = true;

		List<IReadable> results = new ArrayList<>();
		
		IReadableXML currentObject = null;

		XMLInputFactory factory;
		XMLEventReader reader = null;
				 
//		try (FileReader fReader = new FileReader(filename)) {
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename))) {
			factory = XMLInputFactory.newInstance();
			reader = factory.createXMLEventReader(bis, StandardCharsets.UTF_8.name());
			while (Ok && reader.hasNext()) {
				XMLEvent event = reader.peek();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						String name = event.asStartElement().getName().getLocalPart();
						if (name.equalsIgnoreCase(XMLEntities.category.getTagText())) {
							currentObject = ICategory.createFromXML(reader);
							if (currentObject == null) {
								Ok = false;
								break;
							}
							results.add(currentObject);
						} else if (name.equalsIgnoreCase(XMLEntities.corpus.getTagText())) {
							currentObject = IDataUnitCorpus.createFromXML(reader);
							if (currentObject == null) {
								Ok = false;
								break;
							}
							results.add(currentObject);
						} else if (name.equalsIgnoreCase(XMLEntities.config.getTagText())) {
							event = reader.nextEvent();
							currentObject = ConfigurationData.createFromXML(reader);
							if (currentObject == null) {
								Ok = false;
								break;
							}
							results.add(currentObject);
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
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (XMLStreamException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		if (Ok) {
			this.data.addAll(results);
		}
		return Ok;
	}

	public List<IReadable> getData() {
		return data;
	}	
}