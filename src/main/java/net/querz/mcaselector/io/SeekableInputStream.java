package net.querz.mcaselector.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public abstract class SeekableInputStream extends InputStream {

    public abstract void seek(long pointer) throws IOException;

    public static class RandomAccessFileAdapter extends SeekableInputStream {
        private final RandomAccessFile raf;

        public RandomAccessFileAdapter(RandomAccessFile raf) {
            this.raf = raf;
        }

        public RandomAccessFileAdapter(File file, String mode) throws FileNotFoundException {
            this(new RandomAccessFile(file, mode));
        }

        @Override
        public void seek(long pointer) throws IOException {
            raf.seek(pointer);
        }

        @Override
        public int read() throws IOException {
            return raf.read();
        }
    }

}
