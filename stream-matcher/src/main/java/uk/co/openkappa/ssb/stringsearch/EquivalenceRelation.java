package uk.co.openkappa.ssb.stringsearch;

import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;

import static uk.co.openkappa.ssb.stringsearch.Utils.rank;

public interface EquivalenceRelation {

    static EquivalenceRelation transformed(byte[] alphabet, IntUnaryOperator op) {
        return Implementations.SparseNaiveEquivalenceRelation.of(alphabet, op);
    }

    static EquivalenceRelation of(byte[]... relations) {
        int cost = Implementations.SparseNaiveEquivalenceRelation.cost(relations);
        if (cost < 8192) {
            return Implementations.SparseNaiveEquivalenceRelation.of(relations);
        }
        return Implementations.DenseEquivalenceRelation.of(relations);
    }

    void forEachEquivalentTo(byte symbol, IntConsumer consumer);

    class Implementations {

        static class DenseEquivalenceRelation implements EquivalenceRelation {
            private final long[] equivalences = new long[1024];

            public DenseEquivalenceRelation(byte[][] relations) {
                for (int i = 0; i < 256; ++i) {
                    byte literal = (byte)i;
                    equivalences[(literal & 0xFF) * 4 + ((literal & 0xFF) >>> 6)] |= (1L << (literal & 0xFF));
                }
                for (var relation : relations) {
                    for (byte b : relation) {
                        for (byte c : relation) {
                            equivalences[(b & 0xFF) * 4 + ((c & 0xFF) >>> 6)] |= (1L << (c & 0xFF));
                        }
                    }
                }
            }

            public static EquivalenceRelation of(byte[]... relations) {
                return new DenseEquivalenceRelation(relations);
            }

            public void forEachEquivalentTo(byte symbol, IntConsumer consumer) {
                int minWord = (symbol & 0xFF) * 4;
                for (int i = 0; i < 4; ++i) {
                    long word = equivalences[minWord + i];
                    while (word != 0) {
                        int tz = Long.numberOfTrailingZeros(word);
                        word &= (word - 1);
                        consumer.accept(i * Long.SIZE + tz);
                    }
                }
            }
        }

        static class SparseNaiveEquivalenceRelation implements EquivalenceRelation {

            private final long[] index;
            private final byte[] extents;
            private final byte[] relations;

            SparseNaiveEquivalenceRelation(byte[]... relations) {
                long[] bitset = new long[4];
                int length = 0;
                for (byte[] relation : relations) {
                    for (byte value : relation) {
                        int x = value & 0xFF;
                        long word = bitset[x >>> 6];
                        bitset[x >>> 6] |= (1L << x);
                        if (Long.bitCount(word ^ bitset[x >>> 6]) == 0) {
                            throw new IllegalArgumentException("must be unique: saw " + value + " twice");
                        }
                    }
                    length += relation.length;
                }
                int cardinality = Long.bitCount(bitset[0])
                        + Long.bitCount(bitset[1])
                        + Long.bitCount(bitset[2])
                        + Long.bitCount(bitset[3]);
                this.index = bitset;
                this.extents = new byte[2 * cardinality];
                this.relations = new byte[length];
                int hwm = 0;
                for (byte[] relation : relations) {
                    System.arraycopy(relation, 0, this.relations, hwm, relation.length);
                    for (byte value : relation) {
                        int position = rank(value, bitset);
                        extents[position * 2] = (byte) hwm;
                        extents[position * 2 + 1] = (byte) relation.length;
                    }
                    hwm += relation.length;
                }
            }

            public static int cost(byte[][] relations) {
                // assumes values are distinct
                int cost = Long.SIZE * 4; // index
                for (byte[] relation : relations) {
                    cost += relation.length; // values
                    cost += 2 * relation.length; // extents
                }
                return cost;
            }

            public static int cost(byte[] values) {
                // assumes values are distinct
                return Long.SIZE * 4 // index
                        + 2 * values.length // extents
                        + 2 * values.length; // values
            }

            public static EquivalenceRelation of(byte[][] relations) {
                return new SparseNaiveEquivalenceRelation(relations);
            }

            public static EquivalenceRelation of(byte[] relations, IntUnaryOperator op) {
                byte[][] pairs = new byte[relations.length][];
                for (int i = 0; i < relations.length; ++i) {
                    pairs[i] = new byte[]{relations[i], (byte) op.applyAsInt(relations[i])};
                }
                return new SparseNaiveEquivalenceRelation(pairs);
            }

            @Override
            public void forEachEquivalentTo(byte symbol, IntConsumer consumer) {
                int s = symbol & 0xFF;
                if ((index[s >>> 6] & (1L << s)) != 0) {
                    int position = rank(s, index);
                    int start = extents[position * 2] & 0xFF;
                    int end = start + extents[position * 2 + 1] & 0xFF;
                    for (int i = start; i < end; ++i) {
                        consumer.accept(relations[i] & 0xFF);
                    }
                } else {
                    consumer.accept(s);
                }
            }
        }
    }


}
