package net.querz.mcaselector.io;

import net.querz.mcaselector.io.mca.McaType;
import net.querz.mcaselector.point.Point2i;
import java.io.File;

// TODO suspiciously similar to WorldDirectories
// MAINTAINER these are not directories?
public class RegionDirectories implements Cloneable {

	private Point2i location;
	private File region;
	private File poi;
	private File entities;

	private String locationAsFileName;

	public RegionDirectories() {}

	public RegionDirectories(Point2i location, File region, File poi, File entities) {
		this.location = location;
		this.region = region;
		this.poi = poi;
		this.entities = entities;
	}

	public void setDirectory(McaType type, File dir) {
		if (location == null) {
			location = FileHelper.parseMCAFileName(dir);
		}

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

	public Point2i getLocation() {
		return location;
	}

	public String getLocationAsFileName() {
		if (locationAsFileName == null) {
			return locationAsFileName = FileHelper.createMCAFileName(location);
		}
		return locationAsFileName;
	}

	public static RegionDirectories fromWorldDirectories(WorldDirectories wd, Point2i location) {
		String fileName = FileHelper.createMCAFileName(location);
		return new RegionDirectories(
				location,
				new File(wd.getDirectory(McaType.REGION), fileName),
				new File(wd.getDirectory(McaType.POI), fileName),
				new File(wd.getDirectory(McaType.ENTITIES), fileName)
		);
	}

	public boolean exists() {
		return region.exists() || poi.exists() || entities.exists();
	}

	@Override
	public String toString() {
		return "<region=" + region + ", poi=" + poi + ", entities=" + entities + ">";
	}

	@Override
	public RegionDirectories clone() throws CloneNotSupportedException {
		RegionDirectories clone = (RegionDirectories) super.clone();
		clone.location = location.clone();
		return clone;
	}
}
