package com.gmail.vangnamngo.scriptlangtest.utility;

public class StringUtils {

    public static boolean isQuoted(String str) {
        return (str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"));
    }

    public static String removeQuotes(String str) {
        return isQuoted(str) ? str.substring(1, str.length() - 1) : str;
    }
}
