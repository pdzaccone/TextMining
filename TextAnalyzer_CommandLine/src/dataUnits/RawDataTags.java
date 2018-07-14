package dataUnits;

public enum RawDataTags {
	metadata("meta"),
	link("http");
	
	private final String tagName;
	
	private RawDataTags(String tag) {
		tagName = tag;
	}

	public String getTagText() {
		return tagName;
	}
}
