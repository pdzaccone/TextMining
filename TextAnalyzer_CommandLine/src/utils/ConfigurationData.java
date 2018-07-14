package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import io.IReadableXML;
import io.ISaveableXML;
import io.IWriterXML;
import io.XMLException;

public class ConfigurationData implements IReadableXML, ISaveableXML {

	public static final String configTag = "config";
	
	private static final String fileConfig = "config.xml";
	private static final String defaultFolderData = "data";
	private static final String tagPathCategories = "pathCategories";
	private static final String tagPathData = "pathData";
	private static final String dateTimeString = "dd-MM-yyyy_HH-mm-ss";
	private static final String prefixCorpus = "corpus";
	private static final String separatorFileName = "_";
	private static final String extensionXML = ".xml";

	public static String getConfigPath() {
		return System.getProperty("user.dir") + System.getProperty("file.separator") + fileConfig;
	}

	public static ConfigurationData createFromXML(XMLEventReader reader) {
		boolean Ok = true;
		
		ConfigurationData result = new ConfigurationData();
		
		try
		{
			boolean readPathCat = false, readPathData = false;
			while (reader.hasNext())
			{				
				XMLEvent event = reader.nextEvent();
				switch (event.getEventType())
				{
					case XMLStreamConstants.START_ELEMENT:
						String name = event.asStartElement().getName().getLocalPart();
						switch (name)
						{
							case tagPathCategories:
								readPathCat = true;
								break;
								
							case tagPathData:
								readPathData = true;
								break;
						}
						break;
						
					case XMLStreamConstants.CHARACTERS:
						String text = event.asCharacters().getData();
						text = text.trim();
						if (readPathCat) {
							result.setPathCategories(text);
							readPathCat = false;
						}
						if (readPathData) {
							result.setPathData(text);
							readPathData = false;
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						break;
				}
			}
		}
		catch (Exception e) {
			Ok = false;
		}
		if (!Ok) {
			result = null;
		}
		return result;
	}
	
	private String pathCategories;
	private String pathData;
	
	public ConfigurationData() {
		this.pathCategories = System.getProperty("user.dir");
		this.pathData = System.getProperty("user.dir") + System.getProperty("file.separator") + defaultFolderData;
	}

	@Override
	public boolean writeToXML(IWriterXML writer) {
		boolean Ok = true;
		try {
			writer.writeStartElement(configTag);
			writer.writeStartElement(tagPathCategories);
			writer.writeData(pathCategories);
			writer.writeEndElement();
			writer.writeStartElement(tagPathData);
			writer.writeData(pathData);
			writer.writeEndElement();
			writer.writeEndElement();
		} catch (XMLException e) {
			Ok = false;
		}
		return Ok;
	}

	public String getPathCategories() {
		return this.pathCategories;
	}

	public void setPathCategories(String path) {
		this.pathCategories = path;
	}

	public String getPathData() {
		return this.pathData;
	}

	public void setPathData(String path) {
		this.pathData = path;
	}

	public String generateCorpusName() {
		SimpleDateFormat sdf = new SimpleDateFormat(dateTimeString);
		String timeStr = sdf.format(new Date());
		String str = this.pathData + System.getProperty("file.separator") + prefixCorpus + separatorFileName + timeStr + extensionXML;
		return str;
	}
}