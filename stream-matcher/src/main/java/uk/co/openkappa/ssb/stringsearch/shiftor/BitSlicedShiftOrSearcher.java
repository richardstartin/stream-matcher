package uk.co.openkappa.ssb.stringsearch.shiftor;

import uk.co.openkappa.ssb.stringsearch.Searcher;

import java.util.Arrays;

/**
 * @see <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.53.88&rep=rep1&type=pdf">A New Approach to String Searching</a>
 */
public class BitSlicedShiftOrSearcher implements Searcher {

    private final long[] high = new long[16];
    private final long[] low = new long[16];
    private final long limit;

    public BitSlicedShiftOrSearcher(byte[] term) {
        if (term.length > Long.SIZE) {
            throw new IllegalArgumentException("too many bytes");
        }
        Arrays.fill(low, -1L);
        Arrays.fill(high, -1L);
        long pattern = 1L;
        for (byte b : term) {
            low[b & 0xF] &= ~pattern;
            high[(b >>> 4) & 0xF] &= ~pattern;
            pattern <<= 1;
        }
        this.limit = -(pattern >>> 1);
    }

    @Override
    public int find(byte[] text) {
        long state = -1L;
        for (int i = 0; i < text.length; ++i) {
            long highMask = high[(text[i] >>> 4) & 0xF];
            long lowMask = low[text[i] & 0xF];
            state = (state << 1) | lowMask | highMask;
            if ((limit & ~state) != 0) {
                return i - Long.numberOfTrailingZeros(limit);
            }
        }
        return -1;
    }
}
