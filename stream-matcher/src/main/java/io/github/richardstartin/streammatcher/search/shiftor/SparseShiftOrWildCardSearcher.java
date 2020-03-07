package io.github.richardstartin.streammatcher.search.shiftor;

import io.github.richardstartin.streammatcher.Utils;
import io.github.richardstartin.streammatcher.search.Searcher;

import java.util.Arrays;

/**
 * @see <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.53.88&rep=rep1&type=pdf">A New Approach to String Searching</a>
 */
public class SparseShiftOrWildCardSearcher implements Searcher {

    private static final byte WILDCARD = 0x1;
    private static final byte NEGATE = 0x2;

    private final long[] masks;
    private final byte[] positions = new byte[256];
    private final long limit;

    public SparseShiftOrWildCardSearcher(byte[] query) {
        long all = -1L;
        int i = 0;
        int pos = 0;
        long pattern = 1L;
        int cardinality = 0;
        long[] existence = new long[4];
        while (i < query.length) {
            if (pos > Long.SIZE) {
                throw new IllegalArgumentException("too many bytes");
            }
            if (query[i] == WILDCARD) {
                all &= ~pattern;
            } else {
                if (query[i] == NEGATE) {
                    ++i;
                    if (i < query.length) {
                        all &= ~pattern;
                    } else {
                        throw new IllegalArgumentException("cannot end with negation");
                    }
                }
                int value = query[i] & 0xFF;
                long word = existence[query[i] >>> 6];
                if ((word & (1L << value)) == 0) {
                    ++cardinality;
                    existence[value >>> 6] |= (1L << value);
                }
            }
            ++i;
            pattern <<= 1;
            ++pos;
        }
        this.masks = new long[cardinality + 1];
        Arrays.fill(masks, all);
        Arrays.fill(positions, (byte) cardinality);
        int j = 0;
        pattern = 1;
        while (j < query.length) {
            if (query[j] == NEGATE) {
                byte symbol = query[j + 1];
                int position = Utils.rank(symbol, existence);
                positions[symbol & 0xFF] = (byte) position;
                masks[position] |= pattern;
                ++j;
            } else {
                byte symbol = query[j];
                int position = Utils.rank(symbol, existence);
                positions[query[j] & 0xFF] = (byte) position;
                masks[position] &= ~pattern;
            }
            ++j;
            pattern <<= 1;
        }
        this.limit = -(pattern >>> 1);
    }

    @Override
    public int find(byte[] text) {
        long state = -1L;
        for (int i = 0; i < text.length; ++i) {
            state = (state << 1) | masks[positions[text[i] & 0xFF] & 0xFF];
            if ((limit & ~state) != 0) {
                return i - Long.numberOfTrailingZeros(limit);
            }
        }
        return -1;
    }
}
