package net.querz.mcaselector.version;

import net.querz.mcaselector.point.Point3i;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.NumberTag;

public class ChunkHelper {
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
