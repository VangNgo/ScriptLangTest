package com.gmail.vangnamngo.scriptlangtest.corrections;

import com.gmail.vangnamngo.scriptlangtest.script.ScriptContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Corrector {

    private final static HashMap<ScriptContext, InvalidScriptContext> INVALID_SCRIPT_CONTEXTS = new HashMap<>();

    private interface InvalidScriptContext {
        boolean hasCircularLoop();
        boolean hasNullVariableNames();
        boolean hasNullVariableValues();
        HashSet<String> nullVariableValues();
        boolean finalParentIsBad();
        ScriptContext finalParent();
    }

    /**
     * Determines if a {@link ScriptContext} object is valid. The terms of validity are:
     * <ul>
     *     <li>no variable name is set to null;</li>
     *     <li>no variable value is set to null;</li>
     *     <li>there is no circular parent chain; and</li>
     *     <li>the final parent context is {@link ScriptContext#GLOBAL}.</li>
     * </ul>
     * This method does not test the parents of the provided {@link ScriptContext} object.
     * @param context The ScriptContext to verify.
     * @return True if the ScriptContext object is valid, false otherwise.
     */
    public static boolean verifyScriptContext(ScriptContext context) {
        Set<String> varList = context.getVariableList();
        // Test to see if there are any null variable names
        boolean hasNullVarName = false;
        HashSet<String> nullVarNames = null;
        if (varList.contains(null)) {
            hasNullVarName = true;
        }

        // Test to see if there are any null variable values
        boolean hasNullVarVal = false;
        HashSet<String> nullVars = null;
        for (String varName : varList) {
            if (context.getVariable(varName) == null) {
                hasNullVarVal = true;
                if (nullVars == null) {
                    nullVars = new HashSet<>();
                }
                nullVars.add(varName);
            }
        }

        // Test to see if there are any circular parents.
        boolean hasCircParent = false;
        ScriptContext cc = context;
        while (cc.getParent() != null) {
            if (cc.getParent() == context) {
                hasCircParent = true;
                break;
            }
            cc = cc.getParent();
        }

        // See if the last context in the previous loop is the global script context
        boolean finalParentIsBad = (cc != ScriptContext.GLOBAL);

        if (hasNullVarName || hasNullVarVal || hasCircParent || finalParentIsBad) {
            boolean fHCP = hasCircParent;
            boolean fHNVN = hasNullVarName;
            boolean fHNVV = hasNullVarVal;
            HashSet<String> fNVSet = nullVars;
            boolean fFPIV = finalParentIsBad;
            ScriptContext fCC = cc;
            INVALID_SCRIPT_CONTEXTS.put(context, new InvalidScriptContext() {
                @Override
                public boolean hasCircularLoop() {
                    return fHCP;
                }

                @Override
                public boolean hasNullVariableNames() {
                    return fHNVN;
                }

                @Override
                public boolean hasNullVariableValues() {
                    return fHNVV;
                }

                @Override
                public HashSet<String> nullVariableValues() {
                    return fNVSet;
                }

                @Override
                public boolean finalParentIsBad() {
                    return fFPIV;
                }

                @Override
                public ScriptContext finalParent() {
                    return fCC;
                }
            });
            return false;
        }
        return true;
    }

    /**
     * Attempts to repair a ScriptContext by performing the following actions:
     * <ul>
     *     <li>delete any variable with a null name;</li>
     *     <li>replace any null variable value with {@link com.gmail.vangnamngo.scriptlangtest.object.NullObject};</li>
     *     <li>if a circular parent chain exists, arbitrarily change the current {@link ScriptContext} object's parent
     *         to {@link ScriptContext#GLOBAL}; and</li>
     *     <li>if the final parent is not {@link ScriptContext#GLOBAL}, set the final parent to
     *         {@link ScriptContext#GLOBAL}.</li>
     * </ul>
     * If {@link Corrector#verifyScriptContext(ScriptContext)} has not been used yet, it will be run first to determine
     * if the provided {@link ScriptContext} object requires repairs. This method will not attempt to fix the parents of
     * this ScriptContext.
     * @param context The ScriptContext object to repair.
     */
    public static void repairScriptContext(ScriptContext context) {
        InvalidScriptContext isc = INVALID_SCRIPT_CONTEXTS.get(context);

        // If a verification hasn't been done beforehand, do so now.
        if (isc == null && verifyScriptContext(context)) {
            return;
        }

        // Begin attempts at repairs.
        if (isc.finalParentIsBad()) {
            isc.finalParent().orphan();
        }
        if (isc.hasCircularLoop()) {
            context.orphan();
        }
        if (isc.hasNullVariableNames()) {
            context.removeVariable(null);
        }
        if (isc.hasNullVariableValues()) {
            for (String nV : isc.nullVariableValues()) {
                context.removeVariable(nV);
            }
        }

        INVALID_SCRIPT_CONTEXTS.remove(context);
    }
}
