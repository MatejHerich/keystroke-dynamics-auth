package com.example.appbackend.controller;

import com.example.appbackend.behavior.BehavioralAuthenticationResult;
import com.example.appbackend.model.Account;
import com.example.appbackend.model.BiometricSample;
import com.example.appbackend.model.User;
import com.example.appbackend.repository.AccountRepository;
import com.example.appbackend.repository.BiometricRepository;
import com.example.appbackend.repository.TransactionRepository;
import com.example.appbackend.repository.UserRepository;
import com.example.appbackend.service.BehavioralBiometricService;
import com.example.appbackend.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080"}, allowCredentials = "true")
public class AuthController {

    private static final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PASSWORD_ITERATIONS = 600_000;
    private static final int PASSWORD_SALT_LENGTH = 16;
    private static final int PASSWORD_KEY_LENGTH = 256;
    private static final String PASSWORD_PREFIX = "pbkdf2";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BiometricRepository biometricRepository;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BehavioralBiometricService behavioralBiometricService;

    private final SecureRandom secureRandom = new SecureRandom();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        Double balance = data.get("balance") == null ? 0.0 : Double.parseDouble(data.get("balance").toString());

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Meno a heslo sú povinné."));
        }
        if (userRepository.findByUsername(username) != null) {
            return ResponseEntity.status(409).body(Map.of("message", "Používateľ s týmto menom už existuje."));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashPassword(password));
        userRepository.save(user);

        String iban = generateIban(user.getId());
        Account account = new Account();
        account.setUser(user);
        account.setIban(iban);
        account.setBalance(balance);
        accountRepository.save(account);

        return ResponseEntity.ok(Map.of(
                "message", "Registrácia úspešná. Môžete sa prihlásiť.",
                "iban", iban
        ));
    }

    private String generateIban(Long userId) {
        String accountNumber = String.format("%016d", userId);
        return "SK00" + "1100" + accountNumber;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> data, HttpSession session, HttpServletRequest request) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        List<Map<String, Object>> biometrics = (List<Map<String, Object>>) data.get("biometrics");
        User existingUser = userRepository.findByUsername(username);
        boolean authenticated = false;
        if (existingUser != null) {
            if (isHashedPassword(existingUser.getPassword())) {
                authenticated = matchesPassword(password, existingUser.getPassword());
            } else if (existingUser.getPassword().equals(password)) {
                authenticated = true;
                existingUser.setPassword(hashPassword(password));
                userRepository.save(existingUser);
            }
        }

        if (authenticated) {
            session.invalidate();
            HttpSession authenticatedSession = request.getSession(true);
            authenticatedSession.setAttribute("loggedUser", existingUser.getUsername());
            for (Map<String, Object> sampleData : biometrics == null ? List.<Map<String, Object>>of() : biometrics) {
                BiometricSample sample = new BiometricSample();
                sample.setUser(existingUser);
                sample.setFieldName((String) sampleData.get("field"));
                sample.setKeyPressed((String) sampleData.get("key"));
                sample.setDwellTime(Double.parseDouble(sampleData.get("dwell").toString()));
                sample.setFlightTime(sampleData.get("flight") == null ? null : Double.parseDouble(sampleData.get("flight").toString()));

                biometricRepository.save(sample);
            }
            return ResponseEntity.ok(Map.of(
                    "message", "Prihlásenie úspešné. Dáta pre biometriu boli zaznamenané.",
                    "username", existingUser.getUsername()
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Chyba: Nesprávne meno alebo heslo!"));
        }
    }

    @GetMapping("/session")
    public ResponseEntity<?> sessionInfo(HttpSession session) {
        String username = getSessionUsername(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
        }
        return ResponseEntity.ok(Map.of("username", username));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Odhlásenie úspešné."));
    }

    @GetMapping("/account-info")
    public ResponseEntity<?> accountInfo(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
        }
        Map<String, Object> response = new HashMap<>();

        Account account = accountRepository.findByUser(user);
        if (account != null) {
            response.put("username", user.getUsername());
            response.put("balance", account.getBalance());
            response.put("iban", account.getIban());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/behavioral-status")
    public ResponseEntity<?> behavioralStatus(HttpSession session) {
        String username = getSessionUsername(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
        }

        BehavioralBiometricService.ProfileStatus profileStatus = behavioralBiometricService.getProfileStatus(username);
        return ResponseEntity.ok(Map.of(
                "paymentEnabled", profileStatus.paymentEnabled(),
                "collectedSamples", profileStatus.collectedSamples(),
                "requiredSamples", profileStatus.requiredSamples(),
                "remainingSamples", profileStatus.remainingSamples(),
                "message", profileStatus.message(),
                "profile", profileStatus.profile() == null ? Map.of() : Map.of(
                        "averageDwellTime", profileStatus.profile().getAverageDwellTime(),
                        "averageFlightTime", profileStatus.profile().getAverageFlightTime(),
                        "dwellDeviation", profileStatus.profile().getDwellDeviation(),
                        "flightDeviation", profileStatus.profile().getFlightDeviation(),
                        "longPauseRatio", profileStatus.profile().getLongPauseRatio(),
                        "referenceAttempts", profileStatus.profile().getReferenceAttempts(),
                        "referenceSamples", profileStatus.profile().getReferenceSamples()
                )
        ));
    }

    @GetMapping("/behavioral-debug")
    public ResponseEntity<?> behavioralDebug(HttpSession session) {
        String username = getSessionUsername(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
        }
        return ResponseEntity.ok(behavioralBiometricService.getDebugOverview(username));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> transactionHistory(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
        }

        Account account = accountRepository.findByUser(user);
        if (account == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Účet používateľa neexistuje."));
        }

        List<Map<String, Object>> response = transactionRepository.findByAccountOrderByTransactionDateDesc(account)
                .stream()
                .map(transaction -> {
                    Map<String, Object> item = new HashMap<>();
                    String transactionType = transaction.getTransactionType() == null
                            ? "OUTGOING"
                            : transaction.getTransactionType().trim().toUpperCase(Locale.ROOT);
                    double amount = transaction.getAmount() == null ? 0.0 : transaction.getAmount();
                    double signedAmount = "INCOMING".equals(transactionType) ? amount : -amount;
                    item.put("id", transaction.getId());
                    item.put("recipientIban", transaction.getRecipientIban());
                    item.put("amount", amount);
                    item.put("signedAmount", signedAmount);
                    item.put("description", transaction.getDescription());
                    item.put("transactionType", transactionType);
                    item.put("transactionDate", transaction.getTransactionDate());
                    return item;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/behavioral-training")
    public ResponseEntity<?> behavioralTraining(@RequestBody Map<String, Object> payload, HttpSession session) {
        String username = getSessionUsername(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
        }

        List<Map<String, Object>> biometrics = (List<Map<String, Object>>) payload.get("phraseBiometrics");
        try {
            BehavioralBiometricService.ProfileStatus profileStatus =
                    behavioralBiometricService.registerTrainingSamples(username, biometrics);

            return ResponseEntity.ok(Map.of(
                    "message", profileStatus.paymentEnabled()
                            ? "Behaviorálny profil je pripravený. Reálne platby sú odomknuté."
                            : "Pseudo-autorizácia bola zaznamenaná. Pokračujte v budovaní profilu.",
                    "paymentEnabled", profileStatus.paymentEnabled(),
                    "collectedSamples", profileStatus.collectedSamples(),
                    "requiredSamples", profileStatus.requiredSamples(),
                    "remainingSamples", profileStatus.remainingSamples()
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> payload, HttpSession session) {
        String username = getSessionUsername(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
        }
        String recipientIban = (String) payload.get("recipientIban");
        Double amount = payload.get("amount") == null ? null : Double.parseDouble(payload.get("amount").toString());
        String variableSymbol = (String) payload.get("variableSymbol");
        String paymentNote = (String) payload.get("paymentNote");
        List<Map<String, Object>> biometrics = (List<Map<String, Object>>) payload.get("phraseBiometrics");

        if (biometrics == null || biometrics.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Chýbajú biometrické dáta pre potvrdenie platby."));
        }

        BehavioralAuthenticationResult authenticationResult = behavioralBiometricService.evaluatePaymentBehavior(username, amount, biometrics);
        behavioralBiometricService.registerPaymentAttempt(username, biometrics, authenticationResult);
        if (!authenticationResult.authenticated()) {
            return ResponseEntity.status(403).body(Map.of(
                    "message", authenticationResult.message(),
                    "confidenceScore", authenticationResult.confidenceScore(),
                    "requiredThreshold", authenticationResult.requiredThreshold(),
                    "paymentEnabled", behavioralBiometricService.getProfileStatus(username).paymentEnabled(),
                    "details", authenticationResult.evaluatorSummaries()
            ));
        }

        try {
            TransactionService.TransactionResult result = transactionService.executeTransaction(
                    username,
                    recipientIban,
                    amount,
                    variableSymbol,
                    paymentNote
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", result.message());
            response.put("transactionId", result.transactionId());
            response.put("updatedBalance", result.updatedBalance());
            response.put("behavioralMessage", authenticationResult.message());
            response.put("confidenceScore", authenticationResult.confidenceScore());
            response.put("requiredThreshold", authenticationResult.requiredThreshold());
            response.put("enrollmentMode", authenticationResult.enrollmentMode());
            response.put("details", authenticationResult.evaluatorSummaries());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    private String getSessionUsername(HttpSession session) {
        Object username = session.getAttribute("loggedUser");
        return username == null ? null : username.toString();
    }

    private User getAuthenticatedUser(HttpSession session) {
        String username = getSessionUsername(session);
        if (username == null) {
            return null;
        }
        return userRepository.findByUsername(username);
    }

    private String hashPassword(String rawPassword) {
        byte[] salt = new byte[PASSWORD_SALT_LENGTH];
        secureRandom.nextBytes(salt);
        byte[] hash = derivePassword(rawPassword, salt, PASSWORD_ITERATIONS);
        return PASSWORD_PREFIX + "$" + PASSWORD_ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || !isHashedPassword(storedPassword)) {
            return false;
        }

        String[] parts = storedPassword.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
        byte[] actualHash = derivePassword(rawPassword, salt, iterations);
        return constantTimeEquals(expectedHash, actualHash);
    }

    private boolean isHashedPassword(String password) {
        return password != null && password.startsWith(PASSWORD_PREFIX + "$");
    }

    private byte[] derivePassword(String rawPassword, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, PASSWORD_KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Nepodarilo sa spracovať hash hesla.", exception);
        }
    }

    private boolean constantTimeEquals(byte[] left, byte[] right) {
        if (left.length != right.length) {
            return false;
        }

        int result = 0;
        for (int index = 0; index < left.length; index++) {
            result |= left[index] ^ right[index];
        }
        return result == 0;
    }
}
