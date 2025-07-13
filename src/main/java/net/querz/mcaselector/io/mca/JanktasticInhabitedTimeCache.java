package net.querz.mcaselector.io.mca;

import net.querz.mcaselector.filter.filters.InhabitedTimeFilter;
import net.querz.mcaselector.io.RegionDirectories;
import net.querz.mcaselector.selection.ChunkSet;
import net.querz.mcaselector.util.point.Point2i;
import net.querz.mcaselector.version.ChunkFilter;
import net.querz.mcaselector.version.VersionHandler;
import net.querz.nbt.LongTag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JanktasticInhabitedTimeCache {

    /**
     * Region => (Chunk => InhabitedTime)
     * Jobs are parallelized per-region, so nesting a non-concurrent map in a concurrent one avoids a lot of synchronization locking.
     */
    private static final ConcurrentHashMap<Point2i, Map<Point2i, Long>> cache = new ConcurrentHashMap<>();

    public static ChunkSet getFilteredChunksWithCache(InhabitedTimeFilter filterInhabitedTime, RegionDirectories rd) throws IOException {
        var regionCache = cache.get(rd.getLocation());
        if (regionCache == null) {
            regionCache = new HashMap<>();

            Region region = Region.loadRegion(rd);
            for (short i = 0; i < 1024; i++) {
                var data = region.getChunkData(i, false);
                LongTag tag = VersionHandler.getImpl(data, ChunkFilter.InhabitedTime.class).getInhabitedTime(data);
                regionCache.put(new Point2i(i), tag == null ? 0L : tag.asLong());
            }

            cache.put(rd.getLocation(), regionCache);
        }

        // Determine selection based on (fresh) cache
        var chunks = new ChunkSet();
        long filterValue = filterInhabitedTime.getFilterValue();
        for (Map.Entry<Point2i, Long> entry : regionCache.entrySet()) {
            long chunkValue = entry.getValue();

            var matches = switch (filterInhabitedTime.getComparator()) {
                case EQUAL -> chunkValue == filterValue;
                case NOT_EQUAL -> chunkValue != filterValue;
                case SMALLER -> chunkValue < filterValue;
                case LARGER -> chunkValue > filterValue;
                case LARGER_EQUAL -> chunkValue >= filterValue;
                case SMALLER_EQUAL -> chunkValue <= filterValue;
                default -> throw new UnsupportedOperationException("Unsupported comparator " + filterInhabitedTime.getComparator());
            };

            if (matches) {
                chunks.set(entry.getKey().asChunkIndex());
            }
        }

        return chunks;
    }

}
