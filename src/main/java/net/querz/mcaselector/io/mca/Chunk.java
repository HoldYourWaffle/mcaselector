package net.querz.mcaselector.io.mca;

import net.querz.mcaselector.io.ByteArrayPointer;
import net.querz.mcaselector.io.FileHelper;
import net.querz.mcaselector.point.Point2i;
import net.querz.mcaselector.point.Point3i;
import net.querz.mcaselector.range.Range;
import net.querz.mcaselector.validation.ValidationHelper;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.NBTUtil;
import net.querz.nbt.Tag;
import net.querz.nbt.io.NBTReader;
import net.querz.nbt.io.NBTWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.function.Function;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public abstract sealed class Chunk implements Cloneable permits RegionChunk, PoiChunk, EntitiesChunk {

	protected int timestamp;
	protected CompoundTag data;
	protected CompressionType compressionType;
	protected final Point2i absoluteLocation;

	public Chunk(Point2i absoluteLocation) {
		this.absoluteLocation = absoluteLocation;
	}

	public void load(ByteArrayPointer ptr) throws IOException {
		int length = ptr.readInt();
		compressionType = CompressionType.fromByte(ptr.readByte());

		DataInputStream nbtIn = switch (compressionType) {
			case GZIP -> new DataInputStream(new BufferedInputStream(new GZIPInputStream(ptr, length)));
			case ZLIB -> new DataInputStream(new BufferedInputStream(new InflaterInputStream(ptr, new Inflater(), length)));
			case NONE -> new DataInputStream(ptr);
			case GZIP_EXT -> new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(FileHelper.createMCCFilePath(getType(), absoluteLocation)))));
			case ZLIB_EXT -> new DataInputStream(new BufferedInputStream(new InflaterInputStream(new FileInputStream(FileHelper.createMCCFilePath(getType(), absoluteLocation)))));
			case NONE_EXT -> new DataInputStream(new BufferedInputStream(new FileInputStream(FileHelper.createMCCFilePath(getType(), absoluteLocation))));
		};

		Tag tag = new NBTReader().read(nbtIn);

		if (tag instanceof CompoundTag) {
			data = (CompoundTag) tag;
		} else {
			throw new IOException("unexpected chunk data tag type " + tag.getID() + ", expected " + Tag.COMPOUND);
		}
	}

	public void load(RandomAccessFile raf) throws IOException {
		int length = raf.readInt();
		compressionType = CompressionType.fromByte(raf.readByte());

		DataInputStream nbtIn = switch (compressionType) {
			case GZIP -> new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(raf.getFD()))));
			case ZLIB -> new DataInputStream(new BufferedInputStream(new InflaterInputStream(new FileInputStream(raf.getFD()))));
			case NONE -> new DataInputStream(new BufferedInputStream(new FileInputStream(raf.getFD()), length - 1));
			case GZIP_EXT -> new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(FileHelper.createMCCFilePath(getType(), absoluteLocation)))));
			case ZLIB_EXT -> new DataInputStream(new BufferedInputStream(new InflaterInputStream(new FileInputStream(FileHelper.createMCCFilePath(getType(), absoluteLocation)))));
			case NONE_EXT -> new DataInputStream(new BufferedInputStream(new FileInputStream(FileHelper.createMCCFilePath(getType(), absoluteLocation))));
		};

		Tag tag = new NBTReader().read(nbtIn);

		if (tag instanceof CompoundTag) {
			data = (CompoundTag) tag;
		} else {
			throw new IOException("unexpected chunk data tag type " + tag.getID() + ", expected " + Tag.COMPOUND);
		}
	}

	public int save(DataOutput out) throws IOException {
		ExposedByteArrayOutputStream baos = new ExposedByteArrayOutputStream();

		// CHECK DataOutputStream wrapper unnecessary?
		DataOutputStream nbtOut = new DataOutputStream(new BufferedOutputStream(switch (compressionType) {
			case GZIP, GZIP_EXT -> new GZIPOutputStream(baos);
			case ZLIB, ZLIB_EXT -> new DeflaterOutputStream(baos);
			case NONE, NONE_EXT -> baos;
		}));

		new NBTWriter().write(nbtOut, data);
		nbtOut.close();

		// save mcc file if chunk doesn't fit in mca file
		if (baos.size() > 1048576) { // XXX magic number
			// if the chunk's version is below 2203, we throw an exception instead
			Integer dataVersion = ValidationHelper.withDefault(this::getDataVersion, null);
			if (dataVersion == null) {
				throw new RuntimeException("no DataVersion for oversized chunk");
			} else if (dataVersion < 2203) {
				throw new RuntimeException("chunk at " + absoluteLocation + " is oversized and can't be saved when DataVersion is below 2203");
			}

			out.writeInt(1);
			out.writeByte(compressionType.getExternal().getByte());
			try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FileHelper.createMCCFilePath(getType(), absoluteLocation)), baos.size())) {
				bos.write(baos.getBuffer(), 0, baos.size());
			}
			return 5; // XXX magic number
		} else {
			out.writeInt(baos.size() + 1); // length includes the compression type byte
			out.writeByte(compressionType.getByte());
			out.write(baos.getBuffer(), 0, baos.size());
			return baos.size() + 5; // data length + 1 compression type byte + 4 length bytes
		}
	}

	public boolean relocate(Point3i offset) {
		return VersionController.getChunkRelocator(getType(), getDataVersion()).relocate(data, offset);
	}

	public void merge(CompoundTag destination, List<Range> ranges, int yOffset) {
		VersionController.getChunkMerger(getType(), getDataVersion()).mergeChunks(data, destination, ranges, yOffset);
	}

	public boolean isEmpty() {
		return data == null;
	}

	public CompoundTag getData() {
		return data;
	}

	public void setData(CompoundTag data) {
		this.data = data;
	}

	public int getDataVersion() {
		return data.getInt("DataVersion");
	}

	public CompressionType getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(CompressionType compressionType) {
		this.compressionType = compressionType;
	}

	public Point2i getAbsoluteLocation() {
		return absoluteLocation;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		String s = NBTUtil.toSNBT(data);
		return "<absoluteLocation=" + absoluteLocation + ", compressionType=" + compressionType + ", data=" + s + ">";
	}

	protected <T extends Chunk> T clone(Function<Point2i, T> chunkConstructor) {
		T clone = chunkConstructor.apply(absoluteLocation);
		clone.compressionType = compressionType;
		clone.timestamp = timestamp;
		if (data != null) {
			clone.data = data.copy();
		}
		return clone;
	}

	public abstract Chunk clone();

	public abstract McaType getType();

}
