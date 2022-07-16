package net.querz.mcaselector.io.mca;

import net.querz.mcaselector.point.Point2i;

public non-sealed class EntitiesChunk extends Chunk {

	public EntitiesChunk(Point2i absoluteLocation) {
		super(absoluteLocation);
	}

	@Override
	public EntitiesChunk clone() {
		return clone(EntitiesChunk::new);
	}

	@Override
	public McaType getType() {
		return McaType.ENTITIES;
	}

}
