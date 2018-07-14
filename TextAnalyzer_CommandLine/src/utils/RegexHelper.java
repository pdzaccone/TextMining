package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
//	public static final Pattern patternWords = Pattern.compile("\\b([\\p{L}]+[\\/\\-]*)\\b");
	public static final Pattern patternWords = Pattern.compile("[[^\\P{L}\\d]+[-]*]+");

	public static Collection<String> split(Pattern pattern, String value) {
		List<String> strings = new ArrayList<String>();
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			String s = matcher.group();
			if (s.endsWith("-")) {
				s = s.substring(0, s.length() - 1);
			}
			if (s.startsWith("-")) {
				s = s.substring(1);
			}
			strings.add(s);
		}
		return strings;
	}
}
