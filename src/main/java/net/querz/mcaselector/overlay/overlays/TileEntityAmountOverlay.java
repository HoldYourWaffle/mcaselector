package net.querz.mcaselector.overlay.overlays;

import net.querz.mcaselector.io.anvil.chunk.ChunkData;
import net.querz.mcaselector.overlay.AmountParser;
import net.querz.mcaselector.overlay.OverlayType;
import net.querz.mcaselector.version.ChunkHandler;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.ListTag;

public class TileEntityAmountOverlay extends AmountParser {

	public TileEntityAmountOverlay() {
		super(OverlayType.TILE_ENTITY_AMOUNT);
	}

	@Override
	public int parseValue(ChunkData chunkData) {
		if (chunkData.region() == null) {
			return 0;
		}
		ChunkHandler chunkHandler = VersionController.getChunkHandler(chunkData.getDataVersion());
		ListTag tileEntities = chunkHandler.getTileEntities(chunkData.region().getData());
		return tileEntities == null ? 0 : tileEntities.size();
	}

	@Override
	public String name() {
		return "TileEntityAmount";
	}
}
