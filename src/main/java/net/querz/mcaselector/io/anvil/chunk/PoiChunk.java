package net.querz.mcaselector.io.anvil.chunk;

import net.querz.mcaselector.io.anvil.McaType;
import net.querz.mcaselector.point.Point2i;

public class PoiChunk extends Chunk {

	public PoiChunk(Point2i absoluteLocation) {
		super(absoluteLocation);
	}

	@Override
	public PoiChunk clone() {
		return clone(PoiChunk::new);
	}

	@Override
	public McaType getType() {
		return McaType.POI;
	}

}
