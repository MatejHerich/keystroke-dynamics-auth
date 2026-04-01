package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PausePatternEvaluator implements BehaviorEvaluator {

    private static final double LONG_PAUSE_THRESHOLD_MS = 180.0;

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        if (referenceSamples.isEmpty() || candidateSamples.isEmpty()) {
            return new EvaluatorResult("PausePattern", 0.0, "Nie sú dostupné dáta pre pauzy medzi klávesmi.");
        }

        double referenceRatio = calculateLongPauseRatio(referenceSamples);
        double candidateRatio = calculateLongPauseRatio(candidateSamples);
        double deviation = Math.abs(referenceRatio - candidateRatio);
        double score = Math.max(0.0, 1.0 - deviation);

        return new EvaluatorResult(
                "PausePattern",
                score,
                "Odchýlka podielu dlhších pauz: " + String.format("%.2f", deviation)
        );
    }

    private double calculateLongPauseRatio(List<KeystrokeSample> samples) {
        long allFlights = samples.stream()
                .map(KeystrokeSample::flightTime)
                .filter(value -> value != null)
                .count();

        if (allFlights == 0) {
            return 0.0;
        }

        long longPauses = samples.stream()
                .map(KeystrokeSample::flightTime)
                .filter(value -> value != null && value >= LONG_PAUSE_THRESHOLD_MS)
                .count();

        return (double) longPauses / allFlights;
    }
}
