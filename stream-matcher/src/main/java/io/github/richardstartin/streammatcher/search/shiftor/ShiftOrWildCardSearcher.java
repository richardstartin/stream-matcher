package io.github.richardstartin.streammatcher.search.shiftor;

import io.github.richardstartin.streammatcher.relations.EquivalenceRelation;
import io.github.richardstartin.streammatcher.search.Searcher;

import java.util.Arrays;

/**
 * @see <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.53.88&rep=rep1&type=pdf">A New Approach to String Searching</a>
 */
public class ShiftOrWildCardSearcher implements Searcher {


    private static final byte WILDCARD = 0x1;
    private static final byte NEGATE = 0x2;
    private final long[] masks;
    private final long limit;

    public ShiftOrWildCardSearcher(byte[] query) {
        this.masks = new long[256];
        long all = -1L;
        int i = 0;
        int pos = 0;
        long pattern = 1L;
        while (i < query.length) {
            if (pos > Long.SIZE) {
                throw new IllegalArgumentException("too many bytes");
            }
            if (query[i] == WILDCARD) {
                all &= ~pattern;
            } else if (query[i] == NEGATE) {
                if (++i < query.length) {
                    all &= ~pattern;
                } else {
                    throw new IllegalArgumentException("cannot end with negation");
                }
            }
            ++i;
            pattern <<= 1;
            ++pos;
        }
        Arrays.fill(masks, all);
        int j = 0;
        pattern = 1;
        while (j < query.length) {
            if (query[j] == NEGATE) {
                masks[query[++j]] |= pattern;
            } else {
                masks[query[j]] &= ~pattern;
            }
            ++j;
            pattern <<= 1;
        }
        this.limit = -(pattern >>> 1);
    }

    private ShiftOrWildCardSearcher(long limit, long[] masks) {
        this.limit = limit;
        this.masks = masks;
    }

    public static Searcher of(EquivalenceRelation relation, byte[] query) {
        long[] masks = new long[255];
        long all = -1L;
        int i = 0;
        int pos = 0;
        long pattern = 1L;
        while (i < query.length) {
            if (pos > Long.SIZE) {
                throw new IllegalArgumentException("too many bytes");
            }
            if (query[i] == WILDCARD) {
                all &= ~pattern;
            } else if (query[i] == NEGATE) {
                if (++i < query.length) {
                    all &= ~pattern;
                } else {
                    throw new IllegalArgumentException("cannot end with negation");
                }
            }
            ++i;
            pattern <<= 1;
            ++pos;
        }
        Arrays.fill(masks, all);
        int j = 0;
        pattern = 1;
        while (j < query.length) {
            if (query[j] == NEGATE) {
                long pat = pattern;
                relation.forEachEquivalentTo(query[++j], k -> masks[k] |= pat);
            } else {
                long negatedPattern = ~pattern;
                relation.forEachEquivalentTo(query[j], k -> masks[k] &= negatedPattern);
            }
            ++j;
            pattern <<= 1;
        }
        return new ShiftOrWildCardSearcher(-(pattern >>> 1), masks);
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
