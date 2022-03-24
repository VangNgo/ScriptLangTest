package com.gmail.vangnamngo.scriptlangtest.script;

public class Script {
    public final String name;
    public final String directory;
    public final ScriptGroup group;

    private final String[] rawScript;
    private final ExtendedData[] compiledScript;

    public Script(String name, String[] rawScript) {
        this(name, rawScript, ".", null);
    }

    public Script(String name, String[] rawScript, ExtendedData[] compiledScript) {
        this(name, rawScript, ".", null);
    }

    public Script(String name, String[] rawScript, ExtendedData[] compiledScript, String directory, ScriptGroup group) {
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
            return data.clone();
        }

        public Integer lineToJumpTo() {
            return lineJump;
        }
    }
}
