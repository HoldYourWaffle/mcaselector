package net.querz.mcaselector.io.mca;

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
