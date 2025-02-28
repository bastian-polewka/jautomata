package jautomata.generator;

public class Converter {
	
	public static String convertBase26(int index) {
	    StringBuilder result = new StringBuilder();
	    while (index >= 0) {
	        result.insert(0, (char) ('A' + index % 26));
	        index = index / 26 - 1;
	    }
	    return result.toString();
	}
	
	public static int convertBase26(String s) {
		if (s == null || s.isEmpty()) {
			throw new IllegalArgumentException("Input string cannot be null or empty.");
		}

		int index = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < 'A' || c > 'Z') {
				throw new IllegalArgumentException("Invalid character in input string: " + c);
			}
			index = index * 26 + (c - 'A' + 1);
		}
		return index - 1;
	}

	public static String incrementBase26(String s) {
		return convertBase26(convertBase26(s) + 1);
	}
}
