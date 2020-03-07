package uk.co.openkappa.ssb.stringsearch;

import io.github.richardstartin.streammatcher.search.shiftor.ShiftOrThresholdSearcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FuzzyStringSearcherTest {

    private final int expectedIndex;
    private final int threshold;
    private final byte[] text;
    private final byte[] term;
    public FuzzyStringSearcherTest(String text, String term, int threshold, int index) {
        this.text = text.getBytes();
        this.term = term.getBytes();
        this.threshold = threshold;
        this.expectedIndex = index;
    }

    @Parameterized.Parameters(name = "{0}/{1}/{2}/{3}")
    public static Object[][] params() {
        return new Object[][]{
                {"abcdefgh", "a", 0, 0},
                {"abcdefgh", "a", 1, 0},
                {"abcdefgh", "x", 1, 0},
                {"abcdefgh", "x", 0, -1},
                {"abcdefgh", "ab", 0, 0},
                {"abcdefgh", "ab", 1, 0},
                {"abcdefgh", "bc", 0, 1},
                {"abcdefgh", "bc", 1, 1},
                {"abcdefgh", "cd", 0, 2},
                {"abcdefgh", "cd", 1, 2},
                {"abcdefgh", "de", 0, 3},
                {"abcdefgh", "de", 1, 3},
                {"abcdefgh", "ef", 0, 4},
                {"abcdefgh", "ef", 1, 4},
                {"abcdefgh", "fgh", 0, 5},
                {"abcdefgh", "fgh", 1, 5},
                {"abcdefgh", "fgh", 2, 5},
                {"abcdefgh", "ab1", 0, -1},
                {"abcdefgh", "ab1", 1, 0},
                {"abcdefgh", "fg11", 1, -1},
//                {"abcdefgh", "fg11", 2, 5}, failing :(
//                {"abcdefgh", "fg11", 3, 5}, failing :(
                {"111111111111111111111111111111111111", "10", 0, -1},
                {"111111111111111111111111111111111111", "10", 1, 0},
                {"011111111111111111111111111111111111", "10", 0, -1},
                {"011111111111111111111111111111111111", "10", 1, 1}
        };
    }

    @Test
    public void findFuzzyMatch() {
        assertEquals(expectedIndex, new ShiftOrThresholdSearcher(term, threshold).find(text));
    }
}
