package dataUnits;

/**
 * Enumeration with special tags that appear in raw data (json) 
 * @author Pdz
 *
 */
public enum RawDataTags {
	
	/**
	 * Elemental data block with metadata
	 */
	metadata("meta"),
	
	/**
	 * Elemental data block with link
	 */
	link("http");
	
	/**
	 * Tag text
	 */
	private final String tagName;
	
	private RawDataTags(String tag) {
		tagName = tag;
	}

	public String getTagText() {
		return tagName;
	}
}
