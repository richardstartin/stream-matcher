package uk.co.openkappa.ssb.stringsearch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.co.openkappa.ssb.stringsearch.shiftand.*;
import uk.co.openkappa.ssb.stringsearch.shiftor.BitSlicedShiftOrSearcher;
import uk.co.openkappa.ssb.stringsearch.shiftor.ShiftOrSearcher;
import uk.co.openkappa.ssb.stringsearch.shiftor.ShiftOrWildCardSearcher;
import uk.co.openkappa.ssb.stringsearch.shiftor.SparseShiftOrWildCardSearcher;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class StringSearcherTest {

    @Parameterized.Parameters(name = "{0}/{1}")
    public static Object[][] params() {
        return new Object[][] {
                {"abcdefgh", "ab"},
                {"abcdefgh", "bc"},
                {"abcdefgh", "cd"},
                {"abcdefgh", "de"},
                {"abcdefgh", "ef"},
                {"abcdefgh", "fgh"},
                {"abcdefgh", "ab1"},
                {"abcdefgh", "bc1"},
                {"abcdefgh", "cd1"},
                {"abcdefgh", "de1"},
                {"abcdefgh", "ef1"},
                {"abcdefgh", "fgh1"},
                {"1", "0"},
                {"1", "1"},
                {"011111110", "111111110"},
                {"111111111111111111111111111111111111", "1"},
                {"011111111111111111111111111111111111", "1"},
                {"011111111111111111111111111111111111", "011111111111111111111111"},
                {"101011010100100001", "0010"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"ABC1lksjwhfo[whef[o", "ABC1"},
                {"xABC1lksjwhfo[whef[o", "ABC1"},
                {"xxABC1lksjwhfo[whef[o", "ABC1"},
                {"xxxABC1lksjwhfo[whef[o", "ABC1"},
                {"xxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"xxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"xxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"xxxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"xxxxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"xxxxxxxxxABC1lksjwhfo[whef[o", "ABC1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdABC1lksjwhfo[whef[o", "ABC2"},

                {"ababcdfeeeeeeeeeepopopopopopopoenoughaskljdl;aksjd", "enough"},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "enough_enough_enough"},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "1"},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"},
        };
    }

    private final String data;
    private final String term;
    private final int expected;

    public StringSearcherTest(String data, String term) {
        this.data = data;
        this.term = term;
        this.expected = data.indexOf(term);
    }

    @Test
    public void verifyBitMatrix() {
        assertEquals(expected, new ShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifySparseBitMatrix() {
        assertEquals(expected, new SparseShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyUnsafeSparseBitMatrix() {
        assertEquals(expected, new UnsafeSparseShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyUnsafeBitMatrix() {
        assertEquals(expected, new UnsafeShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyUnsafeSWARSparseBitMatrix() {
        assertEquals(expected, new UnsafeSWARByteFilterSparseShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyBitSliced() {
        assertEquals(expected, new BitSlicedShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyUnsafeBitSliced() {
        assertEquals(expected, new UnsafeBitSlicedShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyUnsafeBitSlicedSWAR() {
        assertEquals(expected, new UnsafeSWARByteFilterBitSlicedShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyUnsafeBitSlicedSWARPair() {
        assertEquals(expected, new UnsafeSWARPairFilterBitSlicedShiftAndSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyShiftOr() {
        assertEquals(expected, new ShiftOrSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyBitSlicedShiftOr() {
        assertEquals(expected, new BitSlicedShiftOrSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyShiftOrWildcard() {
        assertEquals(expected, new ShiftOrWildCardSearcher(term.getBytes()).find(data.getBytes()));
    }

    @Test
    public void verifyShiftOrBitSlicedWildcard() {
        assertEquals(expected, new SparseShiftOrWildCardSearcher(term.getBytes()).find(data.getBytes()));
    }
}
