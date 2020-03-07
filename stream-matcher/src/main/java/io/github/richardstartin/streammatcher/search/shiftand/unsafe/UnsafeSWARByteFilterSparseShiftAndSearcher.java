package io.github.richardstartin.streammatcher.search.shiftand.unsafe;

import io.github.richardstartin.streammatcher.Utils;
import io.github.richardstartin.streammatcher.search.Searcher;

import static io.github.richardstartin.streammatcher.Utils.compilePattern;
import static io.github.richardstartin.streammatcher.search.shiftand.unsafe.UnsafeAccess.BYTE_ARRAY_OFFSET;
import static io.github.richardstartin.streammatcher.search.shiftand.unsafe.UnsafeAccess.UNSAFE;

public class UnsafeSWARByteFilterSparseShiftAndSearcher implements Searcher, AutoCloseable {

    private final long masksOffset;
    private final long positionsOffset;
    private final long success;
    private final long first;


    public UnsafeSWARByteFilterSparseShiftAndSearcher(byte[] searchString) {
        if (searchString.length > 64) {
            throw new IllegalArgumentException("Too many bytes");
        }
        int cardinality = 0;
        long[] existence = new long[4];
        for (byte key : searchString) {
            int value = key & 0xFF;
            long word = existence[value >>> 6];
            if ((word & (1L << value)) == 0) {
                ++cardinality;
                existence[value >>> 6] |= (1L << value);
            }
        }
        int masksStorage = (cardinality + 1) * Long.BYTES;
        int positionsStorage = 256;
        this.masksOffset = UNSAFE.allocateMemory(masksStorage + positionsStorage);
        UNSAFE.setMemory(masksOffset, masksStorage, (byte) 0);
        UNSAFE.setMemory(masksOffset + masksStorage, positionsStorage, (byte) cardinality);
        this.positionsOffset = masksOffset + masksStorage;
        int index = 0;
        for (byte key : searchString) {
            int position = Utils.rank(key, existence);
            UNSAFE.putByte(positionAddress(key & 0xFF), (byte) position);
            UNSAFE.putLong(maskAddress(position),
                    UNSAFE.getLong(maskAddress(position)) | (1L << index));
            ++index;
        }
        this.success = 1L << (searchString.length - 1);
        this.first = compilePattern(searchString[0]);
    }

    public int find(byte[] data) {
        long current = 0L;
        int i = 0;
        while (i + 7 < data.length) {
            long word = first ^ UNSAFE.getLong(data, i + BYTE_ARRAY_OFFSET);
            long tmp = (word & 0x7F7F7F7F7F7F7F7FL) + 0x7F7F7F7F7F7F7F7FL;
            tmp = ~(tmp | word | 0x7F7F7F7F7F7F7F7FL);
            int j = Long.numberOfTrailingZeros(tmp) >>> 3;
            if (j != Long.BYTES) { // found the first byte
                i += j;
                for (; i < data.length; ++i) {
                    int value = data[i] & 0xFF;
                    int position = UNSAFE.getByte(positionAddress(value)) & 0xFF;
                    long mask = UNSAFE.getLong(maskAddress(position));
                    current = ((current << 1) | 1) & mask;
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
        for (; i < data.length; ++i) {
            int value = data[i] & 0xFF;
            int position = UNSAFE.getByte(positionAddress(value)) & 0xFF;
            long mask = UNSAFE.getLong(maskAddress(position));
            current = ((current << 1) | 1) & mask;
            if ((current & success) == success) {
                return i - Long.numberOfTrailingZeros(success);
            }
        }
        return -1;
    }


    @Override
    public void close() {
        UNSAFE.freeMemory(masksOffset);
    }

    private long positionAddress(int value) {
        return positionsOffset + value;
    }

    private long maskAddress(int position) {
        return masksOffset + (position * Long.BYTES);
    }
}
