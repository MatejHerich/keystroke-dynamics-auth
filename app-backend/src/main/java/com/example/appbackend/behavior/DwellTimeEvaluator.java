package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DwellTimeEvaluator implements BehaviorEvaluator {

    private static final double MAX_ACCEPTABLE_AVERAGE_DEVIATION_MS = 80.0;

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        int comparableLength = Math.min(referenceSamples.size(), candidateSamples.size());
        if (comparableLength == 0) {
            return new EvaluatorResult("DwellTime", 0.0, "Nebolo možné porovnať dwell time.");
        }

        double totalDeviation = 0.0;
        for (int index = 0; index < comparableLength; index++) {
            Double expected = referenceSamples.get(index).dwellTime();
            Double actual = candidateSamples.get(index).dwellTime();
            if (expected == null || actual == null) {
                totalDeviation += MAX_ACCEPTABLE_AVERAGE_DEVIATION_MS;
                continue;
            }
            totalDeviation += Math.abs(expected - actual);
        }

        double averageDeviation = totalDeviation / comparableLength;
        double score = Math.max(0.0, 1.0 - (averageDeviation / MAX_ACCEPTABLE_AVERAGE_DEVIATION_MS));

        return new EvaluatorResult(
                "DwellTime",
                score,
                "Priemerná odchýlka dwell time: " + String.format("%.2f", averageDeviation) + " ms"
        );
    }
}
