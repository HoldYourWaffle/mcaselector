package net.querz.mcaselector.io.mca;

import net.querz.mcaselector.point.Point2i;
import net.querz.mcaselector.version.ChunkRenderer;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.CompoundTag;

import java.io.File;

public class RegionMCAFile extends MCAFile<RegionChunk> {

	public RegionMCAFile(File file) {
		super(file, RegionChunk.class);
	}

	private RegionMCAFile(Point2i location) {
		super(location);
	}

	public RegionMCAFile minimizeForRendering() {
		RegionMCAFile min = new RegionMCAFile(getLocation());
		min.setFile(getFile());
		min.chunks = new RegionChunk[1024];

		for (int index = 0; index < 1024; index++) {
			RegionChunk chunk = getChunk(index);
			if (chunk == null || chunk.data == null) {
				continue;
			}

			try {
				ChunkRenderer chunkRenderer = VersionController.getChunkRenderer(chunk.getDataVersion());
				CompoundTag minData = chunkRenderer.minimizeChunk(chunk.data);

				RegionChunk minChunk = new RegionChunk(chunk.absoluteLocation.clone());
				minChunk.data = minData;

				min.chunks[index] = minChunk;
			} catch (Exception ex) {
				min.chunks[index] = chunk;
			}
		}
		return min;
	}

	@Override
	public RegionMCAFile clone() {
		return clone(RegionMCAFile::new);
	}

	@Override
	public McaType getType() {
		return McaType.REGION;
	}

}
