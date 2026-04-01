package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BoundaryStabilityEvaluator implements BehaviorEvaluator {

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        if (referenceSamples.isEmpty() || candidateSamples.isEmpty()) {
            return new EvaluatorResult("Boundary", 0.0, "Nie sú dostupné dáta pre začiatok a koniec frázy.");
        }

        double startScore = compareWindow(referenceSamples, candidateSamples, true);
        double endScore = compareWindow(referenceSamples, candidateSamples, false);
        double score = (startScore + endScore) / 2.0;

        return new EvaluatorResult(
                "Boundary",
                score,
                "Stabilita začiatku a konca frázy: " + String.format("%.2f", score)
        );
    }

    private double compareWindow(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples, boolean fromStart) {
        int window = Math.min(3, Math.min(referenceSamples.size(), candidateSamples.size()));
        if (window == 0) {
            return 0.0;
        }

        int matches = 0;
        for (int index = 0; index < window; index++) {
            int referenceIndex = fromStart ? index : referenceSamples.size() - window + index;
            int candidateIndex = fromStart ? index : candidateSamples.size() - window + index;
            String expected = normalize(referenceSamples.get(referenceIndex).key());
            String actual = normalize(candidateSamples.get(candidateIndex).key());
            if (expected.equals(actual)) {
                matches++;
            }
        }
        return (double) matches / window;
    }

    private String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase();
    }
}
