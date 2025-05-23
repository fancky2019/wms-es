package gs.com.gses.utility;

import brave.internal.Platform;
import brave.internal.RecyclableBuffers;

/**
 * 用到了io.zipkin.brave包下的两个类
 *
 * 可以引入sleuth 依赖
 */
public class TraceIdCreater {

    static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    public static String getTraceId() {
        return toLowerHex(nextId());
    }

    public static long nextId() {
        long nextId;
        for(nextId = Platform.get().randomLong(); nextId == 0L; nextId = Platform.get().randomLong()) {
        }
        return nextId;
    }

    public static String toLowerHex(long v) {
        char[] data = RecyclableBuffers.parseBuffer();
        writeHexLong(data, 0, v);
        return new String(data, 0, 16);
    }

    private static void writeHexLong(char[] data, int pos, long v) {
        writeHexByte(data, pos, (byte) ((v >>> 56L) & 0xff));
        writeHexByte(data, pos + 2, (byte) ((v >>> 48L) & 0xff));
        writeHexByte(data, pos + 4, (byte) ((v >>> 40L) & 0xff));
        writeHexByte(data, pos + 6, (byte) ((v >>> 32L) & 0xff));
        writeHexByte(data, pos + 8, (byte) ((v >>> 24L) & 0xff));
        writeHexByte(data, pos + 10, (byte) ((v >>> 16L) & 0xff));
        writeHexByte(data, pos + 12, (byte) ((v >>> 8L) & 0xff));
        writeHexByte(data, pos + 14, (byte) (v & 0xff));
    }

    private static void writeHexByte(char[] data, int pos, byte b) {
        data[pos] = HEX_DIGITS[(b >> 4) & 0xf];
        data[pos + 1] = HEX_DIGITS[b & 0xf];
    }
}
