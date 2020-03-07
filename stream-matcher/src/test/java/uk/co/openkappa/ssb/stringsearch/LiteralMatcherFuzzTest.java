package uk.co.openkappa.ssb.stringsearch;

import io.github.richardstartin.streammatcher.search.Searcher;
import io.github.richardstartin.streammatcher.search.shiftand.BitSlicedShiftAndSearcher;
import io.github.richardstartin.streammatcher.search.shiftand.ShiftAndSearcher;
import io.github.richardstartin.streammatcher.search.shiftand.SparseShiftAndSearcher;
import io.github.richardstartin.streammatcher.search.shiftand.UnsafeBitSlicedShiftAndSearcher;
import io.github.richardstartin.streammatcher.search.shiftand.unsafe.*;
import io.github.richardstartin.streammatcher.search.shiftor.BitSlicedShiftOrSearcher;
import io.github.richardstartin.streammatcher.search.shiftor.ShiftOrSearcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;
import java.util.function.Function;

import static java.util.Map.entry;

@RunWith(Parameterized.class)
public class LiteralMatcherFuzzTest {

    private static final Map<Class<? extends Searcher>, Function<byte[], Searcher>> CONSTRUCTORS = Map.ofEntries(
            entry(ShiftAndSearcher.class, ShiftAndSearcher::new),
            entry(BitSlicedShiftAndSearcher.class, BitSlicedShiftAndSearcher::new),
            entry(BitSlicedShiftOrSearcher.class, BitSlicedShiftOrSearcher::new),
            entry(ShiftOrSearcher.class, ShiftOrSearcher::new),
            entry(SparseShiftAndSearcher.class, SparseShiftAndSearcher::new),
            entry(UnsafeShiftAndSearcher.class, UnsafeShiftAndSearcher::new),
            entry(UnsafeBitSlicedShiftAndSearcher.class, UnsafeBitSlicedShiftAndSearcher::new),
            entry(UnsafeSWARPairFilterBitSlicedShiftAndSearcher.class, UnsafeSWARPairFilterBitSlicedShiftAndSearcher::new),
            entry(UnsafeSWARByteFilterBitSlicedShiftAndSearcher.class, UnsafeSWARByteFilterBitSlicedShiftAndSearcher::new),
            entry(UnsafeSWARByteFilterSparseShiftAndSearcher.class, UnsafeSWARByteFilterSparseShiftAndSearcher::new),
            entry(UnsafeSparseShiftAndSearcher.class, UnsafeSparseShiftAndSearcher::new)

    );
    private final Class<?> type;
    private final Function<byte[], Searcher> constructor;

    public LiteralMatcherFuzzTest(Class<? extends Searcher> type) {
        this.type = type;
        this.constructor = CONSTRUCTORS.get(type);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Object[][] params() {
        return CONSTRUCTORS.keySet().stream().map(type -> new Object[]{type}).toArray(Object[][]::new);
    }

    @Test
    public void fuzz() {
        long cases =
                Fuzzing.verify(Fuzzing::generateLiteralTestCases, Fuzzing::literalMatchReferenceImplementation, constructor);
        System.out.println("Evaluated " + cases + " random literal matches for " + type);
    }
}
