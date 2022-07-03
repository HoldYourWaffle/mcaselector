package net.querz.mcaselector.version;

import net.querz.mcaselector.point.Point3i;
import net.querz.nbt.CompoundTag;

public interface ChunkRelocator {

	boolean relocate(CompoundTag root, Point3i offset);

}
