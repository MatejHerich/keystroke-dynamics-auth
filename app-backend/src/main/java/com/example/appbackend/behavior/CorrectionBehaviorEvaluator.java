package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CorrectionBehaviorEvaluator implements BehaviorEvaluator {

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        double referenceRatio = calculateCorrectionRatio(referenceSamples);
        double candidateRatio = calculateCorrectionRatio(candidateSamples);
        double deviation = Math.abs(referenceRatio - candidateRatio);
        double score = Math.max(0.0, 1.0 - (deviation * 2.0));

        return new EvaluatorResult(
                "Corrections",
                score,
                "Odchýlka správania pri opravách: " + String.format("%.2f", deviation)
        );
    }

    private double calculateCorrectionRatio(List<KeystrokeSample> samples) {
        if (samples.isEmpty()) {
            return 0.0;
        }

        long correctionKeys = samples.stream()
                .map(KeystrokeSample::key)
                .filter(this::isCorrectionKey)
                .count();

        return (double) correctionKeys / samples.size();
    }

    private boolean isCorrectionKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.trim().toLowerCase();
        return normalized.equals("backspace") || normalized.equals("delete");
    }
}
