package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
	public static final Pattern patternWords = Pattern.compile("[[^\\P{L}\\d]+[-]*]+");
	public static final Pattern patternNumeric = Pattern.compile("\\b([0-9]|[1-9][0-9])\\b");

	public static Collection<String> split(Pattern pattern, String value) {
		List<String> strings = new ArrayList<String>();
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			String s = matcher.group();
			//TODO Should be improved! For example, "z. B." is interpreted as 2 words. :/
			if (s.endsWith("-")) {
				s = s.substring(0, s.length() - 1);
			}
			if (s.startsWith("-")) {
				s = s.substring(1);
			}
			strings.add(s.trim().toLowerCase());
		}
		return strings;
	}

	public static int toNumeric(String input) {
		int result = 0;
		Matcher matcher = patternNumeric.matcher(input);
		if (matcher.find()) {
			try {
				result = Integer.parseInt(matcher.group(0));
			} catch (NumberFormatException e) {
				result = 0;
			}
		}
		return result;
	}
}
