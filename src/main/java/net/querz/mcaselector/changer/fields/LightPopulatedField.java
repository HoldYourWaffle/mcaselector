package net.querz.mcaselector.changer.fields;

import net.querz.mcaselector.changer.Field;
import net.querz.mcaselector.changer.FieldType;
import net.querz.mcaselector.io.anvil.chunk.ChunkData;
import net.querz.mcaselector.version.ChunkHandler;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.ByteTag;

public class LightPopulatedField extends Field<Byte> {

	public LightPopulatedField() {
		super(FieldType.LIGHT_POPULATED);
	}

	@Override
	public Byte getOldValue(ChunkData data) {
		ChunkHandler chunkHandler = VersionController.getChunkHandler(data.getDataVersion());
		ByteTag lightPopulated = chunkHandler.getLightPopulated(data.region().getData());
		return lightPopulated == null ? null : lightPopulated.asByte();
	}

	@Override
	public boolean parseNewValue(String s) {
		if ("1".equals(s)) {
			setNewValue((byte) 1);
			return true;
		} else if ("0".equals(s)) {
			setNewValue((byte) 0);
			return true;
		}
		return super.parseNewValue(s);

	}

	@Override
	public void change(ChunkData data) {
		ChunkHandler chunkHandler = VersionController.getChunkHandler(data.getDataVersion());
		ByteTag tag = chunkHandler.getLightPopulated(data.region().getData());
		if (tag != null) {
			chunkHandler.setLightPopulated(data.region().getData(), getNewValue());
		}
	}

	@Override
	public void force(ChunkData data) {
		ChunkHandler chunkHandler = VersionController.getChunkHandler(data.getDataVersion());
		chunkHandler.setLightPopulated(data.region().getData(), getNewValue());
	}
}
