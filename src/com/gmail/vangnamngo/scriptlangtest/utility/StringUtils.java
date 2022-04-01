package com.gmail.vangnamngo.scriptlangtest.utility;

import java.util.HashSet;
import java.util.Set;

public class StringUtils {

    private final static Set<Character> ALPHABETICAL_CHARS = new HashSet<>();
    private final static Set<Character> NUMERIC_CHARS = new HashSet<>();
    private final static Set<Character> UNICODE_CHARS = new HashSet<>();

    static {
        // Letters
        for (int i = 0; i < 26; i++) {
            if (i < 10) {
                char num = (char) ('0' + i);
                NUMERIC_CHARS.add(num);
                UNICODE_CHARS.add(num);
            }
            char low = (char)('a' + i);
            char upper = (char)('A' + i);
            if (i < 6) {
                UNICODE_CHARS.add(low);
                UNICODE_CHARS.add(upper);
            }
            ALPHABETICAL_CHARS.add(low);
            ALPHABETICAL_CHARS.add(upper);
        }
        ALPHABETICAL_CHARS.add('_');
    }

    public static boolean isAlphanumericChar(char c) {
        return isAlphabeticChar(c) || isNumericChar(c);
    }

    public static boolean isAlphabeticChar(char c) {
        return ALPHABETICAL_CHARS.contains(c);
    }

    public static boolean isNumericChar(char c) {
        return NUMERIC_CHARS.contains(c);
    }

    public static boolean isUnicodeChar(char c) {
        return UNICODE_CHARS.contains(c);
    }

    public static boolean isQuoted(String str) {
        return (str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"));
    }

    public static String removeQuotes(String str) {
        return isQuoted(str) ? str.substring(1, str.length() - 1) : str;
    }
}
