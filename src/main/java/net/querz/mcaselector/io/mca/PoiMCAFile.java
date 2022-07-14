package net.querz.mcaselector.io.mca;

import java.io.File;

public class PoiMCAFile extends MCAFile<PoiChunk> {

	public PoiMCAFile(File file) {
		super(file, PoiChunk::new);
		super.chunks = new PoiChunk[1024];
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
