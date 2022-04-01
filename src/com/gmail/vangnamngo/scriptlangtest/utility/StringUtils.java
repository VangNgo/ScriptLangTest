package com.gmail.vangnamngo.scriptlangtest.utility;

import java.util.HashSet;
import java.util.Set;

public class StringUtils {

    public final static Set<Character> ALPHABETICAL_CHARS = new HashSet<>();
    public final static Set<Character> NUMERIC_CHARS = new HashSet<>();

    static {
        // Letters
        for (int i = 0; i < 26; i++) {
            if (i < 10) {
                StringUtils.NUMERIC_CHARS.add((char) ('0' + i));
            }
            StringUtils.ALPHABETICAL_CHARS.add((char)('a' + i));
            StringUtils.ALPHABETICAL_CHARS.add((char)('A' + i));
        }
        StringUtils.ALPHABETICAL_CHARS.add('_');
    }

    public static boolean isAlphanumeric(char c) {
        return isAlphabetic(c) || isNumeric(c);
    }

    public static boolean isAlphabetic(char c) {
        return ALPHABETICAL_CHARS.contains(c);
    }

    public static boolean isNumeric(char c) {
        return NUMERIC_CHARS.contains(c);
    }

    public static boolean isQuoted(String str) {
        return (str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"));
    }

    public static String removeQuotes(String str) {
        return isQuoted(str) ? str.substring(1, str.length() - 1) : str;
    }
}
