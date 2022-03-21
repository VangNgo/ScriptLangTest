package com.gmail.vangnamngo.scriptlangtest;

import com.gmail.vangnamngo.scriptlangtest.script.ScriptContext;

import java.io.File;

public class Main {

    public final static ScriptContext GLOBAL_SCRIPT_CONTEXT = new ScriptContext();

    public static void main(String args[]) {
        File thisDir = new File(".");
    }
}
