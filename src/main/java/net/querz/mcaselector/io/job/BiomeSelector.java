package net.querz.mcaselector.io.job;

import net.querz.mcaselector.Config;
import net.querz.mcaselector.filter.Comparator;
import net.querz.mcaselector.filter.Filter;
import net.querz.mcaselector.filter.filters.BiomeFilter;
import net.querz.mcaselector.io.JobHandler;
import net.querz.mcaselector.io.RegionDirectories;
import net.querz.mcaselector.io.WorldDirectories;
import net.querz.mcaselector.io.mca.Region;
import net.querz.mcaselector.point.Point2i;
import net.querz.mcaselector.progress.Progress;
import net.querz.mcaselector.progress.Timer;
import net.querz.mcaselector.selection.ChunkSet;
import net.querz.mcaselector.selection.Selection;
import net.querz.mcaselector.tile.Tile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public final class BiomeSelector {

	private static final Logger LOGGER = LogManager.getLogger(BiomeSelector.class);

	private int radius = 0;

	public void selectBiomes(Point2i seedChunk, Consumer<Selection> callback, Progress progressChannel, boolean cli) {
		Selection seedChunkSelection = new Selection();
		seedChunkSelection.addChunk(seedChunk);

		WorldDirectories wd = Config.getWorldDirs();
		RegionDirectories[] rd = wd.listRegions(seedChunkSelection);
		if (rd == null || rd.length != 1) {
			// TODO implement more efficient singular WorldDirectories.listRegion?
			throw new IllegalStateException("Not just 1 region for seed chunk? "+(rd == null ? "null" : rd.length)+" regions found");
		}
		RegionDirectories regionDir = rd[0];

		/*if (rd == null || rd.length == 0) {
			CHECK does this mean that null might be an expected/legal result in some cases?
			if (cli) {
				progressChannel.done("no files");
			} else {
				progressChannel.done(Translation.DIALOG_PROGRESS_NO_FILES.toString());
			}
			return;
		}*/

		progressChannel.setMax(1);
		progressChannel.updateProgress(regionDir.getLocationAsFileName(), 0);

		// TODO implement inter-region flooding
		// TODO properly handle progress (chunk count?)

		JobHandler.clearQueues();
		JobHandler.addJob(new BiomeSelectorProcessJob(regionDir, seedChunk, callback, radius, progressChannel));
	}

	private static class BiomeSelectorProcessJob extends ProcessDataJob {

		private final Progress progressChannel;
		private final Consumer<Selection> callback;
		private final Point2i seedChunk;
		private final int radius;

		private BiomeSelectorProcessJob(RegionDirectories dir, Point2i seedChunk, Consumer<Selection> callback, int radius, Progress progressChannel) {
			super(dir, PRIORITY_LOW);
			this.seedChunk = seedChunk;
			this.callback = callback;
			this.progressChannel = progressChannel;
			this.radius = radius;
		}

		@Override
		public boolean execute() {
			// load all files
			Point2i location = getRegionDirectories().getLocation();

			// TODO major code duplication with ChunkFilterSelector

			byte[] regionData = loadRegion();
			byte[] poiData = loadPoi();
			byte[] entitiesData = loadEntities();

			if (regionData == null && poiData == null && entitiesData == null) {
				LOGGER.warn("failed to load any data from {}", getRegionDirectories().getLocationAsFileName());
				progressChannel.incrementProgress(getRegionDirectories().getLocationAsFileName());
				return true;
			}

			// load MCAFile
			Timer t = new Timer();
			try {
				Region region = Region.loadRegion(getRegionDirectories(), regionData, poiData, entitiesData);

				if (region.getRegion() == null) {
					//CHECK when does this happen?
					progressChannel.incrementProgress(getRegionDirectories().getLocationAsFileName());
					return true;
				}

				// SOON regular BiomeFilter can do 0% threshold, others would require a new method on ChunkFilter
				Filter<?> filter = new BiomeFilter();
				filter.setComparator(Comparator.CONTAINS);
				filter.setFilterValue("minecraft:swamp"); // NOW get clicked biome

				ChunkSet chunks = region.getFloodFilteredChunks(seedChunk, filter, null);
				if (chunks.size() > 0) {
					if (chunks.size() == Tile.CHUNKS) {
						chunks = null;
					}
					Selection selection = new Selection();
					selection.addAll(location, chunks);

					selection.addRadius(radius, null);

					callback.accept(selection);
				}
				LOGGER.debug("took {} to select chunks in {}", t, getRegionDirectories().getLocationAsFileName());
			} catch (Exception ex) {
				// XXX why is the exception swallowed?
				LOGGER.warn("error selecting chunks in {}", getRegionDirectories().getLocationAsFileName(), ex);
			}
			progressChannel.incrementProgress(getRegionDirectories().getLocationAsFileName());
			return true;
		}
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
}
