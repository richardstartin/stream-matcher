package uk.co.openkappa.ssb.stringsearch;

import io.github.richardstartin.streammatcher.generators.DataGenerator;
import io.github.richardstartin.streammatcher.generators.DataSets;

import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class Fuzzing {

    private static final long SEED = parseLong(System.getProperty("fuzz.seed", String.valueOf(System.nanoTime())));
    private static final int ITERATIONS = parseInt(System.getProperty("fuzz.iterations", "10000"));

    static class TestCase {
        private final String dataSet;
        private final SplittableRandom random;
        private final DataGenerator generator;
        private final byte[] pattern;
        private final byte[] text;

        public TestCase(SplittableRandom random, DataGenerator generator, int maxSize, String dataSet) {
            int patternSize = random.nextInt(2, 64);
            int textSize = random.nextInt(patternSize, maxSize);
            this.random = random;
            this.generator = generator;
            this.pattern = new byte[patternSize];
            this.text = new byte[textSize];
            this.dataSet = dataSet;
        }

        public void regenenate() {
            generator.nextBytes(text);
            if (random.nextBoolean()) {
                int offset = text.length == pattern.length
                        ? 0
                        : random.nextInt(0, text.length - pattern.length);
                System.arraycopy(text, text.length - pattern.length - offset, pattern, 0, pattern.length);
            } else {
                generator.nextBytes(pattern);
            }
        }

        @Override
        public String toString() {
            return "dataset=" + dataSet + ", pattern=" + new String(pattern, UTF_8) + ", text=" + new String(text, UTF_8);
        }
    }


    public static TestCase[] generateLiteralTestCases() {
        SplittableRandom random = new SplittableRandom(SEED);
        return Arrays.stream(DataSets.values())
                .flatMap(ds -> IntStream.range(0, Runtime.getRuntime().availableProcessors())
                        .mapToObj(i -> new TestCase(random.split(), ds.create(SEED), 500, ds.name())))
                .toArray(TestCase[]::new);
    }

    public static long verify(Supplier<TestCase[]> supplier,
                              ToIntFunction<TestCase> reference,
                              Function<byte[], Searcher> searcherConstructor) {
        ToIntFunction<TestCase> sut = tc -> search(searcherConstructor, tc);
        AtomicLong casesEvaluated = new AtomicLong();
        Arrays.stream(supplier.get())
                .parallel()
                .forEach(testCase -> {
                    for (int i = 0; i < ITERATIONS; ++i) {
                        evaluate(testCase, reference, sut);
                    }
                    casesEvaluated.getAndAdd(ITERATIONS);
                });
        return casesEvaluated.get();
    }


    public static void evaluate(TestCase testCase,
                                ToIntFunction<TestCase> reference,
                                ToIntFunction<TestCase> sut) {
        testCase.regenenate();
        int x = reference.applyAsInt(testCase);
        int y = sut.applyAsInt(testCase);
        if (x != y) {
            assertEquals(testCase.toString(), x, y);
        }
    }


    public static int literalMatchReferenceImplementation(TestCase testCase) {
        byte[] text = testCase.text;
        byte[] pattern = testCase.pattern;
        for (int i = 0; i < text.length; ++i) {
            int j = 0;
            for (; j < pattern.length && i + j < text.length; ++j) {
                if (text[i + j] != pattern[j]) {
                    break;
                }
            }
            if (j == pattern.length) {
                return i;
            }
        }
        return -1;
    }

    public static int search(Function<byte[], Searcher> ctor, TestCase testCase) {
        var searcher = ctor.apply(testCase.pattern);
        int result = searcher.find(testCase.text);
        if (searcher instanceof AutoCloseable) {
            try {
                ((AutoCloseable)searcher).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

}
