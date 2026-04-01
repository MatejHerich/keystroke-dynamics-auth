package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlightTimeEvaluator implements BehaviorEvaluator {

    private static final double MAX_ACCEPTABLE_AVERAGE_DEVIATION_MS = 120.0;

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        int comparableLength = Math.min(referenceSamples.size(), candidateSamples.size());
        if (comparableLength == 0) {
            return new EvaluatorResult("FlightTime", 0.0, "Nebolo možné porovnať flight time.");
        }

        double totalDeviation = 0.0;
        int measuredSamples = 0;
        for (int index = 0; index < comparableLength; index++) {
            Double expected = referenceSamples.get(index).flightTime();
            Double actual = candidateSamples.get(index).flightTime();
            if (expected == null || actual == null) {
                continue;
            }
            totalDeviation += Math.abs(expected - actual);
            measuredSamples++;
        }

        if (measuredSamples == 0) {
            return new EvaluatorResult("FlightTime", 0.0, "Flight time zatiaľ nie je dostupný.");
        }

        double averageDeviation = totalDeviation / measuredSamples;
        double score = Math.max(0.0, 1.0 - (averageDeviation / MAX_ACCEPTABLE_AVERAGE_DEVIATION_MS));

        return new EvaluatorResult(
                "FlightTime",
                score,
                "Priemerná odchýlka flight time: " + String.format("%.2f", averageDeviation) + " ms"
        );
    }
}
