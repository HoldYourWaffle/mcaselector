package net.querz.mcaselector.version;

import net.querz.mcaselector.point.Point3i;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.NumberTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ChunkHelper {

    public static Iterable<CompoundTag> iterateSections(ChunkHandler handler, CompoundTag data) {
        // TODO scan for additional potential usages
        return handler.getSections(data).iterateType(CompoundTag.TYPE);
    }

    // XXX there's probably a better name for this
    public static <T> List<T> combineSections(ChunkHandler handler, CompoundTag data, Function<CompoundTag, T[]> sectionMethod) {
        List<T> combined = new ArrayList<>();
        for (CompoundTag sectionData : ChunkHelper.iterateSections(handler, data)) {
            // OPTIMIZE is it faster to keep using arrays here, pre-allocating it with a second iteration?
            combined.addAll(Arrays.asList(sectionMethod.apply(sectionData)));
        }
        return combined;
    }

    public static boolean applyOffsetToSection(CompoundTag section, Point3i offset, int minY, int maxY) {
        NumberTag sectionY = NbtHelper.tagFromCompound(section, "Y");
        if (sectionY == null) {
            return true;
        } else if (sectionY.asByte() > maxY || sectionY.asByte() < minY) {
            return false;
        }

        int offsetY = sectionY.asByte() + offset.getY();
        if (offsetY > maxY || offsetY < minY) {
            return false;
        }

        section.putByte("Y", (byte) offsetY);
        return true;
    }

}
