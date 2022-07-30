package net.querz.mcaselector.io.anvil.chunk;

import net.querz.mcaselector.changer.Field;
import net.querz.mcaselector.point.Point3i;

import java.util.List;

public record ChunkData(RegionChunk region, PoiChunk poi, EntitiesChunk entities) {

	public boolean relocate(Point3i offset) {
		// XXX boolean return value is never used
		return relocateChunk(region, offset) && relocateChunk(poi, offset) && relocateChunk(entities, offset);
	}

	private boolean relocateChunk(Chunk c, Point3i offset) {
		if (c != null && c.getData() != null && c.getData().containsKey("DataVersion")) {
			return c.relocate(offset);
		}
		return true;
	}

	public void applyFieldChanges(List<Field<?>> fields, boolean force) {
		for (Field<?> field : fields) {
			if (force) {
				field.force(this);
			} else {
				field.change(this);
			}
		}
	}

	public int getDataVersion() {
		return region.getDataVersion();
	}

}
