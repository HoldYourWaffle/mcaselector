package net.querz.mcaselector.io.mca;

import net.querz.mcaselector.changer.Field;
import net.querz.mcaselector.filter.Filter;
import net.querz.mcaselector.io.ByteArrayPointer;
import net.querz.mcaselector.io.FileHelper;
import net.querz.mcaselector.io.RegionDirectories;
import net.querz.mcaselector.point.Point2i;
import net.querz.mcaselector.point.Point3i;
import net.querz.mcaselector.progress.Timer;
import net.querz.mcaselector.range.Range;
import net.querz.mcaselector.selection.ChunkSet;
import net.querz.mcaselector.selection.Selection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.List;

// holds data for chunks, poi and entities
public class Region {

	private static final Logger LOGGER = LogManager.getLogger(Region.class);

	private RegionMCAFile region;
	private PoiMCAFile poi;
	private EntitiesMCAFile entities;

	private RegionDirectories directories;

	private Point2i location;

	public static Region loadRegion(RegionDirectories dirs, byte[] regionData, byte[] poiData, byte[] entitiesData) throws IOException {
		Region r = new Region();
		if (dirs.getDirectory(McaType.REGION) != null && dirs.getDirectory(McaType.REGION).length() > FileHelper.HEADER_SIZE && regionData != null) {
			r.loadRegion(dirs.getDirectory(McaType.REGION), new ByteArrayPointer(regionData));
		}
		if (dirs.getDirectory(McaType.POI) != null && poiData != null) {
			r.loadPoi(dirs.getDirectory(McaType.POI), new ByteArrayPointer(poiData));
		}
		if (dirs.getDirectory(McaType.ENTITIES) != null && entitiesData != null) {
			r.loadEntities(dirs.getDirectory(McaType.ENTITIES), new ByteArrayPointer(entitiesData));
		}
		r.location = dirs.getLocation();
		r.directories = dirs;
		return r;
	}

	public static Region loadRegion(RegionDirectories dirs) throws IOException {
		Region r = new Region();
		if (dirs.getDirectory(McaType.REGION) != null) {
			r.loadRegion(dirs.getDirectory(McaType.REGION));
		}
		if (dirs.getDirectory(McaType.POI) != null) {
			r.loadPoi(dirs.getDirectory(McaType.POI));
		}
		if (dirs.getDirectory(McaType.ENTITIES) != null) {
			r.loadEntities(dirs.getDirectory(McaType.ENTITIES));
		}
		r.location = dirs.getLocation();
		r.directories = dirs;
		return r;
	}

	public static Region loadRegionHeaders(RegionDirectories dirs, byte[] regionHeader, byte[] poiHeader, byte[] entitiesHeader) throws IOException {
		Region r = new Region();
		if (dirs.getDirectory(McaType.REGION) != null && regionHeader != null) {
			r.region = new RegionMCAFile(dirs.getDirectory(McaType.REGION));
			r.region.loadHeader(new ByteArrayPointer(regionHeader));
		}
		if (dirs.getDirectory(McaType.POI) != null && poiHeader != null) {
			r.poi = new PoiMCAFile(dirs.getDirectory(McaType.POI));
			r.poi.loadHeader(new ByteArrayPointer(poiHeader));
		}
		if (dirs.getDirectory(McaType.ENTITIES) != null && entitiesHeader != null) {
			r.entities = new EntitiesMCAFile(dirs.getDirectory(McaType.ENTITIES));
			r.entities.loadHeader(new ByteArrayPointer(entitiesHeader));
		}
		r.directories = dirs;
		return r;
	}

