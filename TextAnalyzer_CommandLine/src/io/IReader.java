package io;

import java.util.List;

public interface IReader {
	public boolean readFromFile(String filename);
	public List<IReadable> getData();
}