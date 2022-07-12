package net.querz.mcaselector.version.anvil114;

import net.querz.mcaselector.version.NbtHelper;
import net.querz.mcaselector.version.anvil113.Anvil113ChunkHandler;
import net.querz.nbt.*;

public class Anvil114ChunkHandler extends Anvil113ChunkHandler {

	@Override
	public ByteTag getLightPopulated(CompoundTag data) {
		return NbtHelper.tagFromLevelFromRoot(data, "isLightOn");
	}

	@Override
	public void setLightPopulated(CompoundTag data, byte lightPopulated) {
		CompoundTag level = NbtHelper.levelFromRoot(data);
		if (level != null) {
			level.putLong("isLightOn", lightPopulated);
		}
	}
}
