package net.querz.mcaselector.io.anvil;

import net.querz.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public record BlockState(String name, Map<String, String> properties) {

    public BlockState(String name, CompoundTag propertiesTag) {
        this(name, new HashMap<>());

        if (propertiesTag != null) {
            for (String key : propertiesTag.keySet()) {
                // XXX nbt's getString swallows mismatched-types
                properties.put(key, propertiesTag.getString(key));
            }
        }
    }

}
