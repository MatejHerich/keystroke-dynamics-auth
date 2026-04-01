package com.example.appbackend.behavior;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DigraphConsistencyEvaluator implements BehaviorEvaluator {

    @Override
    public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
        List<String> referenceDigraphs = buildDigraphs(referenceSamples);
        List<String> candidateDigraphs = buildDigraphs(candidateSamples);
        int comparableLength = Math.min(referenceDigraphs.size(), candidateDigraphs.size());
        if (comparableLength == 0) {
            return new EvaluatorResult("Digraph", 0.0, "Nebolo možné porovnať dvojice kláves.");
        }

        int matches = 0;
        for (int index = 0; index < comparableLength; index++) {
            if (referenceDigraphs.get(index).equals(candidateDigraphs.get(index))) {
                matches++;
            }
        }

        double score = (double) matches / comparableLength;
        return new EvaluatorResult(
                "Digraph",
                score,
                "Zhoda dvojíc kláves: " + matches + "/" + comparableLength
        );
    }

    private List<String> buildDigraphs(List<KeystrokeSample> samples) {
        List<String> result = new ArrayList<>();
        for (int index = 1; index < samples.size(); index++) {
            String previous = normalize(samples.get(index - 1).key());
            String current = normalize(samples.get(index).key());
            result.add(previous + "->" + current);
        }
        return result;
    }

    private String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase();
    }
}
