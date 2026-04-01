package com.example.appbackend.behavior;

import java.util.List;

public record BehavioralAuthenticationResult(
        boolean authenticated,
        boolean enrollmentMode,
        double confidenceScore,
        double requiredThreshold,
        String message,
        List<String> evaluatorSummaries
) {
}
