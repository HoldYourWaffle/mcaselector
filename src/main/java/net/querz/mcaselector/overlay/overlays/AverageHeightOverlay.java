package net.querz.mcaselector.overlay.overlays;

import net.querz.mcaselector.io.anvil.chunk.ChunkData;
import net.querz.mcaselector.overlay.Overlay;
import net.querz.mcaselector.overlay.OverlayType;
import net.querz.mcaselector.tile.Tile;
import net.querz.mcaselector.version.ChunkHandler;
import net.querz.mcaselector.version.VersionController;

public class AverageHeightOverlay extends Overlay {

	private static final int MIN_VALUE = -64;
	private static final int MAX_VALUE = 320;

	public AverageHeightOverlay() {
		super(OverlayType.AVERAGE_HEIGHT);
	}

	@Override
	public int parseValue(ChunkData chunkData) {
		if (chunkData.region() == null || chunkData.region().getData() == null) {
			return 0;
		}

		int[] heightmap = VersionController.getChunkHandler(chunkData.getDataVersion()).getHeightmap(chunkData.region().getData(), ChunkHandler.HeightmapType.WORLD_SURFACE);
		int totalHeight = 0;
		for (int height : heightmap) {
			totalHeight += height;
		}
		return totalHeight / (Tile.CHUNK_SIZE * Tile.CHUNK_SIZE);
	}

	@Override
	public String name() {
		return "AverageHeight";
	}

	@Override
	public boolean setMin(String raw) {
		setRawMin(raw);
		try {
			int value = Integer.parseInt(raw);
			if (value < MIN_VALUE || value > MAX_VALUE) {
				return setMin((Integer) null);
			}
			return setMin(value);
		} catch (NumberFormatException ex) {
			return setMin((Integer) null);
		}
	}

	@Override
	public boolean setMax(String raw) {
		setRawMax(raw);
		try {
			int value = Integer.parseInt(raw);
			if (value < MIN_VALUE || value > MAX_VALUE) {
				return setMax((Integer) null);
			}
			return setMax(value);
		} catch (NumberFormatException ex) {
			return setMax((Integer) null);
		}
	}
}
