package io.github.richardstartin.streammatcher.benchmark;

import org.openjdk.jmh.annotations.*;
import uk.co.openkappa.ssb.stringsearch.*;
import uk.co.openkappa.ssb.stringsearch.shiftand.*;

import java.util.SplittableRandom;

@State(Scope.Thread)
public class CyclingTermSearchState {

    @Param({"100", "2000", "4000"})
    int dataLength;
    @Param({"3", "19", "40", "59"})
    int termLength;
    @Param("16")
    int logVariety;
    @Param({"3", "4", "5", "6", "7", "8", "9", "10", "16"})
    int logTerms;
    @Param("90210")
    long seed;
    @Param
    SearcherType searcherType;
    Searcher[] searchers;
    int searcher;
    int step;
    private byte[][] data;
    private int instance;

    public byte[] next() {
        return data[instance++ & (data.length - 1)];
    }

    public Searcher nextSearcher() {
        return searchers[(searcher + step) & (searchers.length - 1)];
    }

    @Setup(Level.Trial)
    public void init() {
        data = new byte[1 << logVariety][dataLength];
        SplittableRandom random = new SplittableRandom(seed);
        byte[][] terms = new byte[1 << logTerms][termLength];
        searchers = new Searcher[1 << logTerms];
        for (int i = 0; i < terms.length; ++i) {
            random.nextBytes(terms[i]);
            searchers[i] = searcherType.compile(terms[i]);
        }
        for (int i = 0; i < data.length; ++i) {
            tryFill(data[i], random, terms[i & (logTerms - 1)], searchers[i & (logTerms - 1)]);
        }
        step = 607;
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        for (Searcher searcher : searchers) {
            if (searcher instanceof AutoCloseable) {
                ((AutoCloseable) searcher).close();
            }
        }
    }

    private void tryFill(byte[] data, SplittableRandom random, byte[] term, Searcher searcher) {
        random.nextBytes(data);
        int startPosition = dataLength - termLength - random.nextInt(10);
        System.arraycopy(term, 0, data, startPosition, term.length);
        int pos;
        if ((pos = searcher.find(data)) != startPosition) {
            System.out.println("Expected " + startPosition + " got " + pos);
            tryFill(data, random, term, searcher);
        }
    }


    public enum SearcherType {
        UNSAFE_SPARSE_BIT_MATRIX {
            @Override
            public Searcher compile(byte[] term) {
                return new UnsafeSparseShiftAndSearcher(term);
            }
        },
        UNSAFE_BIT_MATRIX {
            @Override
            public Searcher compile(byte[] term) {
                return new UnsafeShiftAndSearcher(term);
            }
        },
        UNSAFE_SPARSE_BIT_MATRIX_SWAR {
            @Override
            public Searcher compile(byte[] term) {
                return new UnsafeSWARByteFilterSparseShiftAndSearcher(term);
            }
        },
        UNSAFE_BIT_SLICED {
            @Override
            public Searcher compile(byte[] term) {
                return new UnsafeBitSlicedShiftAndSearcher(term);
            }
        },
        UNSAFE_BIT_SLICED_SWAR {
            @Override
            public Searcher compile(byte[] term) {
                return new UnsafeSWARByteFilterBitSlicedShiftAndSearcher(term);
            }
        };

        public abstract Searcher compile(byte[] term);
    }

}
