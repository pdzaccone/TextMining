package utils;

import java.util.Arrays;
import java.util.List;

/**
 * This enumeration lists supported languages
 * @author Pdz
 *
 */
public enum Languages {
	
	/**
	 * English
	 */
	en("EN", Arrays.asList("a", "the", "with", "and", "if", "is", "are", "of")),
	
	/**
	 * German
	 */
	de("DE", Arrays.asList("die", "der", "das", "eine", "mit", "und", "wir", "oder")),
	
	/**
	 * The so-called "unknown" language
	 */
	unknown("IDK", Arrays.asList());
	
	/**
	 * Language description
	 */
	private final String text;
	
	/**
	 * List of words, used for language identification
	 */
	private final List<String> listOfWords;
	
	/**
	 * Constructor with parameters
	 * @param descr Language description
	 * @param list Identifying words
	 */
	private Languages(String descr, List<String> list) {
		text = descr;
		listOfWords = list;
	}

	public static Languages fromString(String value) {
		Languages res = Languages.unknown;
		for (Languages lang : values()) {
			if (lang.getTagText().equalsIgnoreCase(value)) {
				res = lang;
				break;
			}
		}
		return res;
	}

	/**
	 * Checks whether a provided word can be used to identify a language
	 * @param input Word to use for language identification
	 * @return True, if word identifies one of the supported languages, otherwise false
	 */
	public static boolean contains(String input) {
		boolean Ok = false;
		for (Languages lang : values()) {
			if (lang.getTagText().equalsIgnoreCase(input)) {
				Ok = true;
				break;
			}
		}
		return Ok;
	}

	/**
	 * Checks whether a provided word is in this language
	 * @param input Word to use for language identification
	 * @return True if it is, otherwise false
	 */
	public boolean containsKeyword(String input) {
		return listOfWords.contains(input.toLowerCase());
	}

	/**
	 * Gets language description text
	 * @return Language description
	 */
	public String getTagText() {
		return text;
	}
}