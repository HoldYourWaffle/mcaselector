package net.querz.mcaselector.io.mca;

import java.io.File;

public class EntitiesMCAFile extends MCAFile<EntitiesChunk> {

	public EntitiesMCAFile(File file) {
		super(file, EntitiesChunk::new);
		super.chunks = new EntitiesChunk[1024];
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
