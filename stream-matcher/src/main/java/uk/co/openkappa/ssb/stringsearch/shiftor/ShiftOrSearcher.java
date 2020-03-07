package uk.co.openkappa.ssb.stringsearch.shiftor;

import uk.co.openkappa.ssb.stringsearch.Searcher;

import java.util.Arrays;

/**
 * @see <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.53.88&rep=rep1&type=pdf">A New Approach to String Searching</a>
 */
public class ShiftOrSearcher implements Searcher {

    private final long[] masks = new long[256];
    private final long limit;

    public ShiftOrSearcher(byte[] term) {
        if (term.length > Long.SIZE) {
            throw new IllegalArgumentException("too many bytes");
        }
        Arrays.fill(masks, -1L);
        long pattern = 1L;
        for (byte b : term) {
            masks[b & 0xFF] ^= pattern;
            pattern <<= 1;
        }
        this.limit = -(pattern >>> 1);
    }

    @Override
    public int find(byte[] text) {
        long state = -1L;
        for (int i = 0; i < text.length; ++i) {
            state = (state << 1) | masks[text[i] & 0xFF];
            if ((limit & ~state) != 0) {
                return i - Long.numberOfTrailingZeros(limit);
            }
        }
        return -1;
    }
}
