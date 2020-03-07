package io.github.richardstartin.streammatcher.search.shiftor;

import io.github.richardstartin.streammatcher.search.Searcher;

import java.util.Arrays;

/**
 * @see <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.53.88&rep=rep1&type=pdf">A New Approach to String Searching</a>
 */
public class ShiftOrThresholdSearcher implements Searcher {

    private final long limit;
    private final long mask;
    private final long[] masks;
    private final boolean match;
    private final boolean shortCircuit;
    private final long overflowMask;
    private final int bits;
    private final int length;

    public ShiftOrThresholdSearcher(byte[] term, int threshold) {
        this.shortCircuit = term.length == 1 && threshold > 0;
        this.masks = new long[256];
        this.length = term.length;
        boolean match = false;
        if (2 * threshold > term.length) {
            match = true;
            threshold = term.length - threshold;
        }
        int bits = (int) (Math.ceil(Math.log(threshold + 1) / Math.log(2)) + 1);
        if (term.length > (float) Long.SIZE / bits) {
            throw new IllegalArgumentException("term too long, need " + bits + " bits per symbol to match");
        }
        long limit = ((long) threshold) << ((term.length - 1) * bits);
        long overflowMask = 0L;
        for (int i = 1; i <= term.length; ++i) {
            overflowMask = (overflowMask << bits) | (1 << (bits - 1));
        }
        if (!match) {
            limit += (1L << ((term.length - 1) * bits));
            Arrays.fill(masks, overflowMask >>> (bits - 1));
        }
        long bit = 1L;
        for (byte symbol : term) {
            int x = symbol & 0xFF;
            masks[x] = match ? masks[x] + bit : masks[x] & ~bit;
            bit <<= bits;
        }
        this.limit = limit;
        this.mask = term.length * bits == Long.SIZE ? -1L : bit - 1;
        this.match = match;
        this.overflowMask = overflowMask;
        this.bits = bits;
    }

    @Override
    public int find(byte[] text) {
        if (shortCircuit) { // just in case
            return 0;
        }
        return match
                ? findMatch(text)
                : findMismatch(text);
    }

    private int findMatch(byte[] text) {
        long state = 0L;
        long overflow = 0L;
        for (int i = 0; i < text.length; ++i) {
            state = ((state << bits) + masks[text[i] & 0xFF]) & mask;
            overflow = ((overflow << bits) | (state & overflowMask)) & mask;
            state &= ~overflowMask;
            if (((state | overflow) & limit) != 0) {
                return i - length + 1;
            }
        }
        return -1;
    }

    private int findMismatch(byte[] text) {
        long state = mask & ~overflowMask;
        long overflow = overflowMask;
        for (int i = 0; i < text.length; ++i) {
            state = ((state << bits) + masks[text[i] & 0xFF]) & mask;
            overflow = ((overflow << bits) | (state & overflowMask)) & mask;
            state &= ~overflowMask;
            if ((limit & ~(state | overflow)) != 0) {
                return i - length + 1;
            }
        }
        return -1;
    }
}
