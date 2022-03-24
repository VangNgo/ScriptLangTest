package com.gmail.vangnamngo.scriptlangtest.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileParser {
    public static void compileFile(File file) throws FileNotFoundException {
        if (file == null) {
            return;
        }

        String fName = file.getName().substring(0, file.getName().length() - 4);
        String type = null;
        String dir = file.getPath();

        String scriptType = null;

        boolean inHeader = true;

        Scanner scan = new Scanner(file);
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            // Always skip empty lines.
            if (line.matches("\b\\s*\b")) {
                continue;
            }

            String[] pData = LineReader.breakDownLine(line);
            if (pData.length == 0) {
                continue;
            }
            if (scriptType == null && !LineReader.isHeader(pData)) {
                throw new IllegalArgumentException("Script type not declared!");
            }

            if (LineReader.isHeader(pData)) {
                if (!inHeader) {
                    // TODO: Error since we're not allowed to use headers past the first line of script
                    throw new IllegalArgumentException("");
                }
                switch (LineReader.getHeaderPrefix(pData)) {
                    case "type":
                        scriptType = LineReader.getScriptType(pData);
                        if (scriptType == null) {
                            // TODO: Error
                            throw new IllegalArgumentException();
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid script header: " + line);
                }
            }
            // TODO: Read the line
        }
        scan.close();
    }
}
