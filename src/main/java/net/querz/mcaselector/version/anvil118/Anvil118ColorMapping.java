package net.querz.mcaselector.version.anvil118;

import net.querz.mcaselector.debug.Debug;
import net.querz.mcaselector.text.TextHelper;
import net.querz.mcaselector.version.ColorMapping;
import net.querz.mcaselector.version.Helper;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.Tag;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Anvil118ColorMapping implements ColorMapping {

	// value can either be an Integer (color) or a BlockStateMapping
	private final Map<String, Object> mapping = new TreeMap<>();
	private final Set<String> grass = new HashSet<>();
	private final Set<String> foliage = new HashSet<>();

	private final Map<String, Integer> biomeGrassTints = new HashMap<>();
	private final Map<String, Integer> biomeFoliageTints = new HashMap<>();
	private final Map<String, Integer> biomeWaterTints = new HashMap<>();

	private final int[] biomeGrassTintsLegacy = new int[256];
	private final int[] biomeFoliageTintsLegacy = new int[256];
	private final int[] biomeWaterTintsLegacy = new int[256];

	public Anvil118ColorMapping() {
		// note_block:pitch=1,powered=true,instrument=flute;01ab9f
		try (BufferedReader bis = new BufferedReader(
			new InputStreamReader(Objects.requireNonNull(Anvil118ColorMapping.class.getClassLoader().getResourceAsStream("mapping/118/colors.txt"))))) {
			String line;
			while ((line = bis.readLine()) != null) {
				String[] elements = line.split(";");
				if (elements.length < 2 || elements.length > 3) {
					Debug.dumpf("invalid line in color file: \"%s\"", line);
					continue;
				}
				String[] blockData = elements[0].split(":");
				if (blockData.length > 2) {
					Debug.dumpf("invalid line in color file: \"%s\"", line);
					continue;
				}
				Integer color = TextHelper.parseInt(elements[1], 16);
				if (color == null || color < 0x0 || color > 0xFFFFFF) {
					Debug.dumpf("invalid color code in color file: \"%s\"", elements[1]);
					continue;
				}

				if (blockData.length == 1) {
					// default block color, set value to Integer color
					mapping.put("minecraft:" + blockData[0], color | 0xFF000000);
				} else {
					BlockStateMapping bsm;
					if (mapping.containsKey("minecraft:" + blockData[0])) {
						bsm = (BlockStateMapping) mapping.get("minecraft:" + blockData[0]);
					} else {
						bsm = new BlockStateMapping();
						mapping.put("minecraft:" + blockData[0], bsm);
					}
					Set<String> conditions = new HashSet<>(Arrays.asList(blockData[1].split(",")));
					bsm.blockStateMapping.put(conditions, color | 0xFF000000);
				}
				if (elements.length == 3) {
					switch (elements[2]) {
						case "g" -> grass.add("minecraft:" + blockData[0]);
						case "f" -> foliage.add("minecraft:" + blockData[0]);
						default -> throw new RuntimeException("invalid grass / foliage type " + elements[2]);
					}
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException("failed to read mapping/118/colors.txt");
		}

		try (BufferedReader bis = new BufferedReader(
			new InputStreamReader(Objects.requireNonNull(ColorMapping.class.getClassLoader().getResourceAsStream("mapping/118/biome_colors.txt"))))) {

			String line;
			while ((line = bis.readLine()) != null) {
				String[] elements = line.split(";");
				if (elements.length != 4) {
					Debug.dumpf("invalid line in biome color file: \"%s\"", line);
					continue;
				}

				String biomeName = elements[0];
				int grassColor = Integer.parseInt(elements[1], 16);
				int foliageColor = Integer.parseInt(elements[2], 16);
				int waterColor = Integer.parseInt(elements[3], 16);

				biomeGrassTints.put("minecraft:" + biomeName, grassColor);
				biomeFoliageTints.put("minecraft:" + biomeName, foliageColor);
				biomeWaterTints.put("minecraft:" + biomeName, waterColor);
			}
		} catch (IOException ex) {
			throw new RuntimeException("failed to read mapping/118/biome_colors.txt");
		}

		Arrays.fill(biomeGrassTintsLegacy, DEFAULT_GRASS_TINT);
		Arrays.fill(biomeFoliageTintsLegacy, DEFAULT_FOLIAGE_TINT);
		Arrays.fill(biomeWaterTintsLegacy, DEFAULT_WATER_TINT);

		try (BufferedReader bis = new BufferedReader(
			new InputStreamReader(Objects.requireNonNull(ColorMapping.class.getClassLoader().getResourceAsStream("mapping/118/biome_colors_legacy.txt"))))) {

			String line;
			while ((line = bis.readLine()) != null) {
				String[] elements = line.split(";");
				if (elements.length != 4) {
					Debug.dumpf("invalid line in biome color file: \"%s\"", line);
					continue;
				}

				int biomeID = Integer.parseInt(elements[0]);
				int grassColor = Integer.parseInt(elements[1], 16);
				int foliageColor = Integer.parseInt(elements[2], 16);
				int waterColor = Integer.parseInt(elements[3], 16);

				biomeGrassTintsLegacy[biomeID] = grassColor;
				biomeFoliageTintsLegacy[biomeID] = foliageColor;
				biomeWaterTintsLegacy[biomeID] = waterColor;
			}
		} catch (IOException ex) {
			throw new RuntimeException("failed to read mapping/118/biome_colors_legacy.txt");
		}
	}

	@Override
	public int getRGB(Object o, int biome) {
		String name = Helper.stringFromCompound((CompoundTag) o, "Name", "");
		Object value = mapping.get(name);
		if (value instanceof Integer) {
			return applyBiomeTintLegacy(name, biome, (int) value);
		} else if (value instanceof BlockStateMapping) {
			int color = ((BlockStateMapping) value).getColor(Helper.tagFromCompound((CompoundTag) o, "Properties"));
			return applyBiomeTintLegacy(name, biome, color);
		}
		return 0xFF000000;
	}

	@Override
	public int getRGB(Object o, String biome) {
		String name = Helper.stringFromCompound((CompoundTag) o, "Name", "");
		Object value = mapping.get(name);
		if (value instanceof Integer) {
			return applyBiomeTint(name, biome, (int) value);
		} else if (value instanceof BlockStateMapping) {
			int color = ((BlockStateMapping) value).getColor(Helper.tagFromCompound((CompoundTag) o, "Properties"));
			return applyBiomeTint(name, biome, color);
		}
		return 0xFF000000;
	}

	@Override
	public boolean isFoliage(Object name) {
		if (foliage.contains((String) name)) {
			return true;
		}
		return switch ((String) name) {
			case "minecraft:birch_leaves", "minecraft:spruce_leaves", "minecraft:azalea_leaves", "minecraft:flowering_azalea_leaves" -> true;
			default -> false;
		};
	}

	private int applyBiomeTintLegacy(String name, int biome, int color) {
		if (grass.contains(name)) {
			return applyTint(color, biomeGrassTintsLegacy[biome]);
		} else if (foliage.contains(name)) {
			return applyTint(color, biomeFoliageTintsLegacy[biome]);
		} else if (name.equals("minecraft:water")) {
			return applyTint(color, biomeWaterTintsLegacy[biome]);
		}
		return color;
	}

	private int applyBiomeTint(String name, String biome, int color) {
		if (grass.contains(name)) {
			return applyTint(color, biomeGrassTints.getOrDefault(biome, DEFAULT_GRASS_TINT));
		} else if (foliage.contains(name)) {
			return applyTint(color, biomeFoliageTints.getOrDefault(biome, DEFAULT_FOLIAGE_TINT));
		} else if (name.equals("minecraft:water")) {
			return applyTint(color, biomeWaterTints.getOrDefault(biome, DEFAULT_WATER_TINT));
		}
		return color;
	}

	private static class BlockStateMapping {

		private final Map<Set<String>, Integer> blockStateMapping = new HashMap<>();

		public int getColor(CompoundTag properties) {
			if (properties != null) {
				for (Map.Entry<String, Tag> property : properties.entrySet()) {
					Map<Set<String>, Integer> clone = new HashMap<>(blockStateMapping);
					for (Map.Entry<Set<String>, Integer> blockState : blockStateMapping.entrySet()) {
						String value = property.getKey() + "=" + ((StringTag) property.getValue()).getValue();
						if (!blockState.getKey().contains(value)) {
							clone.remove(blockState.getKey());
						}
					}
					Iterator<Map.Entry<Set<String>, Integer>> it = clone.entrySet().iterator();
					if (it.hasNext()) {
						return it.next().getValue();
					}
				}
			}
			return 0x000000;
		}
	}
}
