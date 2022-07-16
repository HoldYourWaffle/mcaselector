package net.querz.mcaselector.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class IoUtil {

    /** @see java.io.DataInputStream#readByte */
    public static byte readByte(InputStream in) throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    /** @see java.io.DataInputStream#readInt */
    public static int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4);
    }

}
