package io;

/**
 * This enumeration holds various file extensions and serves as a Factory class for {@link IReader}
 * @author Pdz
 *
 */
public enum FileExtensions {
	
	/**
	 * Helper-value
	 */
	unsupported("") {
		@Override
		public IReader createReader() {
			// TODO Auto-generated method stub
			return null;
		}
	},
	
	/**
	 * XML-files
	 */
	xml("xml") {
		@Override
		public IReader createReader() {
			return new ReaderXML();
		}
	},
	
	/**
	 * JSON-files
	 */
	json("json") {
		@Override
		public IReader createReader() {
			return new ReaderJSONData();
		}
	};
	
	/**
	 * File extension
	 */
	private final String text;
	
	private FileExtensions(String input) {
		this.text = input;
	}
	
	/**
	 * Returns enumeration member, corresponding to provided string
	 * @param input String to identify
	 * @return Resulting enumeration member or {@link FileExtensions#unsupported}
	 */
	public static FileExtensions fromString(String input) {
		for (FileExtensions val : FileExtensions.values()) {
			if (val.getText().equalsIgnoreCase(input)) {
				return val;
			}
		}
		return FileExtensions.unsupported;
	}

	/**
	 * Creates proper reader
	 * @return Resulting {@link IReader} or null
	 */
	public abstract IReader createReader();

	public String getText() {
		return this.text;
	}
}