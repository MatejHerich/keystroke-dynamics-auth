package com.example.appbackend.service;

import com.example.appbackend.behavior.*;
import com.example.appbackend.model.BehaviorAttempt;
import com.example.appbackend.model.BehavioralProfile;
import com.example.appbackend.model.BiometricSample;
import com.example.appbackend.model.User;
import com.example.appbackend.model.VerificationSample;
import com.example.appbackend.repository.BehaviorAttemptRepository;
import com.example.appbackend.repository.BehavioralProfileRepository;
import com.example.appbackend.repository.BiometricRepository;
import com.example.appbackend.repository.UserRepository;
import com.example.appbackend.repository.VerificationSampleRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BehavioralBiometricService {

    private static final int MIN_REFERENCE_VERIFICATION_SAMPLES = 60;
    private static final int MAX_REFERENCE_ATTEMPTS = 12;
    private static final double BASE_PAYMENT_THRESHOLD = 0.74;
    private static final double LARGE_PAYMENT_THRESHOLD = 0.82;
    private static final double VERY_LARGE_PAYMENT_THRESHOLD = 0.88;

    private static final Map<String, Double> EVALUATOR_WEIGHTS = Map.ofEntries(
            Map.entry("KeySequence", 0.08),
            Map.entry("DwellTime", 0.16),
            Map.entry("Stability", 0.10),
            Map.entry("FlightTime", 0.18),
            Map.entry("Cadence", 0.18),
            Map.entry("PausePattern", 0.10),
            Map.entry("Digraph", 0.10),
            Map.entry("Corrections", 0.04),
            Map.entry("Boundary", 0.04),
            Map.entry("WordTempo", 0.04),
            Map.entry("Profile", 0.16)
    );

    private final UserRepository userRepository;
    private final BiometricRepository biometricRepository;
    private final VerificationSampleRepository verificationSampleRepository;
    private final BehaviorAttemptRepository behaviorAttemptRepository;
    private final BehavioralProfileRepository behavioralProfileRepository;
    private final List<BehaviorEvaluator> evaluators;

    public BehavioralBiometricService(
            UserRepository userRepository,
            BiometricRepository biometricRepository,
            VerificationSampleRepository verificationSampleRepository,
            BehaviorAttemptRepository behaviorAttemptRepository,
            BehavioralProfileRepository behavioralProfileRepository,
            KeySequenceEvaluator keySequenceEvaluator,
            DwellTimeEvaluator dwellTimeEvaluator,
            KeyHoldStabilityEvaluator keyHoldStabilityEvaluator,
            FlightTimeEvaluator flightTimeEvaluator,
            RhythmCadenceEvaluator rhythmCadenceEvaluator,
            PausePatternEvaluator pausePatternEvaluator,
            DigraphConsistencyEvaluator digraphConsistencyEvaluator,
            CorrectionBehaviorEvaluator correctionBehaviorEvaluator,
            BoundaryStabilityEvaluator boundaryStabilityEvaluator,
            WordTransitionEvaluator wordTransitionEvaluator
    ) {
        this.userRepository = userRepository;
        this.biometricRepository = biometricRepository;
        this.verificationSampleRepository = verificationSampleRepository;
        this.behaviorAttemptRepository = behaviorAttemptRepository;
        this.behavioralProfileRepository = behavioralProfileRepository;
        this.evaluators = List.of(
                keySequenceEvaluator,
                dwellTimeEvaluator,
                keyHoldStabilityEvaluator,
                flightTimeEvaluator,
                rhythmCadenceEvaluator,
                pausePatternEvaluator,
                digraphConsistencyEvaluator,
                correctionBehaviorEvaluator,
                boundaryStabilityEvaluator,
                wordTransitionEvaluator
        );
    }

    public BehavioralAuthenticationResult evaluatePaymentBehavior(
            String username,
            Double amount,
            List<Map<String, Object>> biometricPayload
    ) {
        List<KeystrokeSample> candidateSamples = mapPayloadSamples(biometricPayload);
        double requiredThreshold = resolveThreshold("PAYMENT", amount);

        if (candidateSamples.isEmpty()) {
            return new BehavioralAuthenticationResult(
                    false,
                    false,
                    0.0,
                    requiredThreshold,
                    "Chýbajú key stroke dáta pre behaviorálne overenie.",
                    List.of("Bez vstupných vzoriek nie je možné vyhodnotiť používateľa.")
            );
        }

        ProfileStatus profileStatus = getProfileStatus(username);
        if (!profileStatus.paymentEnabled()) {
            return new BehavioralAuthenticationResult(
                    false,
                    false,
                    0.0,
                    requiredThreshold,
                    "Behaviorálny profil ešte nie je pripravený na autorizáciu platieb.",
                    List.of(
                            "Nazbierané vzorky: " + profileStatus.collectedSamples() + "/" + profileStatus.requiredSamples(),
                            "Najprv je potrebné dokončiť pseudo-autorizáciu v dashboarde."
                    )
            );
        }

        List<KeystrokeSample> referenceSamples = loadReferenceSamples(username);
        BehavioralProfile profile = ensureBehavioralProfile(username);

        List<BehaviorEvaluator.EvaluatorResult> evaluatorResults = evaluators.stream()
                .map(evaluator -> evaluator.evaluate(referenceSamples, candidateSamples))
                .toList();

        BehaviorEvaluator.EvaluatorResult profileResult = evaluateAgainstProfile(profile, candidateSamples);
        List<BehaviorEvaluator.EvaluatorResult> allResults = new ArrayList<>(evaluatorResults);
        allResults.add(profileResult);

        double finalScore = calculateWeightedScore(allResults);
        boolean authenticated = finalScore >= requiredThreshold;
        List<String> summaries = allResults.stream()
                .map(result -> {
                    double weight = EVALUATOR_WEIGHTS.getOrDefault(result.name(), 0.1);
                    return result.name() + ": " + result.summary()
                            + " | score=" + String.format("%.2f", result.score())
                            + " | weight=" + String.format("%.2f", weight);
                })
                .toList();

        String message = authenticated
                ? "Behaviorálne overenie bolo úspešné."
                : "Behaviorálne overenie zlyhalo. Štýl písania sa príliš odlišuje od referenčného profilu.";

        return new BehavioralAuthenticationResult(
                authenticated,
                false,
                roundScore(finalScore),
                roundScore(requiredThreshold),
                message,
                summaries
        );
    }

    public ProfileStatus getProfileStatus(String username) {
        List<BehaviorAttempt> successfulAttempts = behaviorAttemptRepository.findByUsernameAndAuthenticatedTrueOrderByCreatedAtDesc(username);
        int collectedSamples = successfulAttempts.stream()
                .map(BehaviorAttempt::getSampleCount)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum();
        int remainingSamples = Math.max(0, MIN_REFERENCE_VERIFICATION_SAMPLES - collectedSamples);
        boolean paymentEnabled = collectedSamples >= MIN_REFERENCE_VERIFICATION_SAMPLES;
        BehavioralProfile profile = behavioralProfileRepository.findByUsername(username);
        String message = paymentEnabled
                ? "Behaviorálny profil je pripravený. Používateľ môže autorizovať reálne platby."
                : "Používateľ musí ešte dokončiť pseudo-autorizáciu a nazbierať ďalšie vzorky.";

        return new ProfileStatus(
                paymentEnabled,
                collectedSamples,
                MIN_REFERENCE_VERIFICATION_SAMPLES,
                remainingSamples,
                message,
                profile
        );
    }

    public ProfileStatus registerTrainingSamples(String username, List<Map<String, Object>> biometricPayload) {
        List<KeystrokeSample> candidateSamples = mapPayloadSamples(biometricPayload);
        if (candidateSamples.isEmpty()) {
            throw new IllegalArgumentException("Chýbajú key stroke dáta pre tréning behaviorálneho profilu.");
        }

        List<String> evaluatorDetails = List.of(
                "TrainingMode: Pseudo-autorizácia bola uložená pre budovanie behaviorálneho profilu.",
                "RequiredThreshold: 0.00",
                "SampleCount: " + candidateSamples.size()
        );
        saveBehaviorAttempt(username, "TRAINING", true, 1.0, 0.0, evaluatorDetails, candidateSamples);
        rebuildProfile(username);
        return getProfileStatus(username);
    }

    public void registerPaymentAttempt(String username, List<Map<String, Object>> biometricPayload, BehavioralAuthenticationResult result) {
        List<KeystrokeSample> candidateSamples = mapPayloadSamples(biometricPayload);
        if (candidateSamples.isEmpty()) {
            return;
        }
        saveBehaviorAttempt(
                username,
                "PAYMENT",
                result.authenticated(),
                result.confidenceScore(),
                result.requiredThreshold(),
                result.evaluatorSummaries(),
                candidateSamples
        );
        if (result.authenticated()) {
            rebuildProfile(username);
        }
    }

    public Map<String, Object> getDebugOverview(String username) {
        ProfileStatus status = getProfileStatus(username);
        BehavioralProfile profile = ensureBehavioralProfile(username);
        List<Map<String, Object>> attempts = behaviorAttemptRepository.findTop20ByUsernameOrderByCreatedAtDesc(username).stream()
                .map(attempt -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", attempt.getId());
                    item.put("attemptType", attempt.getAttemptType());
                    item.put("authenticated", attempt.getAuthenticated());
                    item.put("confidenceScore", attempt.getConfidenceScore());
                    item.put("requiredThreshold", attempt.getRequiredThreshold());
                    item.put("sampleCount", attempt.getSampleCount());
                    item.put("createdAt", attempt.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    item.put("evaluatorDetails", attempt.getEvaluatorDetails() == null ? "" : attempt.getEvaluatorDetails());
                    return item;
                })
                .toList();

        Map<String, Object> profileMap = new LinkedHashMap<>();
        if (profile != null) {
            profileMap.put("averageDwellTime", roundScore(profile.getAverageDwellTime()));
            profileMap.put("averageFlightTime", roundScore(profile.getAverageFlightTime()));
            profileMap.put("dwellDeviation", roundScore(profile.getDwellDeviation()));
            profileMap.put("flightDeviation", roundScore(profile.getFlightDeviation()));
            profileMap.put("longPauseRatio", roundScore(profile.getLongPauseRatio()));
            profileMap.put("referenceAttempts", profile.getReferenceAttempts());
            profileMap.put("referenceSamples", profile.getReferenceSamples());
            profileMap.put("updatedAt", profile.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return Map.of(
                "status", Map.of(
                        "paymentEnabled", status.paymentEnabled(),
                        "collectedSamples", status.collectedSamples(),
                        "requiredSamples", status.requiredSamples(),
                        "remainingSamples", status.remainingSamples(),
                        "message", status.message()
                ),
                "weights", EVALUATOR_WEIGHTS,
                "thresholds", Map.of(
                        "training", 0.0,
                        "payment", BASE_PAYMENT_THRESHOLD,
                        "largePayment", LARGE_PAYMENT_THRESHOLD,
                        "veryLargePayment", VERY_LARGE_PAYMENT_THRESHOLD
                ),
                "profile", profileMap,
                "attempts", attempts
        );
    }

    private List<KeystrokeSample> loadReferenceSamples(String username) {
        List<BehaviorAttempt> successfulAttempts = behaviorAttemptRepository.findByUsernameAndAuthenticatedTrueOrderByCreatedAtDesc(username);
        if (!successfulAttempts.isEmpty()) {
            return normalizeAttempts(successfulAttempts.stream().limit(MAX_REFERENCE_ATTEMPTS).toList());
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return List.of();
        }

        return biometricRepository.findByUserOrderByIdAsc(user).stream()
                .sorted(Comparator.comparing(BiometricSample::getId))
                .map(sample -> new KeystrokeSample(sample.getKeyPressed(), sample.getDwellTime(), sample.getFlightTime()))
                .toList();
    }

    private List<KeystrokeSample> mapPayloadSamples(List<Map<String, Object>> biometricPayload) {
        if (biometricPayload == null) {
            return List.of();
        }

        return biometricPayload.stream()
                .map(sample -> new KeystrokeSample(
                        sample.get("key") == null ? null : sample.get("key").toString(),
                        sample.get("dwell") == null ? null : Double.parseDouble(sample.get("dwell").toString()),
                        sample.get("flight") == null ? null : Double.parseDouble(sample.get("flight").toString())
                ))
                .toList();
    }

    private List<KeystrokeSample> normalizeAttempts(List<BehaviorAttempt> attempts) {
        Map<Integer, List<KeystrokeSample>> samplesByIndex = new HashMap<>();

        for (BehaviorAttempt attempt : attempts) {
            List<VerificationSample> samples = verificationSampleRepository.findByAttemptIdOrderBySampleIndexAsc(attempt.getId());
            for (VerificationSample sample : samples) {
                int sampleIndex = sample.getSampleIndex() == null ? 0 : sample.getSampleIndex();
                samplesByIndex.computeIfAbsent(sampleIndex, ignored -> new ArrayList<>())
                        .add(new KeystrokeSample(sample.getKeyPressed(), sample.getDwellTime(), sample.getFlightTime()));
            }
        }

        return samplesByIndex.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> normalizeSampleGroup(entry.getValue()))
                .toList();
    }

    private KeystrokeSample normalizeSampleGroup(List<KeystrokeSample> samples) {
        String dominantKey = samples.stream()
                .map(KeystrokeSample::key)
                .filter(key -> key != null && !key.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        Double averageDwell = average(samples.stream().map(KeystrokeSample::dwellTime).toList());
        Double averageFlight = average(samples.stream().map(KeystrokeSample::flightTime).toList());

        return new KeystrokeSample(dominantKey, averageDwell, averageFlight);
    }

    private double calculateWeightedScore(List<BehaviorEvaluator.EvaluatorResult> results) {
        double weightedScore = 0.0;
        double totalWeight = 0.0;

        for (BehaviorEvaluator.EvaluatorResult result : results) {
            double weight = EVALUATOR_WEIGHTS.getOrDefault(result.name(), 0.1);
            weightedScore += result.score() * weight;
            totalWeight += weight;
        }

        return totalWeight == 0.0 ? 0.0 : weightedScore / totalWeight;
    }

    private double resolveThreshold(String operationType, Double amount) {
        if ("TRAINING".equals(operationType)) {
            return 0.0;
        }
        if (amount != null && amount >= 5000) {
            return VERY_LARGE_PAYMENT_THRESHOLD;
        }
        if (amount != null && amount >= 1000) {
            return LARGE_PAYMENT_THRESHOLD;
        }
        return BASE_PAYMENT_THRESHOLD;
    }

    private BehavioralProfile ensureBehavioralProfile(String username) {
        BehavioralProfile profile = behavioralProfileRepository.findByUsername(username);
        if (profile != null) {
            return profile;
        }
        return rebuildProfile(username);
    }

    private BehavioralProfile rebuildProfile(String username) {
        List<BehaviorAttempt> successfulAttempts = behaviorAttemptRepository.findByUsernameAndAuthenticatedTrueOrderByCreatedAtDesc(username);
        List<KeystrokeSample> referenceSamples = normalizeAttempts(successfulAttempts.stream().limit(MAX_REFERENCE_ATTEMPTS).toList());
        if (referenceSamples.isEmpty()) {
            return null;
        }

        BehavioralProfile profile = behavioralProfileRepository.findByUsername(username);
        if (profile == null) {
            profile = new BehavioralProfile();
            profile.setUsername(username);
        }

        profile.setAverageDwellTime(defaultZero(average(referenceSamples.stream().map(KeystrokeSample::dwellTime).toList())));
        profile.setAverageFlightTime(defaultZero(average(referenceSamples.stream().map(KeystrokeSample::flightTime).toList())));
        profile.setDwellDeviation(defaultZero(calculateDeviation(referenceSamples.stream().map(KeystrokeSample::dwellTime).toList(), profile.getAverageDwellTime())));
        profile.setFlightDeviation(defaultZero(calculateDeviation(referenceSamples.stream().map(KeystrokeSample::flightTime).toList(), profile.getAverageFlightTime())));
        profile.setLongPauseRatio(calculateLongPauseRatio(referenceSamples));
        profile.setReferenceAttempts(successfulAttempts.size());
        profile.setReferenceSamples(referenceSamples.size());
        return behavioralProfileRepository.save(profile);
    }

    private BehaviorEvaluator.EvaluatorResult evaluateAgainstProfile(BehavioralProfile profile, List<KeystrokeSample> candidateSamples) {
        if (profile == null || candidateSamples.isEmpty()) {
            return new BehaviorEvaluator.EvaluatorResult("Profile", 0.0, "Profil používateľa zatiaľ nie je dostupný.");
        }

        double avgDwell = defaultZero(average(candidateSamples.stream().map(KeystrokeSample::dwellTime).toList()));
        double avgFlight = defaultZero(average(candidateSamples.stream().map(KeystrokeSample::flightTime).toList()));
        double pauseRatio = calculateLongPauseRatio(candidateSamples);

        double dwellScore = scoreByDeviation(avgDwell, profile.getAverageDwellTime(), Math.max(30.0, profile.getDwellDeviation() + 20.0));
        double flightScore = scoreByDeviation(avgFlight, profile.getAverageFlightTime(), Math.max(40.0, profile.getFlightDeviation() + 30.0));
        double pauseScore = Math.max(0.0, 1.0 - Math.abs(pauseRatio - profile.getLongPauseRatio()));
        double finalScore = (dwellScore + flightScore + pauseScore) / 3.0;

        return new BehaviorEvaluator.EvaluatorResult(
                "Profile",
                finalScore,
                "Zhoda s profilom: dwell=" + String.format("%.2f", dwellScore)
                        + ", flight=" + String.format("%.2f", flightScore)
                        + ", pauses=" + String.format("%.2f", pauseScore)
        );
    }

    private double scoreByDeviation(double value, double reference, double tolerance) {
        return Math.max(0.0, 1.0 - (Math.abs(value - reference) / tolerance));
    }

    private Double average(List<Double> values) {
        double sum = 0.0;
        int count = 0;
        for (Double value : values) {
            if (value == null) {
                continue;
            }
            sum += value;
            count++;
        }
        return count == 0 ? null : sum / count;
    }

    private double calculateDeviation(List<Double> values, Double mean) {
        if (mean == null) {
            return 0.0;
        }
        double sum = 0.0;
        int count = 0;
        for (Double value : values) {
            if (value == null) {
                continue;
            }
            sum += Math.abs(value - mean);
            count++;
        }
        return count == 0 ? 0.0 : sum / count;
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
                .filter(value -> value != null && value >= 180.0)
                .count();
        return (double) longPauses / allFlights;
    }

    private void saveBehaviorAttempt(
            String username,
            String attemptType,
            boolean authenticated,
            double confidenceScore,
            double requiredThreshold,
            List<String> evaluatorDetails,
            List<KeystrokeSample> samples
    ) {
        BehaviorAttempt attempt = new BehaviorAttempt();
        attempt.setUsername(username);
        attempt.setAttemptType(attemptType);
        attempt.setAuthenticated(authenticated);
        attempt.setConfidenceScore(roundScore(confidenceScore));
        attempt.setRequiredThreshold(roundScore(requiredThreshold));
        attempt.setSampleCount(samples.size());
        attempt.setEvaluatorDetails(String.join("\n", evaluatorDetails));
        BehaviorAttempt savedAttempt = behaviorAttemptRepository.save(attempt);

        for (int index = 0; index < samples.size(); index++) {
            KeystrokeSample sample = samples.get(index);
            VerificationSample verificationSample = new VerificationSample();
            verificationSample.setUsername(username);
            verificationSample.setKeyPressed(sample.key());
            verificationSample.setDwellTime(sample.dwellTime());
            verificationSample.setFlightTime(sample.flightTime());
            verificationSample.setSampleIndex(index);
            verificationSample.setAttempt(savedAttempt);
            verificationSampleRepository.save(verificationSample);
        }
    }

    private double defaultZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private double roundScore(double score) {
        return Math.round(score * 100.0) / 100.0;
    }

    public record ProfileStatus(
            boolean paymentEnabled,
            int collectedSamples,
            int requiredSamples,
            int remainingSamples,
            String message,
            BehavioralProfile profile
    ) {
    }
}
