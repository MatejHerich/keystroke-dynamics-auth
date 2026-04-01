package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WordTransitionEvaluator implements BehaviorEvaluator {

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        Double referenceTransition = extractSpaceTransition(referenceSamples);
        Double candidateTransition = extractSpaceTransition(candidateSamples);
        if (referenceTransition == null || candidateTransition == null) {
            return new EvaluatorResult("WordTempo", 0.0, "Nie sú dostupné dáta pre tempo medzi slovami.");
        }

        double deviation = Math.abs(referenceTransition - candidateTransition);
        double score = Math.max(0.0, 1.0 - (deviation / 120.0));
        return new EvaluatorResult(
                "WordTempo",
                score,
                "Odchýlka tempa pri prechode medzi slovami: " + String.format("%.2f", deviation) + " ms"
        );
    }

    private Double extractSpaceTransition(List<KeystrokeSample> samples) {
        for (int index = 0; index < samples.size(); index++) {
            String key = samples.get(index).key();
            if (key != null && key.equals(" ")) {
                return samples.get(index).flightTime();
            }
        }
        return null;
    }
}
