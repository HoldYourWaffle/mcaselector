package net.querz.mcaselector.io.anvil.mca;

import net.querz.mcaselector.io.anvil.McaType;
import net.querz.mcaselector.io.anvil.chunk.PoiChunk;

import java.io.File;

public class PoiMCAFile extends MCAFile<PoiChunk> {

	public PoiMCAFile(File file) {
		super(file, PoiChunk.class);
	}

	@Override
	public PoiMCAFile clone() {
		return clone(PoiMCAFile::new);
	}

	@Override
	public McaType getType() {
		return McaType.POI;
	}

}
