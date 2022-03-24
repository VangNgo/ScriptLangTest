package com.gmail.vangnamngo.scriptlangtest.script;

/**
 * Determines the level of protection a given variable or method has.
 */
// TODO: Consider script classes?
public enum ProtectionModifier {
    /**
     * Allows access from all scripts.
     */
    PUBLIC,

    /**
     * Allows access only from within a specific script group and its subgroups.
     */
    GROUP,

    /**
     * Allows access only from scripts that share the same folder. Scripts within subfolders are included.
     */
    DIRECTORY,

    /**
     * Only allows access from within the script.
     */
    PRIVATE;

    /**
     * Determines if a given script can access something from another script.
     * @param accessFrom The context of the script where the access request is coming from.
     * @param accessed The context of the script which is being requested for access.
     * @param accessedMod The protection level to determine if access should be allowed.
     * @return True if access is allowed, false otherwise.
     */
    public static boolean canAccessFrom(ScriptContext accessFrom, ScriptContext accessed, ProtectionModifier accessedMod) {
        // You cannot access nothing!
        if (accessed == null) {
            return false;
        }
        // If you're accessing from a null value, you will only be able to access public data.
        if (accessFrom == null) {
            return accessedMod == ProtectionModifier.PUBLIC;
        }

        switch (accessedMod) {
            case PRIVATE:
                if (accessFrom != accessed) {
                    break;
                }
            // DIRECTORY-level permissions require a script accessor!
            case DIRECTORY:
                if (accessFrom.script == null || accessed.script == null) {
                    break;
                }
                if (accessFrom == ScriptContext.GLOBAL || accessFrom.script.directory.startsWith(accessed.script.directory)) {
                    return true;
                }
                break;
            case GROUP:
                // If the group of the accessor is a subgroup of the accessed, detect it!
                // Anyone can always access the GLOBAL script context
                ScriptContext f = accessFrom;
                while (f != null) {
                    if (f.group == accessFrom.group) {
                        return true;
                    }
                    f = f.getParent();
                }
                break;
            // The default must always be PUBLIC!
            default:
                return true;
        }
        return false;
    }

    // TODO: Classes?
}
