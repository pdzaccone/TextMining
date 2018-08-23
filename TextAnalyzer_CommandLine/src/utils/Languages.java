package utils;

import java.util.Arrays;
import java.util.List;

public enum Languages {
	en("EN", Arrays.asList("a", "the", "with", "and", "if", "is", "are", "of")),
	de("DE", Arrays.asList("die", "der", "das", "eine", "mit", "und", "wir", "oder")),
	unknown("IDK", Arrays.asList());
	
	private final String text;
	private final List<String> listOfWords;
	
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

	public boolean containsKeyword(String input) {
		return listOfWords.contains(input.toLowerCase());
	}

	public String getTagText() {
		return text;
	}
}