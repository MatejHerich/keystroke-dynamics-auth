package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Component
public class KeyHoldStabilityEvaluator implements BehaviorEvaluator {

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        if (referenceSamples.isEmpty() || candidateSamples.isEmpty()) {
            return new EvaluatorResult("Stability", 0.0, "Nie sú dostupné dáta pre stabilitu písania.");
        }

        double referenceSpread = calculateSpread(referenceSamples);
        double candidateSpread = calculateSpread(candidateSamples);
        double deviation = Math.abs(referenceSpread - candidateSpread);
        double score = Math.max(0.0, 1.0 - (deviation / 60.0));

        return new EvaluatorResult(
                "Stability",
                score,
                "Odchýlka stability písania: " + String.format("%.2f", deviation)
        );
    }

    private double calculateSpread(List<KeystrokeSample> samples) {
        DoubleSummaryStatistics statistics = samples.stream()
                .map(KeystrokeSample::dwellTime)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();

        if (statistics.getCount() == 0) {
            return 0.0;
        }
        return statistics.getMax() - statistics.getMin();
    }
}
