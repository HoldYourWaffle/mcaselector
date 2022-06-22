package net.querz.mcaselector.io.job;

import net.querz.mcaselector.point.Point2i;
import net.querz.mcaselector.progress.Progress;
import net.querz.mcaselector.selection.Selection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public final class BiomeSelector {

	private static final Logger LOGGER = LogManager.getLogger(BiomeSelector.class);

	private int radius = 0;

	public void selectBiomes(Point2i startChunk, boolean mark, Consumer<Selection> callback, Progress progressChannel, boolean cli) {
		System.out.println("Selecting around "+startChunk);

		/*WorldDirectories wd = Config.getWorldDirs();
		RegionDirectories[] rd = wd.listRegions(selection);
		if (rd == null || rd.length == 0) {
			if (cli) {
				progressChannel.done("no files");
			} else {
				progressChannel.done(Translation.DIALOG_PROGRESS_NO_FILES.toString());
			}
			return;
		}

		JobHandler.clearQueues();

		progressChannel.setMax(rd.length);
		progressChannel.updateProgress(rd[0].getLocationAsFileName(), 0);

		for (RegionDirectories r : rd) {
			JobHandler.addJob(new MCASelectFilterProcessJob(r, filter, selection, callback, radius, progressChannel));
		}*/
	}

	/*private static class MCASelectFilterProcessJob extends ProcessDataJob {

		private final Progress progressChannel;
		private final GroupFilter filter;
		private final Selection selection;
		private final Consumer<Selection> callback;
		private final int radius;

		private MCASelectFilterProcessJob(RegionDirectories dirs, GroupFilter filter, Selection selection, Consumer<Selection> callback, int radius,  Progress progressChannel) {
			super(dirs, PRIORITY_LOW);
			this.filter = filter;
			this.selection = selection;
			this.callback = callback;
			this.progressChannel = progressChannel;
			this.radius = radius;
		}

		@Override
		public boolean execute() {
			// load all files
			Point2i location = getRegionDirectories().getLocation();

			if (!filter.appliesToRegion(location)) {
				LOGGER.debug("filter does not apply to region {}", getRegionDirectories().getLocation());
				progressChannel.incrementProgress(getRegionDirectories().getLocationAsFileName());
				return true;
			}

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
					progressChannel.incrementProgress(getRegionDirectories().getLocationAsFileName());
					return true;
				}

				ChunkSet chunks = region.getFilteredChunks(filter, this.selection);
				if (chunks.size() > 0) {
					if (chunks.size() == Tile.CHUNKS) {
						chunks = null;
					}
					Selection selection = new Selection();
					selection.addAll(location, chunks);

					selection.addRadius(radius, this.selection);

					callback.accept(selection);
				}
				LOGGER.debug("took {} to select chunks in {}", t, getRegionDirectories().getLocationAsFileName());
			} catch (Exception ex) {
				LOGGER.warn("error selecting chunks in {}", getRegionDirectories().getLocationAsFileName(), ex);
			}
			progressChannel.incrementProgress(getRegionDirectories().getLocationAsFileName());
			return true;
		}
	}*/

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
}
