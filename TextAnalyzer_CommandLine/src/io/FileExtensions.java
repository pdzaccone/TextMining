package io;

public enum FileExtensions {
	unsupported(""),
	xml("xml") {
		@Override
		public IReader createReader() {
			return new ReaderXML();
		}
	},
	json("json") {
		@Override
		public IReader createReader() {
			return new ReaderJSONData();
		}
	};
	
	private final String text;
	
	private FileExtensions(String input) {
		this.text = input;
	}
	
	public static FileExtensions fromString(String input) {
		for (FileExtensions val : FileExtensions.values()) {
			if (val.getText().equalsIgnoreCase(input)) {
				return val;
			}
		}
		return FileExtensions.unsupported;
	}
	
	public String getText() {
		return this.text;
	}

	public IReader createReader() {
		return null;
	}
}
