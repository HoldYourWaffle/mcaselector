package net.querz.mcaselector.io.mca;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

// XXX there has to be a better name for this
public enum McaType {
    REGION, POI, ENTITIES;

    public static <V> Map<McaType, V> mapTo(Function<McaType, V> values) {
        EnumMap<McaType, V> map = new EnumMap<>(McaType.class);
        for (McaType type : McaType.values()) {
            map.put(type, values.apply(type));
        }
        return map;
    }

}
