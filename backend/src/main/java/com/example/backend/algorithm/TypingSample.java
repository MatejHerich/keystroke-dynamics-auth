package com.example.backend.algorithm;

import java.util.List;

public record TypingSample(double[] holdTimes, double[] downDownTimes) {

    public static TypingSample from(List<KeyTiming> keys, int expectedLength) {
        if (keys == null || keys.size() != expectedLength) {
            throw new IllegalArgumentException("Počet záznamov klávesov sa nezhoduje s dĺžkou hesla.");
        }
        return from(keys);
    }

    public static TypingSample from(List<KeyTiming> keys) {
        if (keys == null || keys.size() < 2) {
            throw new IllegalArgumentException("Vzorka potrebuje aspoň 2 stlačenia kláves.");
        }

        int length = keys.size();
        double[] holdTimes = new double[length];
        double[] downDownTimes = new double[length - 1];

        for (int i = 0; i < length; i++) {
            KeyTiming key = keys.get(i);
            if (key == null) {
                throw new IllegalArgumentException("Záznam času klávesu nesmie chýbať.");
            }
            holdTimes[i] = key.upMs() - key.downMs();

            if (i < length - 1) {
                KeyTiming nextKey = keys.get(i + 1);
                if (nextKey == null) {
                    throw new IllegalArgumentException("Záznam času klávesu nesmie chýbať.");
                }
                if (nextKey.downMs() < key.downMs()) {
                    throw new IllegalArgumentException("Časy stlačenia klávesov nesmú klesať.");
                }
                downDownTimes[i] = nextKey.downMs() - key.downMs();
            }
        }
        return new TypingSample(holdTimes, downDownTimes);
    }

    public int length() {
        return holdTimes.length;
    }
}