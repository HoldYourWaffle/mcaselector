package net.querz.mcaselector.io.mca;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static <V> Map<McaType, V> mapTo(Supplier<V> values) {
        return mapTo(type -> values.get());
    }

    public static abstract class McaTyped {

        public abstract McaType getType();

        /**
         * Java's type system isn't sophisticated enough to correctly type two wildcard McaTyped-instances with the same McaType as being "equal".
         * For example:
         * <pre>
         *     MCAFile<?> a = MCAFile.createForType(McaType.REGION, ...);
         *     MCAFile<?> b = MCAFile.createForType(McaType.REGION, ...);
         *
         *     a.mergeChunksInto(b, ...);
         *     // Error! a's "?"-type is not the same as b's :(
         * </pre>
         *
         * This helper method pulls some trickery to get the compiler to shut up.
         *
         * @see <a href="https://stackoverflow.com/q/20543966">Stackoverflow - "incompatible types and fresh type-variable"</a>
         * @see <a href="https://stackoverflow.com/q/49534708">Stackoverflow - "Java generics - cast assignable capture type to subclass"</a>
         * @see <a href="https://docs.oracle.com/javase/tutorial/java/generics/capture.html">Oracle - "Wildcard Capture and Helper Methods"</a>
         *
         * @see net.querz.mcaselector.io.job.ChunkImporter.MCAChunkImporterProcessJob#execute
         */
        @SuppressWarnings({"unchecked", "JavadocReference"})
        public static <T extends McaTyped, U extends McaTyped> void withEqualType(T t, U u, BiConsumer<T, T> func) {
            // XXX this creates an implicit contract that equal McaType's imply assignability

            // For some reason the BiConsumer is absolutely necessary?
            if (t.getType() == u.getType()) {
                func.accept(t, (T) u);
            } else {
                throw new ClassCastException("McaType mismatch: "+t.getType()+" and "+u.getType());
            }
        }

    }

}
