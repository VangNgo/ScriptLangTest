package com.gmail.vangnamngo.scriptlangtest.command;

import com.gmail.vangnamngo.scriptlangtest.object.AbstractObject;
import com.gmail.vangnamngo.scriptlangtest.script.ScriptContext;

import java.util.Map;

public abstract class AbstractCommand {

    protected Map<String, AbstractObject> processedArgs;

    public AbstractCommand() {
        // TODO: Context?
    }

    public abstract boolean processArguments(ScriptContext context, Map<String, AbstractObject> args);

    public abstract void execute(ScriptContext context);
}