	public static Region loadOrCreateEmptyRegion(RegionDirectories dirs) throws IOException {
		Region r = new Region();
		if (dirs.getDirectory(McaType.REGION) != null) {
			if (dirs.getDirectory(McaType.REGION).exists()) {
				r.loadRegion(dirs.getDirectory(McaType.REGION));
			} else {
				r.region = new RegionMCAFile(dirs.getDirectory(McaType.REGION));
			}
		}
		if (dirs.getDirectory(McaType.POI) != null) {
			if (dirs.getDirectory(McaType.POI).exists()) {
				r.loadPoi(dirs.getDirectory(McaType.POI));
			} else {
				r.poi = new PoiMCAFile(dirs.getDirectory(McaType.POI));
			}
		}
		if (dirs.getDirectory(McaType.ENTITIES) != null) {
			if (dirs.getDirectory(McaType.ENTITIES).exists()) {
				r.loadEntities(dirs.getDirectory(McaType.ENTITIES));
			} else {
				r.entities = new EntitiesMCAFile(dirs.getDirectory(McaType.ENTITIES));
			}
		}
		return r;
	}

	public void loadRegion(File src) throws IOException {
		region = new RegionMCAFile(src);
		region.load();
	}

	public void loadRegion(File src, ByteArrayPointer ptr) throws IOException {
		region = new RegionMCAFile(src);
		region.load(ptr);
	}

	public void loadPoi(File src) throws IOException {
		poi = new PoiMCAFile(src);
		poi.load();
	}

	public void loadPoi(File src, ByteArrayPointer ptr) throws IOException {
		poi = new PoiMCAFile(src);
		poi.load(ptr);
	}

	public void loadEntities(File src) throws IOException {
		entities = new EntitiesMCAFile(src);
		entities.load();
	}

	public void loadEntities(File src, ByteArrayPointer ptr) throws IOException {
		entities = new EntitiesMCAFile(src);
		entities.load(ptr);
	}

	public MCAFile<?> getRegion(McaType type) {
		// XXX there might be legitimate use-cases for non-generic access to a specific MCAFile implementation instance

		return switch(type) {
			case REGION -> region;
			case POI -> poi;
			case ENTITIES -> entities;
		};
	}

	public void setRegion(RegionMCAFile region) {
		this.region = region;
	}

	public void setPoi(PoiMCAFile poi) {
		this.poi = poi;
	}

	public void setEntities(EntitiesMCAFile entities) {
		this.entities = entities;
	}

	public boolean isEmpty() {
		boolean empty = true;
		if (region != null) {
			empty = region.isEmpty();
		}
		if (poi != null && empty) {
			empty = poi.isEmpty();
		}
		if (entities != null && empty) {
			empty = entities.isEmpty();
		}
		return empty;
	}

	public void setDirectories(RegionDirectories dirs) {
		if (region != null) {
			region.setFile(dirs.getDirectory(McaType.REGION));
		}
		if (poi != null) {
			poi.setFile(dirs.getDirectory(McaType.POI));
		}
		if (entities != null) {
			entities.setFile(dirs.getDirectory(McaType.ENTITIES));
		}
	}

	public ChunkData getChunkDataAt(Point2i location) {
		RegionChunk regionChunk = null;
		PoiChunk poiChunk = null;
		EntitiesChunk entitiesChunk = null;
		if (region != null) {
			regionChunk = region.getChunkAt(location);
		}
		if (poi != null) {
			poiChunk = poi.getChunkAt(location);
		}
		if (entities != null) {
			entitiesChunk = entities.getChunkAt(location);
		}
		return new ChunkData(regionChunk, poiChunk, entitiesChunk);
	}

	public ChunkData getChunkData(int index) {
		RegionChunk regionChunk = null;
		PoiChunk poiChunk = null;
		EntitiesChunk entitiesChunk = null;
		if (region != null) {
			regionChunk = region.getChunk(index);
		}
		if (poi != null) {
			poiChunk = poi.getChunk(index);
		}
		if (entities != null) {
			entitiesChunk = entities.getChunk(index);
		}
		Point2i location = this.location == null ? new Point2i() : new Point2i(index).add(this.location.regionToChunk());
		return new ChunkData(regionChunk, poiChunk, entitiesChunk);
	}

