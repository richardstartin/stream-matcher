package io.github.richardstartin.streammatcher.generators;

import java.io.IOException;
import java.io.UncheckedIOException;

public enum DataSets {

    KING_JAMES_BIBLE("histograms/en/bible/pairs.csv"),
    LUTHER_1912_BIBLE("histograms/de/bible/pairs.csv"),
    SERBIAN_LATIN_SCRIPT("histograms/sh/bible/pairs.csv"),
    RUSSIAN("histograms/ru/bible/pairs.csv"),
    CHINESE_TRADITIONAL("histograms/zh/bible/pairs.csv"),
    RANDOM {
        @Override
        public DataGenerator create(long seed) {
            return new RandomDataGenerator(seed);
        }
    };

    private final String resource;

    DataSets() {
        this("");
    }

    DataSets(String resource) {
        this.resource = resource;
    }

    public DataGenerator create(long seed) {
        try (var in = ClassLoader.getSystemResourceAsStream(resource)) {
            return MarkovChainDataGenerator.from(in, seed);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
