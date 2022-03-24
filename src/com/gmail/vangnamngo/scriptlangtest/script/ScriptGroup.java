package com.gmail.vangnamngo.scriptlangtest.script;

import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * Represents a collection of scripts under a specific name.
 */
public class ScriptGroup {
    // TODO: When registering scripts, always ensure that they're in GLOBAL
    public final static ScriptGroup GLOBAL = new ScriptGroup();

    private final static Map<ScriptGroup, Set<ScriptGroup>> PARENT_CACHE = new HashMap<>();

    public final String name;
    public final ScriptContext groupContext;
    public final ScriptGroup parent;

    /*
     In order to preserve the integrity of a ScriptGroup, DO NOT allow direct public access to the set of scripts in it!
     The set of scripts in a given ScriptGroup should not arbitrarily change during runtime.
     */
    private final HashSet<Script> scriptSet = new HashSet<>();

    private ScriptGroup() {
        this.name = "Global";
        this.groupContext = ScriptContext.GLOBAL;
        this.parent = null;
    }

    public ScriptGroup(@NotNull String name) {
        this(name, null, null);
    }

    public ScriptGroup(@NotNull String name, ScriptGroup parent) {
        this(name, null, parent);
    }

    public ScriptGroup(@NotNull String name, Collection<Script> scripts) {
        this(name, scripts, null);
    }

    public ScriptGroup(@NotNull String name, Collection<Script> scripts, ScriptGroup parent) {
        this.name = name;
        this.parent = parent;
        groupContext = new ScriptContext(this, this.parent.groupContext);
        if (scripts != null) {
            scriptSet.addAll(scripts);
        }
    }

    public boolean hasScript(Script script) {
        return scriptSet.contains(script);
    }

    public boolean hasAnyScripts(Collection<Script> scripts) {
        for (Script s : scripts) {
            if (hasScript(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllScripts(Collection<Script> scripts) {
        return scriptSet.containsAll(scripts);
    }

    public final Set<Script> scripts() {
        return (HashSet<Script>) scriptSet.clone();
    }
}
