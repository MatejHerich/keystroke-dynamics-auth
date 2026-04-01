package com.example.appbackend.behavior;

import java.util.List;

public interface BehaviorEvaluator {
    EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples);

    record EvaluatorResult(String name, double score, String summary) {
    }
}
