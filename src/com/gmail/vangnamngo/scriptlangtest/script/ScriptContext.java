package com.gmail.vangnamngo.scriptlangtest.script;

import com.gmail.vangnamngo.scriptlangtest.object.AbstractObject;
import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Determines and manages the context of a given script, including variables, script groups, and
 */
public class ScriptContext {
    private ScriptContext parent = null;

    public ScriptContext() {
        // Default constructor
    }

    public ScriptContext(ScriptContext parent) {
        this.parent = parent;
    }

    public ScriptContext getParent() {
        return parent;
    }

    // ------------------------------------------------------------------------
    // Variable management
    // ------------------------------------------------------------------------
    private final Map<String, AbstractObject> varMap = new HashMap<>();

    public boolean addVariable(@NotNull String name, AbstractObject obj) {
        return varMap.putIfAbsent(name, obj) == null;
    }

    public boolean setVariable(@NotNull String name, AbstractObject newObj) {
        return setVariable(name, newObj, 0);
    }

    public boolean setVariable(@NotNull String name, AbstractObject newObj, int n) {
        ScriptContext context = getContextWithVar(getNthParent(this, n), name);
        if (context == null) {
            // TODO: Error
            return false;
        }
        context.varMap.put(name, newObj);
        return true;
    }

    public boolean removeVariable(String name) {
        return removeVariable(name, 0);
    }

    public boolean removeVariable(String name, int n) {
        ScriptContext context = getContextWithVar(getNthParent(this, n), name);
        return context != null && context.varMap.remove(name) != null;
    }

    public AbstractObject getVariable(String name) {
        return getVariable(name, 0);
    }

    public AbstractObject getVariable(String name, int n) {
        ScriptContext context = getContextWithVar(getNthParent(this, n), name);
        if (context == null) {
            // TODO: Error
            return null;
        }
        return context.varMap.get(name);
    }

    public Set<String> getVariableList() {
        return varMap.keySet();
    }

    // Searches for the ScriptContext child/parent with the specified variable.
    private static ScriptContext getContextWithVar(ScriptContext c, String name) {
        ScriptContext context = c;
        while (context != null && !context.varMap.containsKey(name)) {
            context = context.parent;
            if (context == null) {
                // TODO: Error
                return null;
            }
        }
        return context;
    }

    /**
     * Attempts to get the n-th order parent. If n is 0, then this method just returns the {@link ScriptContext}
     * object provided.
     * @param c The child ScriptContext object.
     * @param n The n-th order parent to search for (for example, 2 will search for the parent of the parent).
     * @return The n-th order parent.
     */
    public static ScriptContext getNthParent(ScriptContext c, int n) {
        ScriptContext context = c;
        for (int i = n; i > 0; i--) {
            if (context.parent == null) {
                // TODO: Throw error
                return null;
            }
            context = context.parent;
        }
        return context;
    }
}
