package net.querz.mcaselector.io.mca;

import net.querz.mcaselector.point.Point2i;

public non-sealed class RegionChunk extends Chunk {

	public RegionChunk(Point2i absoluteLocation) {
		super(absoluteLocation);
	}

	@Override
	public RegionChunk clone() {
		return clone(RegionChunk::new);
	}

	@Override
	public McaType getType() {
		return McaType.REGION;
	}

}
