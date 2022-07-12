package net.querz.mcaselector.changer.fields;

import net.querz.mcaselector.changer.Field;
import net.querz.mcaselector.changer.FieldType;
import net.querz.mcaselector.io.anvil.chunk.ChunkData;
import net.querz.mcaselector.version.ChunkHandler;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.LongTag;

public class InhabitedTimeField extends Field<Long> {

	public InhabitedTimeField() {
		super(FieldType.INHABITED_TIME);
	}

	@Override
	public Long getOldValue(ChunkData data) {
		ChunkHandler chunkHandler = VersionController.getChunkHandler(data.getDataVersion());
		LongTag inhabitedTime = chunkHandler.getInhabitedTime(data.region().getData());
		return inhabitedTime == null ? null : inhabitedTime.asLong();
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
		LongTag tag = chunkHandler.getInhabitedTime(data.region().getData());
		if (tag != null) {
			chunkHandler.setInhabitedTime(data.region().getData(), getNewValue());
		}
	}

	@Override
	public void force(ChunkData data) {
		ChunkHandler chunkHandler = VersionController.getChunkHandler(data.getDataVersion());
		chunkHandler.setInhabitedTime(data.region().getData(), getNewValue());
	}
}
