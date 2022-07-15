package net.querz.mcaselector.io.mca;

import java.io.File;

public non-sealed class EntitiesMCAFile extends MCAFile<EntitiesChunk> {

	public EntitiesMCAFile(File file) {
		super(file, EntitiesChunk.class);
	}

	@Override
	public EntitiesMCAFile clone() {
		return clone(EntitiesMCAFile::new);
	}

	@Override
	public McaType getType() {
		return McaType.ENTITIES;
	}

}
