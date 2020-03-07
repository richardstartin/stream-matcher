package io.github.richardstartin.streammatcher.benchmark;

import io.github.richardstartin.streammatcher.generators.DataGenerator;
import io.github.richardstartin.streammatcher.generators.DataSets;
import org.openjdk.jmh.annotations.*;
import uk.co.openkappa.ssb.stringsearch.Searcher;
import uk.co.openkappa.ssb.stringsearch.shiftand.*;
import uk.co.openkappa.ssb.stringsearch.shiftor.ShiftOrSearcher;

import java.io.IOException;
import java.util.SplittableRandom;

@State(Scope.Thread)
public class SearchState {

    @Param({"100", "1000", "2000"})
    int dataLength;
    @Param({"3", "19", "40", "59"})
    int termLength;
    @Param({"7", "12"})
    int logVariety;
    @Param("90210")
    long seed;
    @Param
    SearcherType searcherType;
    @Param
    DataSets dataSet;
    Searcher searcher;
    byte[] term;
    private byte[][] data;
    private int instance;

    public static void main(String... args) throws IOException {
        for (SearcherType type : SearcherType.values()) {
            SearchState searchState = new SearchState();
            searchState.logVariety = 10;
            searchState.searcherType = type;
            searchState.termLength = 8;
            searchState.dataLength = 1000;
            searchState.dataSet = DataSets.KING_JAMES_BIBLE;
            searchState.init();
            System.out.println(new String(searchState.term));
            for (byte[] datum : searchState.data) {
                System.out.println(searchState.searcher.find(datum));
            }
            if (searchState.searcher instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) searchState.searcher).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] next() {
        return data[instance++ & (data.length - 1)];
    }

    @Setup(Level.Trial)
    public void init() {
        data = new byte[1 << logVariety][dataLength];
        DataGenerator generator = dataSet.create(seed);
        SplittableRandom random = new SplittableRandom(seed);
        term = new byte[termLength];
        generator.nextBytes(term);
        searcher = searcherType.compile(term);
        for (byte[] datum : data) {
            tryFill(datum, generator, random, term);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        if (searcher instanceof AutoCloseable) {
            ((AutoCloseable) searcher).close();
        }
    }

    private void tryFill(byte[] data, DataGenerator generator, SplittableRandom random, byte[] term) {
        generator.nextBytes(data);
        int startPosition = dataLength - termLength - random.nextInt(10);
        System.arraycopy(term, 0, data, startPosition, term.length);
        int pos;
        if ((pos = searcher.find(data)) != startPosition) {
            System.out.println("Expected " + startPosition + " got " + pos);
            tryFill(data, generator, random, term);
        }
    }


    public enum SearcherType {
        SHIFT_OR {
            @Override
            public Searcher compile(byte[] term) {
                return new ShiftOrSearcher(term);
            }
        },
        BIT_MATRIX {
            @Override
            public Searcher compile(byte[] term) {
                return new ShiftAndSearcher(term);
            }
        },
        SPARSE_BIT_MATRIX {
            @Override
            public Searcher compile(byte[] term) {
                return new SparseShiftAndSearcher(term);
            }
        },
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
        BIT_SLICED {
            @Override
            public Searcher compile(byte[] term) {
                return new BitSlicedShiftAndSearcher(term);
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
        },
        UNSAFE_BIT_SLICED_SWAR_PAIR {
            @Override
            public Searcher compile(byte[] term) {
                return new UnsafeSWARPairFilterBitSlicedShiftAndSearcher(term);
            }
        };

        public abstract Searcher compile(byte[] term);
    }

}
