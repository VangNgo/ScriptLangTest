package com.gmail.vangnamngo.scriptlangtest.corrections;

import com.gmail.vangnamngo.scriptlangtest.script.ScriptContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * An all-in-one class meant to be able to repair any ScriptLangTest object that may require repairs.
 */
public class Corrector {

    private final static HashMap<ScriptContext, InvalidScriptContext> INVALID_SCRIPT_CONTEXTS = new HashMap<>();

    /**
     * Returns whether the provided {@link ScriptContext} object was seen as invalid by a lone execution of
     * {@link #verifyScriptContext(ScriptContext)}. Does not guarantee that it is still invalid by the time this
     * method is executed.
     * @param context The ScriptContext object to check.
     * @return Whether the provided ScriptContext object is registered as invalid.
     */
    public static boolean wasScriptContextSeenAsInvalid(ScriptContext context) {
        return INVALID_SCRIPT_CONTEXTS.containsKey(context);
    }

    /**
     * Returns the full set of data detailing why the provided {@link ScriptContext} object was seen as invalid by a
     * lone execution of {@link #verifyScriptContext(ScriptContext)}. The data provided may not necessarily be
     * up-to-date.
     * @param context The ScriptContext object to fetch data for.
     * @return A {@link InvalidScriptContext} object detailing why the provided ScriptContext object was seen as
     *         invalid, or null if the ScriptContext object isn't registered as invalid.
     */
    public static InvalidScriptContext getInvalidityDataOnScriptContext(ScriptContext context) {
        return INVALID_SCRIPT_CONTEXTS.get(context);
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
     *
     * <p>This method also caches which {@link ScriptContext} objects are invalid.</p>
     * @see #repairScriptContext(ScriptContext)
     * @see #repairAllInvalidScriptContexts()
     * @param context The ScriptContext to verify.
     * @return True if the ScriptContext object is valid, false otherwise.
     */
    public static boolean verifyScriptContext(ScriptContext context) {
        Set<String> varSet = context.getLocalVariables();

        // Test to see if there are any null variable names
        boolean hasNullVarName = false;
        HashSet<String> nullVarNames = null;
        if (varSet.contains(null)) {
            hasNullVarName = true;
        }

        // Test to see if there are any null variable values
        boolean hasNullVarVal = false;
        HashSet<String> nullVars = null;
        for (String varName : varSet) {
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
                public boolean hasCircularParentChain() {
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
     *     <li>replace any null variable value with {@link com.gmail.vangnamngo.scriptlangtest.Main#NULL_OBJ};</li>
     *     <li>if a circular parent chain exists, arbitrarily change the current {@link ScriptContext} object's parent
     *         to {@link ScriptContext#GLOBAL}; and</li>
     *     <li>if the final parent is not {@link ScriptContext#GLOBAL}, set the final parent to
     *         {@link ScriptContext#GLOBAL}.</li>
     * </ul>
     * If {@link #verifyScriptContext(ScriptContext)} has not been used yet, it will be run first to determine if the
     * provided {@link ScriptContext} object requires repairs. This method will not attempt to fix the parents of this
     * ScriptContext.
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
        if (isc.hasCircularParentChain()) {
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

    /**
     * Repairs all invalid {@link ScriptContext} objects cached by {@link #verifyScriptContext(ScriptContext)}.
     */
    public static void repairAllInvalidScriptContexts() {
        for (ScriptContext c : INVALID_SCRIPT_CONTEXTS.keySet()) {
            repairScriptContext(c);
        }
    }

    /**
     * Extra data for use with this class's cache of invalid {@link ScriptContext} objects.
     */
    public interface InvalidScriptContext {
        /**
         * Returns whether the {@link ScriptContext} object is invalid because of a circular parent chain.
         */
        boolean hasCircularParentChain();
        /**
         * Returns whether the {@link ScriptContext} object is invalid because of a null variable name.
         */
        boolean hasNullVariableNames();
        /**
         * Returns whether the {@link ScriptContext} object is invalid because of one or more null variable values.
         */
        boolean hasNullVariableValues();
        /**
         * Returns the names of the variables specific to the associated {@link ScriptContext} object that have a null
         * value.
         */
        HashSet<String> nullVariableValues();
        /**
         * Returns whether the {@link ScriptContext} object is invalid because its final parent isn't
         * {@link ScriptContext#GLOBAL}.
         */
        boolean finalParentIsBad();
        /**
         * Returns whether the final {@link ScriptContext} object in the parent chain of the associated
         * {@link ScriptContext} object.
         */
        ScriptContext finalParent();
    }
}
