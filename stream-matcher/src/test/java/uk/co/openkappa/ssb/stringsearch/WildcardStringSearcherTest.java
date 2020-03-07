package uk.co.openkappa.ssb.stringsearch;

import io.github.richardstartin.streammatcher.relations.EquivalenceRelation;
import io.github.richardstartin.streammatcher.search.shiftor.ShiftOrWildCardSearcher;
import io.github.richardstartin.streammatcher.search.shiftor.SparseShiftOrWildCardSearcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static uk.co.openkappa.ssb.stringsearch.CaseInsensitiveEquivalenceRelationTest.LOWER_CASE;

@RunWith(Parameterized.class)
public class WildcardStringSearcherTest {

    private static final EquivalenceRelation CASE_INSENSITIVE = EquivalenceRelation.transformed(LOWER_CASE, CaseInsensitiveEquivalenceRelationTest::toUpper);
    private final String data;
    private final String term;
    private final int expected;
    public WildcardStringSearcherTest(String data, String term) {
        this.data = data;
        this.term = term;
        Pattern pattern = Pattern.compile(term);
        var matcher = pattern.matcher(data);
        if (matcher.find()) {
            this.expected = matcher.start();
        } else {
            this.expected = -1;
        }
    }

    @Parameterized.Parameters(name = "{0}/{1}")
    public static Object[][] params() {
        return new Object[][]{
                {"abcdefgh", "a."},
                {"abcdefgh", "b."},
                {"abcdefgh", "c."},
                {"abcdefgh", "d."},
                {"abcdefgh", "e."},
                {"abcdefgh", "fg."},
                {"abcdefgh", "ab"},
                {"abcdefgh", "bc"},
                {"abcdefgh", "cd"},
                {"abcdefgh", "de"},
                {"abcdefgh", "ef"},
                {"abcdefgh", "fgh"},
                {"abcdefgh", "ab."},
                {"abcdefgh", "bc."},
                {"abcdefgh", "cd."},
                {"abcdefgh", "de."},
                {"abcdefgh", "ef."},
                {"abcdefgh", "fgh."},
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
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"ABC1lksjwhfo[whef[o", "AB.1"},
                {"xABC1lksjwhfo[whef[o", "AB.1"},
                {"xxABC1lksjwhfo[whef[o", "AB.1"},
                {"xxxABC1lksjwhfo[whef[o", "AB.1"},
                {"xxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"xxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"xxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"xxxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"xxxxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"xxxxxxxxxABC1lksjwhfo[whef[o", "AB.1"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdABC1lksjwhfo[whef[o", "ABC2"},

                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdxxxxxxxxxABD1lksjwhfo[whef[o", "AB[^C]"},
                {"ABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xxxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xxxxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"xxxxxxxxxABC1lksjwhfo[whef[o", "AB[^C]"},
                {"asjf;whfwqppiqweyqguifhwehdpqiwevpiqwhfnqwjnqpiwehdABC1lksjwhfo[whef[o", "ABC2"},

                {"ababcdfeeeeeeeeeepopopopopopopoenoughaskljdl;aksjd", "enough"},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "enough_enough_enough"},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "1"},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "enough.enough.enough"},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "1."},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"},
                {"ababcdfeeeeeeeeeepopopopopopopo_enough_enough_enough_askljdl;aksjd", "aaaaaaaaaaaaaaa.aaaaaaaaa.aaaaaaaaaaa.aaaaaaaaaaaaaaaaaaaaaaaaaa"},
        };
    }

    private static byte[] toPattern(String term) {
        // works for "not X" expressed as [^X]
        int ignore = 0;
        for (int i = 0; i < term.length(); ++i) {
            if (term.charAt(i) == '[' || term.charAt(i) == ']') {
                ++ignore;
            }
        }
        byte[] pattern = new byte[term.length() - ignore];
        int p = 0;
        for (int i = 0; i < term.length(); ++i) {
            char c = term.charAt(i);
            if (c == '.') {
                pattern[p++] = 0x1;
            } else if (c == '^') {
                pattern[p++] = 0x2;
            } else if (c != '[' && c != ']') {
                pattern[p++] = (byte) c;
            }
        }
        return pattern;
    }

    @Test
    public void verifyShiftOrWildcardMatcher() {
        assertEquals(expected, new ShiftOrWildCardSearcher(toPattern(term)).find(data.getBytes()));
    }

    @Test
    public void verifySparseShiftOrWildcardMatcher() {
        assertEquals(expected, new SparseShiftOrWildCardSearcher(toPattern(term)).find(data.getBytes()));
    }

    @Test
    public void verifyCaseInsensitive() {
        var searcher = ShiftOrWildCardSearcher.of(CASE_INSENSITIVE, toPattern(term));
        assertEquals(expected, searcher.find(data.getBytes()));
        assertEquals(expected, searcher.find(data.toUpperCase().getBytes()));
        assertEquals(expected, searcher.find(data.toLowerCase().getBytes()));
    }
}
