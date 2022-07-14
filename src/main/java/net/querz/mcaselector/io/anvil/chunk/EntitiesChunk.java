package net.querz.mcaselector.io.anvil.chunk;

import net.querz.mcaselector.io.anvil.McaType;
import net.querz.mcaselector.point.Point2i;

public class EntitiesChunk extends Chunk {

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
