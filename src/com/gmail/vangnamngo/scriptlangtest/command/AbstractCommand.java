package com.gmail.vangnamngo.scriptlangtest.command;

import com.gmail.vangnamngo.scriptlangtest.object.AbstractObject;

import java.util.Map;

public abstract class AbstractCommand {
    public AbstractCommand() {
        // TODO: Context?
    }
    public abstract void execute(Map<String, AbstractObject> args); // TODO: Execution arguments
}
