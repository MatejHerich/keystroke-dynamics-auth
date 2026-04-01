package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RhythmCadenceEvaluator implements BehaviorEvaluator {

    private static final double MAX_ACCEPTABLE_CADENCE_DEVIATION_MS = 160.0;

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        int comparableLength = Math.min(referenceSamples.size(), candidateSamples.size());
        if (comparableLength == 0) {
            return new EvaluatorResult("Cadence", 0.0, "Nebolo možné vyhodnotiť rytmus písania.");
        }

        double totalDeviation = 0.0;
        int measuredSamples = 0;
        for (int index = 0; index < comparableLength; index++) {
            Double expectedCadence = buildCadence(referenceSamples.get(index));
            Double actualCadence = buildCadence(candidateSamples.get(index));
            if (expectedCadence == null || actualCadence == null) {
                continue;
            }
            totalDeviation += Math.abs(expectedCadence - actualCadence);
            measuredSamples++;
        }

        if (measuredSamples == 0) {
            return new EvaluatorResult("Cadence", 0.0, "Nie sú dostupné dáta pre cadence.");
        }

        double averageDeviation = totalDeviation / measuredSamples;
        double score = Math.max(0.0, 1.0 - (averageDeviation / MAX_ACCEPTABLE_CADENCE_DEVIATION_MS));

        return new EvaluatorResult(
                "Cadence",
                score,
                "Priemerná odchýlka rytmu písania: " + String.format("%.2f", averageDeviation) + " ms"
        );
    }

    private Double buildCadence(KeystrokeSample sample) {
        if (sample.dwellTime() == null && sample.flightTime() == null) {
            return null;
        }
        double dwell = sample.dwellTime() == null ? 0.0 : sample.dwellTime();
        double flight = sample.flightTime() == null ? 0.0 : sample.flightTime();
        return dwell + flight;
    }
}
