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
 * <p>All created ScriptContext objects must have an associated {@link Script} or {@link ScriptGroup} object!</p>
 */
// TODO: Add scripts, debug context, and whatever might be needed.
public class ScriptContext {
    public final static ScriptContext GLOBAL = new ScriptContext();

    private ScriptContext parent = null;
    public final Script script;
    public final ScriptGroup group;

    // Global context constructor
    private ScriptContext() {
        script = null;
        group = null;
    }

    public ScriptContext(Script script) {
        this(script, GLOBAL, null);
    }

    public ScriptContext(Script script, ScriptContext parent) {
        this(script, parent, null);
    }

    public ScriptContext(ScriptGroup group) {
        this(null, GLOBAL, group);
    }

    public ScriptContext(ScriptGroup group, ScriptContext parent) {
        this(null, parent, null);
    }

    public ScriptContext(Script script, ScriptContext parent, ScriptGroup group) {
        this.script = script;
        this.parent = parent;
        this.group = script != null ? script.group : group;
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
    private final Map<String, VariableData> varMap = new HashMap<>();

    /**
     * Adds a public variable to this object and assigns {@link Main#NULL_OBJ} to that variable, if possible.
     * @param name The name of the variable to add.
     * @return False if another variable of the same name is present, true otherwise.
     */
    public boolean addVariable(@NotNull String name) {
        return addVariable(name, ProtectionModifier.PUBLIC, Main.NULL_OBJ);
    }

    /**
     * Adds a variable to this object and assigns {@link Main#NULL_OBJ} to that variable, if possible.
     * @param name The name of the variable to add.
     * @param protMod The {@link ProtectionModifier} to use for this variable.
     * @return False if another variable of the same name is present, true otherwise.
     */
    public boolean addVariable(@NotNull String name, ProtectionModifier protMod) {
        return addVariable(name, protMod, Main.NULL_OBJ);
    }

    /**
     * Adds a variable to this object, if possible.
     * @param name The name of the variable to add.
     * @param protMod The {@link ProtectionModifier} to use for this variable.
     * @param obj The object to associate with this variable.
     * @return False if another variable of the same name is present, true otherwise.
     */
    public boolean addVariable(@NotNull String name, ProtectionModifier protMod, AbstractObject obj) {
        return varMap.putIfAbsent(name, new VariableData(protMod, obj)) == null;
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
        context.varMap.get(name).value = newObj;
        return true;
    }

    /**
     * Removes an existing variable.
     * @param name The name of the variable to remove.
     * @return True if the variable has been successfully removed, false otherwise.
     */
    public boolean removeVariable(String name) {
        return removeVariable(name, 0);
    }

    /**
     * Removes an existing variable.
     * @param name The name of the variable to remove.
     * @param n The n-th order ScriptContext parent to begin the variable deletion from.
     * @return True if the variable has been successfully removed, false otherwise.
     */
    public boolean removeVariable(String name, int n) {
        ScriptContext context = getContextWithVar(getNthParent(this, n), name);
        return context != null && context.varMap.remove(name) != null;
    }

    /**
     * Fetch the value of an existing variable.
     * @param name The name of the variable.
     * @return The value of the variable.
     */
    public AbstractObject getVariable(String name) {
        return getVariable(name, 0);
    }

    /**
     * Fetch the value of an existing variable.
     * @param name The name of the variable.
     * @param n The n-th order ScriptContext parent to begin the variable search from.
     * @return The value of the variable.
     */
    public AbstractObject getVariable(String name, int n) {
        ScriptContext context = getContextWithVar(getNthParent(this, n), name);
        if (context == null) {
            // TODO: Error
            return null;
        }
        return context.varMap.get(name).value;
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

    /**
     * Represents the data associated with a given variable name.
     */
    public static class VariableData {
        public final ProtectionModifier protectionMod;
        public AbstractObject value;

        public VariableData(ProtectionModifier protectionMod, AbstractObject value) {
            this.protectionMod = protectionMod;
            this.value = value;
        }
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

    // ========================================================================
    // Housekeeping
    // ========================================================================

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
