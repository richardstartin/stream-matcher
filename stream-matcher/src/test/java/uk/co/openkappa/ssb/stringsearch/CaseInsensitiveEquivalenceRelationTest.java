package uk.co.openkappa.ssb.stringsearch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class CaseInsensitiveEquivalenceRelationTest {


    static final byte[] LOWER_CASE = new byte[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z'
    };

    static int toUpper(int lower) {
        return lower - (int) 'a' + (int) 'A';
    }

    public CaseInsensitiveEquivalenceRelationTest(char value, Set<Character> relation) {
        this.value = value;
        this.relation = relation;
    }

    @Parameterized.Parameters
    public static Object[][] params() {
        return new Object[][] {
                {'a', Set.of('a', 'A')},
                {'p', Set.of('p', 'P')},
                {'z', Set.of('z', 'Z')},
                {'.', Set.of('.')},
                {'~', Set.of('~')}
        };
    }

    private final char value;
    private final Set<Character> relation;
    private final EquivalenceRelation sut = EquivalenceRelation.transformed(LOWER_CASE, CaseInsensitiveEquivalenceRelationTest::toUpper);


    @Test
    public void testCaseInsensitiveLatin1LowerCase() {
        var i = new AtomicInteger(0);
        sut.forEachEquivalentTo((byte)value, v -> {
            assertTrue(relation.contains((char)v));
            i.incrementAndGet();
        });
        assertEquals(relation.size(), i.get());
    }

    @Test
    public void testCaseInsensitiveLatin1UpperCase() {
        var i = new AtomicInteger(0);
        sut.forEachEquivalentTo((byte)Character.toUpperCase(value), v -> {
            assertTrue(relation.contains((char)v));
            i.incrementAndGet();
        });
        assertEquals(relation.size(), i.get());
    }

}