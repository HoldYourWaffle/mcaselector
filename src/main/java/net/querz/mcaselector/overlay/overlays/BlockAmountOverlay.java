package net.querz.mcaselector.overlay.overlays;

import net.querz.mcaselector.io.anvil.BlockState;
import net.querz.mcaselector.io.anvil.chunk.ChunkData;
import net.querz.mcaselector.overlay.Overlay;
import net.querz.mcaselector.overlay.OverlayType;
import net.querz.mcaselector.text.TextHelper;
import net.querz.mcaselector.version.ChunkHandler;
import net.querz.mcaselector.version.ChunkHelper;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.CompoundTag;

import java.util.HashSet;
import java.util.Set;

public class BlockAmountOverlay extends Overlay {

	private static final int MIN_VALUE = 0;
	private static final int MAX_VALUE = 98304; // 384 * 16 * 16

	public BlockAmountOverlay() {
		super(OverlayType.BLOCK_AMOUNT);
		setMultiValues(new String[0]);
	}

	@Override
	public int parseValue(ChunkData chunkData) {
		if (chunkData.region() == null || chunkData.region().getData() == null) {
			return 0;
		}

		// contains is O(1) on Set
		// OPTIMIZE can we use this 'trick' in other places?
		Set<String> countedBlocks = Set.of(multiValues());

		ChunkHandler handler = VersionController.getChunkHandler(chunkData.getDataVersion());
		int count = 0;

		// NOTE not using ChunkHelper.getBlockAt because it'd spend a lot of time resolving pointers that we know should not be counted, and comparing pointer ints is faster than block name strings
		for (CompoundTag sectionData : ChunkHelper.iterateSections(handler, chunkData.region().getData())) {
			BlockState[] palette = handler.getPaletteOfSection(sectionData);

			// all pointer values that correspond to a block that should be counted
			Set<Integer> countedPointers = new HashSet<>();
			for (int i = 0; i < palette.length; i++) {
				if (countedBlocks.contains(palette[i].name())) {
					countedPointers.add(i);
				}
			}

			for (int pointer : handler.getBlockStatePointersOfSection(sectionData)) {
				if (countedPointers.contains(pointer)) {
					count++;
				}
			}
		}

		return count;
	}

	@Override
	public String name() {
		return "Blocks";
	}

	@Override
	public boolean setMin(String raw) {
		setRawMin(raw);
		try {
			int value = Integer.parseInt(raw);
			if (value < MIN_VALUE || value > MAX_VALUE) {
				return setMin((Integer) null);
			}
			return setMin(value);
		} catch (NumberFormatException ex) {
			return setMin((Integer) null);
		}
	}

	@Override
	public boolean setMax(String raw) {
		setRawMax(raw);
		try {
			int value = Integer.parseInt(raw);
			if (value < MIN_VALUE || value > MAX_VALUE) {
				return setMax((Integer) null);
			}
			return setMax(value);
		} catch (NumberFormatException ex) {
			return setMax((Integer) null);
		}
	}

	@Override
	public boolean setMultiValues(String raw) {
		if (raw == null) {
			setMultiValues(new String[0]);
			return false;
		}
		setRawMultiValues(raw);
		String[] blocks = TextHelper.parseBlockNames(raw);
		if (blocks == null) {
			setMultiValues(new String[0]);
			return false;
		} else {
			setMultiValues(blocks);
			return true;
		}
	}
}
