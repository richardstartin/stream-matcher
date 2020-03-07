package uk.co.openkappa.ssb.stringsearch.shiftand;

import sun.misc.Unsafe;
import uk.co.openkappa.ssb.stringsearch.Searcher;

import java.lang.reflect.Field;

import static uk.co.openkappa.ssb.stringsearch.Utils.compilePattern;

public class UnsafeSWARByteFilterBitSlicedShiftAndSearcher implements Searcher, AutoCloseable {

    private static final Unsafe UNSAFE;
    private static final int BYTE_ARRAY_OFFSET;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private final long address;
    private final long first;
    private final long success;

    public UnsafeSWARByteFilterBitSlicedShiftAndSearcher(byte[] term) {
        if (term.length > 64) {
            throw new IllegalArgumentException("Too many bytes");
        }
        this.address = UNSAFE.allocateMemory(16 * Long.BYTES * 2);
        UNSAFE.setMemory(address, 16 * Long.BYTES * 2, (byte) 0);
        long word = 1L;
        for (byte b : term) {
            UNSAFE.putLong(lowAddress(b & 0xF), word | UNSAFE.getLong(lowAddress(b & 0xF)));
            UNSAFE.putLong(highAddress((b >>> 4) & 0xF), word | UNSAFE.getLong(highAddress((b >>> 4) & 0xF)));
            word <<= 1;
        }
        this.success = 1L << (term.length - 1);
        this.first = compilePattern(term[0]);
    }

    @Override
    public int find(byte[] text) {
        long current = 0L;
        int i = 0;
        while (i + 7 < text.length) {
            long word = first ^ UNSAFE.getLong(text, i + BYTE_ARRAY_OFFSET);
            long tmp = (word & 0x7F7F7F7F7F7F7F7FL) + 0x7F7F7F7F7F7F7F7FL;
            tmp = ~(tmp | word | 0x7F7F7F7F7F7F7F7FL);
            int j = Long.numberOfTrailingZeros(tmp) >>> 3;
            if (j != Long.BYTES) { // found the first byte
                i += j;
                for (; i < text.length; ++i) {
                    long highMask = UNSAFE.getLong(highAddress((text[i] >>> 4) & 0xF));
                    long lowMask = UNSAFE.getLong(lowAddress(text[i] & 0xF));
                    current = (((current << 1) | 1) & highMask & lowMask);
                    if (current == 0 && (i & (Long.BYTES - 1)) == 0) {
                        break;
                    }
                    if ((current & success) == success) {
                        return i - Long.numberOfTrailingZeros(success);
                    }
                }
            } else {
                i += Long.BYTES;
            }
        }
        for (; i < text.length; ++i) {
            long highMask = UNSAFE.getLong(highAddress((text[i] >>> 4) & 0xF));
            long lowMask = UNSAFE.getLong(lowAddress(text[i] & 0xF));
            current = ((current << 1) | 1) & highMask & lowMask;
            if ((current & success) == success) {
                return i - Long.numberOfTrailingZeros(success);
            }
        }
        return -1;
    }

    private long lowAddress(int position) {
        return address + Long.BYTES * position;
    }

    private long highAddress(int position) {
        return address + Long.BYTES * (position + 16);
    }

    @Override
    public void close() {
        UNSAFE.freeMemory(address);
    }
}
