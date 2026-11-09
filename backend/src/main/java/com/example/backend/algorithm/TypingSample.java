package com.example.backend.algorithm;

import java.util.List;

public record TypingSample(double[] holdTimes, double[] downDownTimes) {
    public static TypingSample from(List<KeyTiming> keys) {
        if (keys.size() < 2) {
            throw new IllegalArgumentException("A sample needs at least 2 keys.");
        }

        int length = keys.size();
        double[] holdTimes = new double[length];
        double[] downDownTimes = new double[length - 1];

        for (int i = 0; i < length; i++) {
            KeyTiming key = keys.get(i);
            holdTimes[i] = key.upMs() - key.downMs();

            if (i < length - 1) {
                KeyTiming nextKey = keys.get(i + 1);
                downDownTimes[i] = nextKey.downMs() - key.downMs();
            }
        }
        return new TypingSample(holdTimes, downDownTimes);
    }
    public int length() {
        return holdTimes.length;
    }
}