package net.querz.mcaselector.version;

import net.querz.mcaselector.io.anvil.McaType;
import net.querz.mcaselector.version.anvil112.*;
import net.querz.mcaselector.version.anvil113.*;
import net.querz.mcaselector.version.anvil114.*;
import net.querz.mcaselector.version.anvil115.*;
import net.querz.mcaselector.version.anvil116.*;
import net.querz.mcaselector.version.anvil117.*;
import net.querz.mcaselector.version.anvil118.*;
import net.querz.mcaselector.version.anvil119.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class VersionController {

	private VersionController() {}

	public static ChunkHandler getChunkHandler(int dataVersion) {
		return Mapping.match(dataVersion).getChunkHandler();
	}

	public static ChunkMerger getChunkMerger(McaType type, int dataVersion) {
		return Mapping.match(dataVersion).getMerger(type);
	}

	public static ChunkRelocator getChunkRelocator(McaType type, int dataVersion) {
		return Mapping.match(dataVersion).getRelocator(type);
	}

	public static ChunkRenderer getChunkRenderer(int dataVersion) {
		return Mapping.match(dataVersion).getChunkRenderer();
	}

	public static ColorMapping getColorMapping(int dataVersion) {
		return Mapping.match(dataVersion).getColorMapping();
	}

	public static EntityFilter getEntityFilter(int dataVersion) {
		return Mapping.match(dataVersion).getEntityFilter();
	}

	private static final Map<Supplier<? extends ChunkHandler>, ChunkHandler> chunkHandlerInstances = new ConcurrentHashMap<>();
	private static final Map<Supplier<? extends ChunkMerger>, ChunkMerger> mergerInstances = new ConcurrentHashMap<>();
	private static final Map<Supplier<? extends ChunkRelocator>, ChunkRelocator> relocatorInstances = new ConcurrentHashMap<>();
	private static final Map<Supplier<? extends EntityFilter>, EntityFilter> entityFilterInstances = new ConcurrentHashMap<>();
	private static final Map<Supplier<? extends ChunkRenderer>, ChunkRenderer> chunkRendererInstances = new ConcurrentHashMap<>();
	private static final Map<Supplier<? extends ColorMapping>, ColorMapping> colorMappingInstances = new ConcurrentHashMap<>();

	private enum Mapping {

		ANVIL112(0,    1343, Anvil112ChunkHandler::new, Anvil112ChunkMerger::new, Anvil112PoiMerger::new, Anvil112EntityMerger::new, Anvil112ChunkRelocator::new, Anvil112PoiRelocator::new, Anvil112EntityRelocator::new, Anvil112EntityFilter::new, Anvil112ChunkRenderer::new, Anvil112ColorMapping::new),
		ANVIL113(1344, 1631, Anvil113ChunkHandler::new, Anvil113ChunkMerger::new, Anvil112PoiMerger::new, Anvil112EntityMerger::new, Anvil113ChunkRelocator::new, Anvil112PoiRelocator::new, Anvil112EntityRelocator::new, Anvil112EntityFilter::new, Anvil113ChunkRenderer::new, Anvil113ColorMapping::new),
		ANVIL114(1632, 2201, Anvil113ChunkHandler::new, Anvil114ChunkMerger::new, Anvil114PoiMerger::new, Anvil112EntityMerger::new, Anvil114ChunkRelocator::new, Anvil114PoiRelocator::new, Anvil112EntityRelocator::new, Anvil112EntityFilter::new, Anvil113ChunkRenderer::new, Anvil114ColorMapping::new),
		ANVIL115(2202, 2526, Anvil115ChunkHandler::new, Anvil115ChunkMerger::new, Anvil114PoiMerger::new, Anvil112EntityMerger::new, Anvil115ChunkRelocator::new, Anvil114PoiRelocator::new, Anvil112EntityRelocator::new, Anvil112EntityFilter::new, Anvil115ChunkRenderer::new, Anvil115ColorMapping::new),
		ANVIL116(2527, 2686, Anvil116ChunkHandler::new, Anvil115ChunkMerger::new, Anvil114PoiMerger::new, Anvil112EntityMerger::new, Anvil116ChunkRelocator::new, Anvil114PoiRelocator::new, Anvil112EntityRelocator::new, Anvil112EntityFilter::new, Anvil116ChunkRenderer::new, Anvil116ColorMapping::new),
		ANVIL117(2687, 2824, Anvil117ChunkHandler::new, Anvil117ChunkMerger::new, Anvil114PoiMerger::new, Anvil117EntityMerger::new, Anvil117ChunkRelocator::new, Anvil114PoiRelocator::new, Anvil117EntityRelocator::new, Anvil117EntityFilter::new, Anvil117ChunkRenderer::new, Anvil117ColorMapping::new),
		ANVIL118(2825, 3065, Anvil118ChunkHandler::new, Anvil118ChunkMerger::new, Anvil114PoiMerger::new, Anvil117EntityMerger::new, Anvil118ChunkRelocator::new, Anvil114PoiRelocator::new, Anvil117EntityRelocator::new, Anvil118EntityFilter::new, Anvil118ChunkRenderer::new, Anvil118ColorMapping::new),
		ANVIL119(3066, Integer.MAX_VALUE, Anvil119ChunkHandler::new, Anvil119ChunkMerger::new, Anvil114PoiMerger::new, Anvil117EntityMerger::new, Anvil119ChunkRelocator::new, Anvil114PoiRelocator::new, Anvil117EntityRelocator::new, Anvil118EntityFilter::new, Anvil119ChunkRenderer::new, Anvil119ColorMapping::new);


		private final int minVersion, maxVersion;
		private final Supplier<? extends ChunkHandler> chunkHandler;
		private final Map<McaType, Supplier<? extends ChunkMerger>> mergers;
		private final Map<McaType, Supplier<? extends ChunkRelocator>> relocators;
		private final Supplier<? extends EntityFilter> entityFilter;

		private final Supplier<? extends ChunkRenderer> chunkRenderer;
		private final Supplier<? extends ColorMapping> colorMapping;

		private static final Map<Integer, Mapping> mappingCache = new HashMap<>();

		Mapping(
				int minVersion,
				int maxVersion,
				Supplier<? extends ChunkHandler> chunkHandler,
				Supplier<? extends ChunkMerger> chunkMerger,
				Supplier<? extends ChunkMerger> poiMerger,
				Supplier<? extends ChunkMerger> entityMerger,
				Supplier<? extends ChunkRelocator> chunkRelocator,
				Supplier<? extends ChunkRelocator> poiRelocator,
				Supplier<? extends ChunkRelocator> entityRelocator,
				Supplier<? extends EntityFilter> entityFilter,
				Supplier<? extends ChunkRenderer> chunkRenderer,
				Supplier<? extends ColorMapping> colorMapping) {
			this.minVersion = minVersion;
			this.maxVersion = maxVersion;
			this.chunkHandler = chunkHandler;
			this.mergers = Map.of(
				McaType.REGION, chunkMerger,
				McaType.POI, poiMerger,
				McaType.ENTITIES, entityMerger
			);
			this.relocators = Map.of(
				McaType.REGION, chunkRelocator,
				McaType.POI, poiRelocator,
				McaType.ENTITIES, entityRelocator
			);
			this.entityFilter = entityFilter;
			this.chunkRenderer = chunkRenderer;
			this.colorMapping = colorMapping;
		}

		ChunkHandler getChunkHandler() {
			return chunkHandlerInstances.computeIfAbsent(chunkHandler, Supplier::get);
		}

		ChunkMerger getMerger(McaType type) {
			return mergerInstances.computeIfAbsent(mergers.get(type), Supplier::get);
		}

		ChunkRelocator getRelocator(McaType type) {
			return relocatorInstances.computeIfAbsent(relocators.get(type), Supplier::get);
		}

		EntityFilter getEntityFilter() {
			return entityFilterInstances.computeIfAbsent(entityFilter, Supplier::get);
		}

		ChunkRenderer getChunkRenderer() {
			return chunkRendererInstances.computeIfAbsent(chunkRenderer, Supplier::get);
		}

		ColorMapping getColorMapping() {
			return colorMappingInstances.computeIfAbsent(colorMapping, Supplier::get);
		}

		static Mapping match(int dataVersion) {
			Mapping mapping = mappingCache.get(dataVersion);
			if (mapping == null) {
				for (Mapping m : Mapping.values()) {
					if (m.minVersion <= dataVersion && m.maxVersion >= dataVersion) {
						mappingCache.put(dataVersion, m);
						return m;
					}
				}
				throw new IllegalArgumentException("invalid DataVersion: " + dataVersion);
			}
			return mapping;
		}
	}
}
