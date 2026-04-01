package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeySequenceEvaluator implements BehaviorEvaluator {

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        int comparableLength = Math.min(referenceSamples.size(), candidateSamples.size());
        if (comparableLength == 0) {
            return new EvaluatorResult("KeySequence", 0.0, "Nebolo možné porovnať sekvenciu kláves.");
        }

        int matches = 0;
        for (int index = 0; index < comparableLength; index++) {
            String expectedKey = normalizeKey(referenceSamples.get(index).key());
            String actualKey = normalizeKey(candidateSamples.get(index).key());
            if (expectedKey.equals(actualKey)) {
                matches++;
            }
        }

        double score = (double) matches / comparableLength;
        return new EvaluatorResult(
                "KeySequence",
                score,
                "Zhoda sekvencie kláves: " + matches + "/" + comparableLength
        );
    }

    private String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase();
    }
}
