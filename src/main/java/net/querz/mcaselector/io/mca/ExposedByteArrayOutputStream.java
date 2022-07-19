package net.querz.mcaselector.io.mca;

import java.io.ByteArrayOutputStream;

/**
 * ExposedByteArrayOutputStream exposes the buffer array in #getBuffer()
 * to avoid calling Arrays.copyOf() when using ByteArrayOutputStream#toByteArray()
 *
 * TODO duplicated by net.querz.mca.ExposedByteArrayOutputStream (Querz/NBT#75)
 */
class ExposedByteArrayOutputStream extends ByteArrayOutputStream {

	public ExposedByteArrayOutputStream() {
		super();
	}

	public ExposedByteArrayOutputStream(int size) {
		super(size);
	}

	public byte[] getBuffer() {
		return buf;
	}
}
