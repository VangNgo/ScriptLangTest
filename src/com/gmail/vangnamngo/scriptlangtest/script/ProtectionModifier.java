package com.gmail.vangnamngo.scriptlangtest.script;

public enum ProtectionModifier {
    PUBLIC,
    GROUP,
    DIRECTORY,
    PRIVATE;

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
                if (accessFrom.getScript() == null || accessed.getScript() == null) {
                    break;
                }
                if (accessFrom == ScriptContext.GLOBAL || accessFrom.getScript().getDirectory().equals(accessed.getScript().getDirectory())) {
                    return true;
                }
                break;
            case GROUP:
                // If the group of the accessor is a subgroup of the accessed, detect it!
                // Anyone can always access the GLOBAL script context
                ScriptContext f = accessFrom;
                while (f != null) {
                    if (f.getGroup() == accessFrom.getGroup()) {
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
