package net.querz.mcaselector.io.mca;

import net.querz.mcaselector.changer.Field;
import net.querz.mcaselector.filter.Filter;
import net.querz.mcaselector.io.ByteArrayPointer;
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
import java.util.Map;

// holds data for chunks, poi and entities
public class Region {

	private static final Logger LOGGER = LogManager.getLogger(Region.class);

	private RegionMCAFile region;
	private PoiMCAFile poi;
	private EntitiesMCAFile entities;

	private RegionDirectories directories;

	private Point2i location;

	public Region(RegionDirectories dirs, Map<McaType, byte[]> data) throws IOException {
		for (McaType type : McaType.values()) {
			File dir = dirs.getDirectory(type);
			byte[] typeData = data.get(type);

			if (dir != null && typeData != null) {
				// MAINTAINER why are we not loading the *supplied* data if the directory doesn't exist (yet)?
				setMcaFile(type, dir).load(new ByteArrayPointer(typeData));
			}
		}

		this.location = dirs.getLocation();
		this.directories = dirs;
	}

	public Region(RegionDirectories dirs) throws IOException {
		for (McaType type : McaType.values()) {
			File dir = dirs.getDirectory(type);
			if (dir == null) {
				continue;
			}
			setMcaFile(type, dir).load();
		}

		this.location = dirs.getLocation();
		this.directories = dirs;
	}


	// SOON de-dupe (static) constructors collectively
	private Region() {}

	public static Region loadRegionHeaders(RegionDirectories dirs, Map<McaType, byte[]> data) throws IOException {
		Region r = new Region();

		for (McaType type : McaType.values()) {
			File dir = dirs.getDirectory(type);
			byte[] typeData = data.get(type);

			if (dir != null && typeData != null) {
				// MAINTAINER why are we not loading the *supplied* data if the directory doesn't exist (yet)?
				r.setMcaFile(type, dir).loadHeader(new ByteArrayPointer(typeData));
			}
		}

		// MAINTAINER no location initialized?
		r.directories = dirs;
		return r;
	}

	public static Region loadOrCreateEmptyRegion(RegionDirectories dirs) throws IOException {
		Region r = new Region();

		for (McaType type : McaType.values()) {
			File dir = dirs.getDirectory(type);
			if (dir == null) {
				continue;
			}

			MCAFile<?> mca = r.setMcaFile(type, dir);
			if (dir.exists()) {
				mca.load();
			}
		}

		return r;
	}

	private MCAFile<?> setMcaFile(McaType type, File src) {
		/*
			NOTE At time of writing (JDK 17) JEP406's switch pattern matching is still a preview feature whose syntax could break in the future
				See #setRegion for the reasoning for using it anyway
		 */

		return switch (MCAFile.createForType(type, src)) {
			case RegionMCAFile f -> (region = f);
			case PoiMCAFile f -> (poi = f);
			case EntitiesMCAFile f -> (entities = f);
		};
	}

	public MCAFile<?> getRegion(McaType type) {
		// XXX there might be legitimate use-cases for non-generic access to a specific MCAFile implementation instance

		return switch(type) {
			case REGION -> region;
			case POI -> poi;
			case ENTITIES -> entities;
		};
	}

	public void setRegion(MCAFile<?> region) {
		/*
			NOTE At time of writing (JDK 17) JEP406's switch pattern matching is still a preview feature whose syntax could break in the future
				This seems very unlikely for such a simple use-case though, given that this basic syntax stays the same until at least JDK 19 (JEP-427)
				Meanwhile the advantages of enabling this syntax are plenty: no data duplication and no implicit coupling between McaType and a class (see previous commit)

			XXX A possible alternative "proper" solution: McaType-ify the fields itself using a Map (similar to VersionController)
				However, there might be legitimate use-cases for non-generic access to a specific MCAFile implementation instance
		 */

		@SuppressWarnings("unused")
		MCAFile<?> exhaustiveCheck = switch (region) {
			case RegionMCAFile r -> this.region = r;
			case PoiMCAFile r -> poi = r;
			case EntitiesMCAFile r -> entities = r;
		};
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