	public void setChunkDataAt(ChunkData chunkData, Point2i location) {
		if (region == null && directories.getDirectory(McaType.REGION) != null) {
			region = new RegionMCAFile(directories.getDirectory(McaType.REGION));
		}
		if (poi == null && directories.getDirectory(McaType.POI) != null) {
			poi = new PoiMCAFile(directories.getDirectory(McaType.POI));
		}
		if (entities == null && directories.getDirectory(McaType.ENTITIES) != null) {
			entities = new EntitiesMCAFile(directories.getDirectory(McaType.ENTITIES));
		}
		if (region != null) {
			region.setChunkAt(location, chunkData.region());
		}
		if (poi != null) {
			poi.setChunkAt(location, chunkData.poi());
		}
		if (entities != null) {
			entities.setChunkAt(location, chunkData.entities());
		}
	}

	public void save() throws IOException {
		if (region != null) {
			region.save();
		}
		if (poi != null) {
			poi.save();
		}
		if (entities != null) {
			entities.save();
		}
	}

	public void saveWithTempFiles() throws IOException {
		if (region != null) {
			region.saveWithTempFile();
		}
		if (poi != null) {
			poi.saveWithTempFile();
		}
		if (entities != null) {
			entities.saveWithTempFile();
		}
	}

	public void saveWithTempFiles(RegionDirectories dest) throws IOException {
		if (region != null) {
			region.saveWithTempFile(dest.getDirectory(McaType.REGION));
		}
		if (poi != null) {
			poi.saveWithTempFile(dest.getDirectory(McaType.POI));
		}
		if (entities != null) {
			entities.saveWithTempFile(dest.getDirectory(McaType.ENTITIES));
		}
	}

	public void deFragment() throws IOException {
		if (region != null) {
			region.deFragment();
		}
		if (poi != null) {
			poi.deFragment();
		}
		if (entities != null) {
			entities.deFragment();
		}
	}

	public void deFragment(RegionDirectories dest) throws IOException {
		if (region != null) {
			region.deFragment(dest.getDirectory(McaType.REGION));
		}
		if (poi != null) {
			poi.deFragment(dest.getDirectory(McaType.POI));
		}
		if (entities != null) {
			entities.deFragment(dest.getDirectory(McaType.ENTITIES));
		}
	}

	public void deleteFiles() {
		if (directories.getDirectory(McaType.REGION) != null && directories.getDirectory(McaType.REGION).exists()) {
			directories.getDirectory(McaType.REGION).delete();
		}
		if (directories.getDirectory(McaType.POI) != null && directories.getDirectory(McaType.POI).exists()) {
			directories.getDirectory(McaType.POI).delete();
		}
		if (directories.getDirectory(McaType.ENTITIES) != null && directories.getDirectory(McaType.ENTITIES).exists()) {
			directories.getDirectory(McaType.ENTITIES).delete();
		}
	}

	public void deleteChunks(ChunkSet selection) {
		if (region != null) {
			region.deleteChunks(selection);
		}
		if (poi != null) {
			poi.deleteChunks(selection);
		}
		if (entities != null) {
			entities.deleteChunks(selection);
		}
	}

	public boolean deleteChunks(Filter<?> filter, Selection selection) {
		boolean deleted = false;
		for (int i = 0; i < 1024; i++) {
			RegionChunk region = this.region.getChunk(i);
			EntitiesChunk entities = this.entities == null ? null : this.entities.getChunk(i);
			PoiChunk poi = this.poi == null ? null : this.poi.getChunk(i);

			if (region == null || region.isEmpty() || selection != null && !selection.isAnyChunkInRegionSelected(region.getAbsoluteLocation())) {
				continue;
			}

			ChunkData filterData = new ChunkData(region, poi, entities);

			Point2i location = region.getAbsoluteLocation();
			if (location == null) {
				continue;
			}

			if ((selection == null || selection.isChunkSelected(location)) && filter.matches(filterData)) {
				deleteChunkIndex(i);
				deleted = true;
			}
		}
		return deleted;
	}

