package net.querz.mcaselector.io;

import net.querz.mcaselector.io.mca.McaType;
import net.querz.mcaselector.point.Point2i;
import net.querz.mcaselector.selection.Selection;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// XXX record-ify?
public class WorldDirectories implements Serializable, Cloneable {

	private File region;
	private File poi;
	private File entities;

	// XXX uninitialized?
	public WorldDirectories() {}

	public WorldDirectories(File region, File poi, File entities) {
		this.region = region;
		this.poi = poi;
		this.entities = entities;
	}

	public void setDirectory(McaType type, File dir) {
		switch (type) {
			case REGION -> region = dir;
			case POI -> poi = dir;
			case ENTITIES -> entities = dir;
		}
	}

	public File getDirectory(McaType type) {
		return switch (type) {
			case REGION -> region;
			case POI -> poi;
			case ENTITIES -> entities;
		};
	}

	public RegionDirectories makeRegionDirectories(Point2i region) {
		RegionDirectories rd = new RegionDirectories();
		rd.setDirectory(McaType.REGION, new File(this.region, FileHelper.createMCAFileName(region)));
		rd.setDirectory(McaType.POI, new File(this.poi, FileHelper.createMCAFileName(region)));
		rd.setDirectory(McaType.ENTITIES, new File(this.entities, FileHelper.createMCAFileName(region)));
		return rd;
	}

	public RegionDirectories[] listRegions(Selection selection) {
		Map<Point2i, RegionDirectories> regionDirectories = new HashMap<>();
		File[] r = this.region.listFiles((d, n) -> FileHelper.MCA_FILE_PATTERN.matcher(n).matches());
		if (r != null) {
			for (File f : r) {
				Point2i l = FileHelper.parseMCAFileName(f);
				if (selection == null || selection.isAnyChunkInRegionSelected(l.asLong())) {
					regionDirectories.put(l, new RegionDirectories(l, f, null, null));
				}
			}
		}

		if (this.entities != null) {
			File[] p = this.entities.listFiles((d, n) -> FileHelper.MCA_FILE_PATTERN.matcher(n).matches());
			if (p != null) {
				for (File f : p) {
					Point2i l = FileHelper.parseMCAFileName(f);
					if (selection == null || selection.isAnyChunkInRegionSelected(l.asLong())) {
						if (regionDirectories.containsKey(l)) {
							regionDirectories.get(l).setDirectory(McaType.ENTITIES, f);
						} else {
							regionDirectories.put(l, new RegionDirectories(l, null, f, null));
						}
					}
				}
			}
		}

		if (this.poi != null) {
			File[] e = this.poi.listFiles((d, n) -> FileHelper.MCA_FILE_PATTERN.matcher(n).matches());
			if (e != null) {
				for (File f : e) {
					Point2i l = FileHelper.parseMCAFileName(f);
					if (selection == null || selection.isAnyChunkInRegionSelected(l.asLong())) {
						if (regionDirectories.containsKey(l)) {
							regionDirectories.get(l).setDirectory(McaType.POI, f);
						} else {
							regionDirectories.put(l, new RegionDirectories(l, null, null, f));
						}
					}
				}
			}
		}

		return regionDirectories.values().toArray(new RegionDirectories[0]);
	}

	@Override
	public WorldDirectories clone() {
		try {
			return (WorldDirectories) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "<region=" + region + ", poi=" + poi + ", entities=" + entities + ">";
	}

	public boolean sharesDirectories(WorldDirectories other) {
		return region != null && region.equals(other.region) ||
				poi != null && poi.equals(other.poi) ||
				entities != null && entities.equals(other.entities);
	}
}