package uk.co.openkappa.ssb.stringsearch.shiftand;

import sun.misc.Unsafe;
import uk.co.openkappa.ssb.stringsearch.Searcher;
import uk.co.openkappa.ssb.stringsearch.Utils;

import java.lang.reflect.Field;

public class UnsafeSWARPairFilterBitSlicedShiftAndSearcher implements Searcher, AutoCloseable {

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
    private final long success;
    private final long pattern;


    public UnsafeSWARPairFilterBitSlicedShiftAndSearcher(byte[] term) {
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
        this.pattern = Utils.compilePattern(term[0], term[Math.min(1, term.length - 1)]);
    }


    @Override
    public int find(byte[] text) {
        long current = 0;
        int i = 0;
        while (i + 8 < text.length) {
            long even = UNSAFE.getLong(text, BYTE_ARRAY_OFFSET + i) ^ pattern;
            long odd = UNSAFE.getLong(text, BYTE_ARRAY_OFFSET + i + 1) ^ pattern;
            long tmp0 = (even & 0x7FFF7FFF7FFF7FFFL) + 0x7FFF7FFF7FFF7FFFL;
            tmp0 = ~(tmp0 | even | 0x7FFF7FFF7FFF7FFFL);
            long tmp1 = (odd & 0x7FFF7FFF7FFF7FFFL) + 0x7FFF7FFF7FFF7FFFL;
            tmp1 = ~(tmp1 | odd | 0x7FFF7FFF7FFF7FFFL);
            int j = (Long.numberOfTrailingZeros(tmp0 | tmp1) >>> 3) & ~1;
            if (j != Long.BYTES) {
                i += j;
                for (; i < text.length; ++i) {
                    long highMask = UNSAFE.getLong(highAddress((text[i] >>> 4) & 0xF));
                    long lowMask = UNSAFE.getLong(lowAddress(text[i] & 0xF));
                    current = (((current << 1) | 1) & highMask & lowMask);
                    if (current == 0 && (i & 7) == 0 && i >= i + Long.BYTES) {
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