	public boolean keepChunks(Filter<?> filter, Selection selection) {
		boolean deleted = false;
		for (int i = 0; i < 1024; i++) {
			RegionChunk region = this.region.getChunk(i);
			EntitiesChunk entities = this.entities == null ? null : this.entities.getChunk(i);
			PoiChunk poi = this.poi == null ? null : this.poi.getChunk(i);

			if (region == null || region.isEmpty()) {
				continue;
			}

			ChunkData filterData = new ChunkData(region, poi, entities);

			Point2i location = region.getAbsoluteLocation();
			if (location == null) {
				continue;
			}

			// keep chunk if filter AND selection applies
			// ignore selection if it's null
			if (!filter.matches(filterData) || selection != null && !selection.isChunkSelected(location)) {
				deleteChunkIndex(i);
				deleted = true;
			}
		}
		return deleted;
	}

	private void deleteChunkIndex(int index) {
		if (this.region != null) {
			this.region.deleteChunk(index);
		}
		if (this.entities != null) {
			this.entities.deleteChunk(index);
		}
		if (this.poi != null) {
			this.poi.deleteChunk(index);
		}
	}

	public ChunkSet getFilteredChunks(Filter<?> filter, Selection selection) {
		ChunkSet chunks = new ChunkSet();

		for (int i = 0; i < 1024; i++) {
			RegionChunk regionChunk = this.region == null ? null : this.region.getChunk(i);
			EntitiesChunk entitiesChunk = this.entities == null ? null : this.entities.getChunk(i);
			PoiChunk poiChunk = this.poi == null ? null : this.poi.getChunk(i);

			ChunkData filterData = new ChunkData(regionChunk, poiChunk, entitiesChunk);

			Point2i chunkLocation = location.regionToChunk().add(new Point2i(i));

			try {
				if ((selection == null || selection.isChunkSelected(chunkLocation)) && filter.matches(filterData)) {
					chunks.set(i);
				}
			} catch (Exception ex) {
				LOGGER.warn("failed to select chunk {}: {}", chunkLocation, ex.getMessage());
			}
		}
		return chunks;
	}

	public void applyFieldChanges(List<Field<?>> fields, boolean force, Selection selection) {
		Timer t = new Timer();
		for (int x = 0; x < 32; x++) {
			for (int z = 0; z < 32; z++) {
				Point2i absoluteLocation = location.regionToChunk().add(x, z);
				ChunkData chunkData = getChunkDataAt(absoluteLocation);
				if (selection == null || selection.isChunkSelected(absoluteLocation)) {
					try {
						chunkData.applyFieldChanges(fields, force);
					} catch (Exception ex) {
						LOGGER.warn("failed to apply field changes to chunk {}: {}", absoluteLocation, ex.getMessage());
					}
				}
			}
		}
		LOGGER.debug("took {} to apply field changes to region {}", t, location);
	}

	public void mergeInto(Region region, Point3i offset, boolean overwrite, ChunkSet sourceChunks, ChunkSet targetChunks, List<Range> ranges) {
		if (this.region != null) {
			this.region.mergeChunksInto(region.region, offset, overwrite, sourceChunks, targetChunks, ranges);
		}
		if (this.poi != null) {
			this.poi.mergeChunksInto(region.poi, offset, overwrite, sourceChunks, targetChunks, ranges);
		}
		if (this.entities != null) {
			this.entities.mergeChunksInto(region.entities, offset, overwrite, sourceChunks, targetChunks, ranges);
		}
	}

	@Override
	protected Region clone() throws CloneNotSupportedException {
		Region clone = (Region) super.clone();
		if (region != null) {
			clone.region = region.clone();
		}
		if (poi != null) {
			clone.poi = poi.clone();
		}
		if (entities != null) {
			clone.entities = entities.clone();
		}
		if (directories != null) {
			clone.directories = directories.clone();
		}
		clone.location = location.clone();
		return clone;
	}
}
