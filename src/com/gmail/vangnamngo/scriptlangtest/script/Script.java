package com.gmail.vangnamngo.scriptlangtest.script;

import java.util.Arrays;

public class Script {
    public final String name;
    public final String directory;
    public final ScriptGroup group;

    private final String[] rawScript;
    private final ExtendedData[] compiledScript;

    /*
    TODO: Allow scripts to be instantiated without properly pre-compiling them?
    public Script(String name, String[] rawScript) {
        this(name, rawScript, ".", null);
    }
     */

    public Script(String name, String directory, String[] rawScript, ExtendedData[] compiledScript) {
        this(name, directory, rawScript, compiledScript, null);
    }

    public Script(String name, String directory, String[] rawScript, ExtendedData[] compiledScript, ScriptGroup group) {
        this.name = name;
        this.rawScript = rawScript;
        this.compiledScript = compiledScript;
        this.directory = directory;
        this.group = group;
    }

    public String getRawLine(int line) {
        return (line < 1 || line > rawScript.length) ? null : rawScript[line - 1];
    }

    public final static class ExtendedData {
        private final String[] data;
        private Integer lineJump = null;

        public ExtendedData(String[] lineData) {
            data = lineData;
        }

        public String[] getData() {
            return Arrays.copyOf(data, data.length);
        }

        public void setLineToJumpTo(int line) {
            lineJump = line;
        }

        public boolean clearLineJumpData() {
            if (lineJump == null) {
                return false;
            }
            lineJump = null;
            return true;
        }

        public Integer lineToJumpTo() {
            return lineJump;
        }
    }
}
