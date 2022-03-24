package com.gmail.vangnamngo.scriptlangtest.parser;

import com.gmail.vangnamngo.scriptlangtest.utility.StringUtils;

import java.util.ArrayList;

public class LineReader {

    public static String[] breakDownLine(String line) {
        ArrayList<String> aList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean ignoreSpecial = false;
        boolean hasDq = false;
        boolean hasSq = false;

        int finalIndex = line.length() - 1;

        for (int i = 0; i <= finalIndex; i++) {
            char c = line.charAt(i);
            switch (c) {
                case ' ':
                    if (!ignoreSpecial && !hasDq && !hasSq) {
                        aList.add(builder.toString());
                        builder.delete(0, builder.length());
                        break;
                    }
                case '\\':
                    ignoreSpecial = true;
                    break;
                case '"':
                    if (!ignoreSpecial && !hasSq) {
                        hasDq = !hasDq;
                        if (!hasDq && i != finalIndex && line.charAt(i + 1) != ' ') {
                            // TODO: Error
                            throw new RuntimeException();
                        }
                    }
                case '\'':
                    if (!ignoreSpecial && !hasDq) {
                        hasSq = !hasSq;
                        if (!hasSq && i != finalIndex && line.charAt(i + 1) != ' ') {
                            // TODO: Error
                            throw new RuntimeException();
                        }
                    }
                default:
                    builder.append(c);
                    ignoreSpecial = false;
            }
        }

        // TODO: Error
        if (hasDq) {
            throw new RuntimeException();
        }
        if (hasSq) {
            throw new RuntimeException();
        }
        if (ignoreSpecial) {
            throw new RuntimeException();
        }

        aList.add(builder.toString());

        return aList.toArray(new String[0]);
    }

    public static boolean isHeader(String line) {
        return isHeader(breakDownLine(line));
    }

    public static boolean isHeader(String[] line) {
        return line != null && line[0].length() > 1 && line[0].startsWith("#");
    }

    public static String getHeaderPrefix(String line) {
        return getHeaderPrefix(breakDownLine(line));
    }

    public static String getHeaderPrefix(String[] line) {
        if (!isHeader(line)) {
            return null;
        }
        return line[0].substring(1);
    }

    public static String getScriptType(String line) {
        return getScriptType(breakDownLine(line));
    }

    // TODO: Properly recognize only certain kinds of scripts
    public static String getScriptType(String[] line) {
        String p = getHeaderPrefix(line);
        if (p == null || !p.equals("type")) {
            return null;
        }
        return StringUtils.removeQuotes(line[1]);
    }
}
