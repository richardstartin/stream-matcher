package io.github.richardstartin.streammatcher;

public class Utils {

    public static int rank(byte key, long[] bitmap) {
        return rank(key & 0xFF, bitmap);
    }

    public static int rank(int value, long[] bitmap) {
        int wi = value >>> 6;
        int i = 0;
        int rank = 0;
        while (i < wi) {
            rank += Long.bitCount(bitmap[i]);
            ++i;
        }
        return rank + Long.bitCount(bitmap[wi] & ((1L << value) - 1));
    }

    public static long compilePattern(byte value) {
        return (value & 0xFFL) * 0x101010101010101L;
    }

    public static long compilePattern(byte first, byte second) {
        return ((first & 0xFFL) | ((second & 0xFFL) << 8)) * 0x0001000100010001L;
    }
}
