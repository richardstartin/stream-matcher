package io.github.richardstartin.streammatcher.search.shiftand;

import io.github.richardstartin.streammatcher.Utils;
import io.github.richardstartin.streammatcher.search.Searcher;

import java.util.Arrays;

public class SparseShiftAndSearcher implements Searcher {

    private final long[] masks;
    private final byte[] positions;
    private final long success;

    public SparseShiftAndSearcher(byte[] searchString) {
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
        this.masks = new long[cardinality + 1];
        this.positions = new byte[256];
        Arrays.fill(positions, (byte) cardinality);
        int index = 0;
        for (byte key : searchString) {
            int position = Utils.rank(key, existence);
            positions[key & 0xFF] = (byte) position;
            masks[position] |= (1L << index);
            assert masks[positions[key & 0xFF] & 0xFF] == masks[position];
            ++index;
        }
        this.success = 1L << (searchString.length - 1);
    }

    public int find(byte[] data) {
        long current = 0L;
        for (int i = 0; i < data.length; ++i) {
            int value = data[i] & 0xFF;
            long mask = masks[positions[value] & 0xFF];
            current = ((current << 1) | 1) & mask;
            if ((current & success) == success) {
                return i - Long.numberOfTrailingZeros(success);
            }
        }
        return -1;
    }
}

