package net.querz.mcaselector.version;

import net.querz.mcaselector.io.anvil.BlockState;
import net.querz.mcaselector.io.registry.BiomeRegistry;
import net.querz.mcaselector.range.Range;
import net.querz.nbt.ByteTag;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.IntTag;
import net.querz.nbt.ListTag;
import net.querz.nbt.LongTag;
import net.querz.nbt.NBTUtil;
import net.querz.nbt.StringTag;

import java.util.List;
import java.util.Map;

public interface ChunkHandler {
	// TODO audit other filter implementations for comparator consolidation

	BlockState[] getPaletteOfSection(CompoundTag sectionData);

	BiomeRegistry.BiomeIdentifier[] getBiomesOfSection(CompoundTag sectionData);

	// CHECK how does this handle 3D biomes?
	void changeBiome(CompoundTag data, BiomeRegistry.BiomeIdentifier biome);

	// CHECK what's force vs change?
	void forceBiome(CompoundTag data, BiomeRegistry.BiomeIdentifier biome);

	void replaceBlocks(CompoundTag data, Map<String, BlockReplaceData> replace);

	int[] getHeightmap(CompoundTag data, HeightmapType heightmapType);

	// value points to index in getPaletteOfSection
	int[] getBlockStatePointersOfSection(CompoundTag sectionData);

	// TODO unparsed nbt desirable here?
	ListTag getTileEntities(CompoundTag data);

	CompoundTag getStructureStarts(CompoundTag data);

	CompoundTag getStructureReferences(CompoundTag data);

	ListTag getSections(CompoundTag data);

	void deleteSections(CompoundTag data, List<Range> ranges);

	// TODO big get/set simple properties dupe? (1.18 snapshot special case)

	LongTag getInhabitedTime(CompoundTag data);

	void setInhabitedTime(CompoundTag data, long inhabitedTime);

	StringTag getStatus(CompoundTag data);

	void setStatus(CompoundTag data, String status);

	LongTag getLastUpdate(CompoundTag data);

	void setLastUpdate(CompoundTag data, long lastUpdate);

	IntTag getXPos(CompoundTag data);

	IntTag getYPos(CompoundTag data);

	IntTag getZPos(CompoundTag data);

	ByteTag getLightPopulated(CompoundTag data);

	void setLightPopulated(CompoundTag data, byte lightPopulated);

	/* end dupe */

	void forceBlending(CompoundTag data);

	class BlockReplaceData {
		// TODO make record type?

		private String name;
		private CompoundTag state;
		private CompoundTag tile;
		private final BlockReplaceType type;

		public BlockReplaceData(String name) {
			type = BlockReplaceType.NAME;
			this.name = name;
			state = new CompoundTag();
			state.putString("Name", name);
		}

		public BlockReplaceData(String name, CompoundTag tile) {
			type = BlockReplaceType.NAME_TILE;
			this.name = name;
			this.tile = tile;
			state = new CompoundTag();
			state.putString("Name", name);
		}

		public BlockReplaceData(CompoundTag state) {
			type = BlockReplaceType.STATE;
			this.state = state;
			name = state.getString("Name");
		}

		public BlockReplaceData(CompoundTag state, CompoundTag tile) {
			type = BlockReplaceType.STATE_TILE;
			this.state = state;
			this.tile = tile;
			name = state.getString("Name");
		}

		public BlockReplaceType getType() {
			return type;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setState(CompoundTag state) {
			this.state = state;
		}

		public CompoundTag getState() {
			return state;
		}

		public void setTile(CompoundTag tile) {
			this.tile = tile;
		}

		public CompoundTag getTile() {
			return tile;
		}

		@Override
		public String toString() {
			switch (type) {
				case NAME:
					if (name.startsWith("minecraft:")) {
						return name;
					} else {
						return "'" + name + "'";
					}
				case STATE:
					return NBTUtil.toSNBT(state);
				case STATE_TILE:
					return NBTUtil.toSNBT(state) + ";" + NBTUtil.toSNBT(tile);
				case NAME_TILE:
					if (name.startsWith("minecraft:")) {
						return name + ";" + NBTUtil.toSNBT(tile);
					} else {
						return "'" + name + "';" + NBTUtil.toSNBT(tile);
					}
				default:
					return null;
			}
		}
	}

	enum BlockReplaceType {
		NAME, STATE, STATE_TILE, NAME_TILE
	}

	enum HeightmapType {
		// XXX right place?
		MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES, OCEAN_FLOOR, OCEAN_FLOOR_WG, WORLD_SURFACE, WORLD_SURFACE_WG
	}
}
