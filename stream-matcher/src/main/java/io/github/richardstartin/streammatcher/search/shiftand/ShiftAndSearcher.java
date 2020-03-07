package io.github.richardstartin.streammatcher.search.shiftand;

import io.github.richardstartin.streammatcher.search.Searcher;

/**
 * Adapted from https://en.wikipedia.org/wiki/Bitap_algorithm
 */
public class ShiftAndSearcher implements Searcher {

    private final long[] masks = new long[256];
    private final long success;

    public ShiftAndSearcher(byte[] searchString) {
        if (searchString.length > 64) {
            throw new IllegalArgumentException("Too many bytes");
        }
        long word = 1L;
        for (byte key : searchString) {
            masks[key & 0xFF] |= word;
            word <<= 1;
        }
        this.success = 1L << (searchString.length - 1);
    }

    public int find(byte[] data) {
        long current = 0L;
        for (int i = 0; i < data.length; ++i) {
            current = ((current << 1) | 1) & masks[data[i] & 0xFF];
            if ((current & success) == success) {
                return i - Long.numberOfTrailingZeros(success);
            }
        }
        return -1;
    }
}
