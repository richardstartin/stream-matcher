package uk.co.openkappa.ssb.stringsearch;

import io.github.richardstartin.streammatcher.relations.EquivalenceRelation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ArbitraryEquivalenceRelationsTest {

    private final char value;
    private final Set<Character> relation;
    private final EquivalenceRelation sut = EquivalenceRelation.of(
            new byte[]{'x', 'y', 'z'},
            new byte[]{'a', 'b', 'c'},
            new byte[]{'d', 'e', 'f'}
    );

    public ArbitraryEquivalenceRelationsTest(char value, Set<Character> relation) {
        this.value = value;
        this.relation = relation;
    }

    @Parameterized.Parameters
    public static Object[][] params() {
        return new Object[][]{
                {'a', Set.of('a', 'b', 'c')},
                {'b', Set.of('a', 'b', 'c')},
                {'c', Set.of('a', 'b', 'c')},
                {'d', Set.of('d', 'e', 'f')},
                {'e', Set.of('d', 'e', 'f')},
                {'f', Set.of('d', 'e', 'f')},
                {'g', Set.of('g')},
        };
    }

    @Test
    public void verify() {
        var i = new AtomicInteger(0);
        sut.forEachEquivalentTo((byte) value, v -> {
            assertTrue(relation.contains((char) v));
            i.incrementAndGet();
        });
        assertEquals(relation.size(), i.get());
    }
}
