package net.querz.mcaselector.changer.fields;

import net.querz.mcaselector.changer.Field;
import net.querz.mcaselector.changer.FieldType;
import net.querz.mcaselector.io.anvil.chunk.ChunkData;
import net.querz.mcaselector.version.ChunkHandler;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.LongTag;

public class LastUpdateField extends Field<Long> {

	public LastUpdateField() {
		super(FieldType.LAST_UPDATE);
	}

	@Override
	public Long getOldValue(ChunkData data) {
		ChunkHandler chunkHandler = VersionController.getChunkHandler(data.getDataVersion());
		LongTag lastUpdate = chunkHandler.getLastUpdate(data.region().getData());
		return lastUpdate == null ? null : lastUpdate.asLong();
	}

	@Override
	public boolean parseNewValue(String s) {
		try {
			setNewValue(Long.parseLong(s));
			return true;
		} catch (NumberFormatException ex) {
			return super.parseNewValue(s);
		}
	}

	@Override
	public void change(ChunkData data) {
		ChunkHandler chunkHandler = VersionController.getChunkHandler(data.getDataVersion());
		LongTag tag = chunkHandler.getLastUpdate(data.region().getData());
		if (tag != null) {
			chunkHandler.setLastUpdate(data.region().getData(), getNewValue());
		}
	}

	@Override
	public void force(ChunkData data) {
		ChunkHandler chunkHandler = VersionController.getChunkHandler(data.getDataVersion());
		chunkHandler.setLastUpdate(data.region().getData(), getNewValue());
	}
}
