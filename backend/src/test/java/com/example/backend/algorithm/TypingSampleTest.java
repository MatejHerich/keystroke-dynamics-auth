package com.example.backend.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypingSampleTest {

    private static final double DELTA = 1e-9;

    @Test
    void computesHoldTimesAndDownDownTimes() {
        List<KeyTiming> keys = List.of(
                new KeyTiming(0, 100),
                new KeyTiming(150, 230),
                new KeyTiming(400, 480));

        TypingSample sample = TypingSample.from(keys);

        assertArrayEquals(new double[]{100, 80, 80}, sample.holdTimes(), DELTA);
        assertArrayEquals(new double[]{150, 250}, sample.downDownTimes(), DELTA);
        assertEquals(3, sample.length());
    }

    @Test
    void handlesOverlappingKeys() {
        List<KeyTiming> keys = List.of(
                new KeyTiming(0, 120),
                new KeyTiming(90, 200));

        TypingSample sample = TypingSample.from(keys);

        assertArrayEquals(new double[]{120, 110}, sample.holdTimes(), DELTA);
        assertArrayEquals(new double[]{90}, sample.downDownTimes(), DELTA);
    }

    @Test
    void rejectsSampleWithOneKey() {
        List<KeyTiming> keys = List.of(new KeyTiming(0, 100));

        assertThrows(IllegalArgumentException.class, () -> TypingSample.from(keys));
    }

    @Test
    void rejectsKeyReleasedBeforePress() {
        assertThrows(IllegalArgumentException.class, () -> new KeyTiming(400, 390));
    }
}