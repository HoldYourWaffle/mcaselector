package net.querz.mcaselector.version.anvil115;

import net.querz.mcaselector.io.registry.BiomeRegistry;
import net.querz.mcaselector.version.NbtHelper;
import net.querz.mcaselector.version.anvil113.Anvil113ChunkHandler;
import net.querz.nbt.CompoundTag;
import java.util.Arrays;

public class Anvil115ChunkHandler extends Anvil113ChunkHandler {

	@Override
	public void forceBiome(CompoundTag data, BiomeRegistry.BiomeIdentifier biome) {
		CompoundTag level = NbtHelper.levelFromRoot(data);
		if (level != null) {
			int[] biomes = new int[1024];
			Arrays.fill(biomes, (byte) biome.getID());
			level.putIntArray("Biomes", biomes);
		}
	}
}
