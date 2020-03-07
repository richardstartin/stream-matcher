package io.github.richardstartin.streammatcher.benchmark;

import org.openjdk.jol.info.GraphLayout;
import uk.co.openkappa.ssb.stringsearch.shiftand.BitSlicedShiftAndSearcher;
import uk.co.openkappa.ssb.stringsearch.shiftand.ShiftAndSearcher;
import uk.co.openkappa.ssb.stringsearch.shiftand.SparseShiftAndSearcher;

public class Layout {

    public static void main(String... args) {
        ShiftAndSearcher shiftAndSearcher = new ShiftAndSearcher(new byte[]{1, 2, 3});
        System.out.println(GraphLayout.parseInstance(shiftAndSearcher).toPrintable());

        SparseShiftAndSearcher smallSparseShiftAndSearcher = new SparseShiftAndSearcher(new byte[]{1, 2, 3});
        System.out.println(GraphLayout.parseInstance(smallSparseShiftAndSearcher).toPrintable());

        byte[] term = new byte[64];
        for (byte i = 0; i < 64; ++i) {
            term[i] = i;
        }
        SparseShiftAndSearcher worstCaseSparseShiftAndSearcher = new SparseShiftAndSearcher(term);
        System.out.println(GraphLayout.parseInstance(worstCaseSparseShiftAndSearcher).toPrintable());

        System.out.println(GraphLayout.parseInstance(new BitSlicedShiftAndSearcher("colonoscopy".getBytes())).toPrintable());

    }
}
