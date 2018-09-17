package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This helper class is responsible for application of REGEX-patterns
 * @author Pdz
 *
 */
public class RegexHelper {
	
	/**
	 * Regex pattern for words
	 */
	public static final Pattern patternWords = Pattern.compile("[[^\\P{L}\\d]+[-]*]+");
	
	/**
	 * Regex pattern for numerical values
	 */
	public static final Pattern patternNumeric = Pattern.compile("\\b([0-9]|[1-9][0-9])\\b");

	/**
	 * Splits the provided string according to the given pattern
	 * @param pattern Pattern to use
	 * @param input String to split
	 * @return Resulting collection of strings
	 */
	public static Collection<String> split(Pattern pattern, String input) {
		List<String> strings = new ArrayList<String>();
		Matcher matcher = pattern.matcher(input);
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

	/**
	 * Converts provided string to numeric value
	 * @param input String to convert
	 * @return Resulting integer or 0
	 */
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
