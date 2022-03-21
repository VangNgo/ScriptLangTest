package com.gmail.vangnamngo.scriptlangtest.corrections;

import com.gmail.vangnamngo.scriptlangtest.Main;
import com.gmail.vangnamngo.scriptlangtest.script.ScriptContext;

import java.util.HashSet;

public class Corrector {

    /**
     * Determines if a {@link ScriptContext} object is valid. The terms of validity are:
     * <ul>
     *     <li>no variable name is set to null;</li>
     *     <li>no variable value is set to null;</li>
     *     <li>there is no circular parenting;</li>
     *     <li>the final parent context is {@link Main#GLOBAL_SCRIPT_CONTEXT}.</li>
     * </ul>
     * @param context The ScriptContext to verify.
     * @return True if the ScriptContext object is valid, false otherwise.
     */
    public static boolean verifyScriptContext(ScriptContext context) {
        // Test to see if there are any null variable names
        if (context.getVariableList().contains(null)) {
            return false;
        }

        // Test to see if the final parent context is GLOBAL_SCRIPT_CONTEXT, and if there are any circular parents
        HashSet<ScriptContext> circleMap = new HashSet<>();
        while (context != Main.GLOBAL_SCRIPT_CONTEXT && context != null) {
            if (circleMap.contains(context)) {
                return false;
            }
            circleMap.add(context);
            context = context.getParent();
        }
        if (context != Main.GLOBAL_SCRIPT_CONTEXT) {
            return false;
        }
        return true;
    }

    /**
     * Attempts to repair a ScriptContext by performing the following actions:
     * <ul>
     *     <li>delete any variable with a null name;</li>
     *     <li>replace any null variable value with {@link com.gmail.vangnamngo.scriptlangtest.object.NullObject};</li>
     *     <li>if the parent is null AND the current {@link ScriptContext} object is not
     *         {@link Main#GLOBAL_SCRIPT_CONTEXT}, set the parent to {@link Main#GLOBAL_SCRIPT_CONTEXT}.</li>
     * </ul>
     * This method will not attempt to additionally fix the parents of this ScriptContext.
     * @param context The ScriptContext object to repair.
     */
    public static void repairScriptContext(ScriptContext context) {

    }
}
