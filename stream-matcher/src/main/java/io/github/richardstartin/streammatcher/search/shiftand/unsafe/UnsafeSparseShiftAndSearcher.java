package io.github.richardstartin.streammatcher.search.shiftand.unsafe;

import io.github.richardstartin.streammatcher.Utils;
import io.github.richardstartin.streammatcher.search.Searcher;

import static io.github.richardstartin.streammatcher.search.shiftand.unsafe.UnsafeAccess.UNSAFE;

public class UnsafeSparseShiftAndSearcher implements Searcher, AutoCloseable {

    private final long masksOffset;
    private final long positionsOffset;
    private final long success;

    public UnsafeSparseShiftAndSearcher(byte[] searchString) {
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
    }

    public int find(byte[] data) {
        long current = 0L;
        for (int i = 0; i < data.length; ++i) {
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
