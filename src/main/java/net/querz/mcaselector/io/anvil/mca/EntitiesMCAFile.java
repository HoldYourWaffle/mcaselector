package net.querz.mcaselector.io.anvil.mca;

import net.querz.mcaselector.io.anvil.McaType;
import net.querz.mcaselector.io.anvil.chunk.EntitiesChunk;

import java.io.File;

public class EntitiesMCAFile extends MCAFile<EntitiesChunk> {

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
