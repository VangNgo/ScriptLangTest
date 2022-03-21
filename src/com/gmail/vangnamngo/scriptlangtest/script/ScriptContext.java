package com.gmail.vangnamngo.scriptlangtest.script;

import com.gmail.vangnamngo.scriptlangtest.Main;
import com.gmail.vangnamngo.scriptlangtest.object.AbstractObject;
import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * Determines and manages the context of any executed commands. Also acts as a "scope" to provide unambiguous
 * functionality to executed commands. ScriptContext objects handle the following data:
 * <ul>
 *     <li>Variables</li>
 *     <li>Associated script (to be implemented)</li>
 *     <li>Debug level (to be implemented)</li>
 * </ul>
 * <p>All created ScriptContext objects must have an associated {@link Script} object!</p>
 */
// TODO: Add scripts, debug context, and whatever might be needed.
public class ScriptContext {
    public final static ScriptContext GLOBAL = new ScriptContext();

    private ScriptContext parent = null;

    // Global context constructor
    private ScriptContext() {
        // Global context constructor
    }

    public ScriptContext(ScriptContext parent) {
        this.parent = parent;
    }

    /**
     * @return The parent of this object.
     */
    public ScriptContext getParent() {
        return parent;
    }

    // ------------------------------------------------------------------------
    // Variable management
    // ------------------------------------------------------------------------
    private final Map<String, AbstractObject> varMap = new HashMap<>();


    /**
     * Adds a variable to this object and assigns {@link Main#NULL_OBJ} to that variable, if possible.
     * @param name The name of the variable to add.
     * @return False if another variable of the same name is present, true otherwise.
     */
    public boolean addVariable(@NotNull String name) {
        return addVariable(name, Main.NULL_OBJ);
    }

    /**
     * Adds a variable to this object, if possible.
     * @param name The name of the variable to add.
     * @param obj The object to associate with this variable.
     * @return False if another variable of the same name is present, true otherwise.
     */
    public boolean addVariable(@NotNull String name, AbstractObject obj) {
        return varMap.putIfAbsent(name, obj) == null;
    }

    /**
     * Sets the value of an existing variable.
     * @param name The name of the variable whose value is to be overwritten.
     * @param newObj The new object to replace that value with.
     * @return False if the new value could not be set, true otherwise.
     */
    public boolean setVariable(@NotNull String name, AbstractObject newObj) {
        return setVariable(name, newObj, 0);
    }

    /**
     * Sets the value of an existing variable.
     * @param name The name of the variable whose value is to be overwritten.
     * @param newObj The new object to replace that value with.
     * @param n The n-th order ScriptContext parent to begin the variable replacement with.
     * @return False if the new value could not be set, true otherwise.
     */
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

    /**
     * Lists the variables this ScriptContext object recognizes. It will list variables in order from the newest child
     * to the oldest parent.
     * @return The list of variables this ScriptContext object recognizes.
     */
    public List<String> getVariableList() {
        List<String> list = new ArrayList<>(varMap.keySet());

        ScriptContext cc = this.parent;
        while (cc != null) {
            list.addAll(cc.varMap.keySet());
            cc = cc.parent;
        }
        return list;
    }

    /**
     * Returns the list of variables specific to this ScriptContext object.
     * @return A set of variables registered to this specific ScriptContext object.
     */
    public Set<String> getLocalVariables() {
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

    /**
     * Reverts this ScriptContext's parent to {@link ScriptContext#GLOBAL}.
     * DO NOT USE THIS METHOD ARBITRARILY!
     */
    public void orphan() {
        if (this == GLOBAL) {
            return;
        }
        parent = GLOBAL;
    }
}
