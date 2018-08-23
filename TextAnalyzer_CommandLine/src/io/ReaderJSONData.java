package io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import dataUnits.CorpusImpl;
import dataUnits.DataUnitElementalBase;
import dataUnits.DocumentBase;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;

public class ReaderJSONData implements IReader {

	private static final String PREFIX_LINK = "http";

	private List<IReadable> documents;
	
	public ReaderJSONData() {
		documents = new ArrayList<IReadable>();
	}
	
	@Override
	public boolean readFromFile(String filename) {
		boolean Ok = true;
		try (InputStream is = new FileInputStream(filename);
			 JsonParser parser = Json.createParser(is)) {
			while (parser.hasNext()) {
				Event e = parser.next();
				if (e == Event.KEY_NAME) {
					DataUnitElementalBase dusp = null;
					if (parser.getString().startsWith(PREFIX_LINK)) {
						IDataUnitDoc data = new DocumentBase();
						dusp = new DataUnitElementalBase(PREFIX_LINK, parser.getString());
						data.addData(PREFIX_LINK, dusp);
						documents.add(data);
					} else {
						String key = parser.getString();
						parser.next();
						String value = parser.getString().trim();
						if (!value.isEmpty()) {
							dusp = new DataUnitElementalBase(key, value);
							((IDataUnitDoc)documents.get(documents.size() - 1)).addData(key, dusp);
						}
					}
				}
			}
		} 
		catch (FileNotFoundException e) {
			Ok = false;
		} 
		catch (IOException e) {
			Ok = false;
		}
		if (Ok) {
			List<IReadable> toRemove = new ArrayList<>();
			for (IReadable doc : documents) {
				if (doc instanceof IDataUnitDoc) {
					if (((IDataUnitDoc)doc).isEmpty()) {
						toRemove.add(doc);
					}
				}
			}
			for (IReadable obj : toRemove) {
				documents.remove(obj);
			}
		}
		return Ok;
	}

	@Override
	public List<IReadable> getData() {
		List<IReadable> result = new ArrayList<>();
		IDataUnitCorpus corpus = new CorpusImpl();
		for (IReadable singleData : documents) {
			corpus.addDocument((IDataUnitDoc) singleData);
		}
		result.add(corpus);
		return result;
	}
}
