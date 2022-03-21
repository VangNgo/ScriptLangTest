package com.gmail.vangnamngo.scriptlangtest.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileParser {
    public static void executeFile(File file) {
        if (file == null) {
            return;
        }

        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
            }
        }
        catch (FileNotFoundException fnfe) {
            //
        }
    }
}
