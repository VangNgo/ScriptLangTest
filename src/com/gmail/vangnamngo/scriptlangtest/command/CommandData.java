package com.gmail.vangnamngo.scriptlangtest.command;

import com.gmail.vangnamngo.scriptlangtest.object.AbstractObject;

import java.util.Map;

/**
 * Controls extra data for commands.
 * <p>Objects created from this class shouldn't persist for any longer than is required to execute a command.</p>
 */
public class CommandData {

    private Map<String, Boolean> flags;
    private Map<String, AbstractObject> data;

    /**
     * Fetches a boolean flag.
     * @param flag The name of the flag.
     * @return The boolean value of the flag.
     */
    public Boolean getFlag(String flag) {
        return flags.get(flag);
    }

    /**
     * Sets a boolean flag.
     * @param flag The name of the flag.
     * @param state The boolean value to assign to the flag.
     */
    public void setFlag(String flag, boolean state) {
        flags.put(flag, state);
    }

    /**
     * Removes a boolean flag.
     * @param flag The name of the flag.
     */
    public void removeFlag(String flag) {
        flags.remove(flag);
    }

    /**
     * Fetches any extra data assign to a flag.
     * @param name The name of the flag.
     * @return The {@link AbstractObject} assigned to this flag.
     */
    public AbstractObject getExtraData(String name) {
        return data.get(name);
    }

    /**
     * Sets extra data to a flag.
     * @param name The name of the flag.
     * @param obj The {@link AbstractObject} to assign to this flag.
     */
    public void setExtraData(String name, AbstractObject obj) {
        data.put(name, obj);
    }

    /**
     * Deletes a flag with extra data.
     * @param name The name of the flag.
     */
    public void deleteExtraData(String name) {
        data.remove(name);
    }
}
