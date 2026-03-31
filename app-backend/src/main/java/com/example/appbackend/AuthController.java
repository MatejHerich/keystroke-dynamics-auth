package com.example.appbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BiometricRepository biometricRepository;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private VerificationSampleRepository verificationSampleRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        List<Map<String, Object>> biometrics = (List<Map<String, Object>>) data.get("biometrics");
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null && existingUser.getPassword().equals(password)) {
            for (Map<String, Object> sampleData : biometrics) {
                BiometricSample sample = new BiometricSample();
                sample.setUser(existingUser);
                sample.setFieldName((String) sampleData.get("field"));
                sample.setKeyPressed((String) sampleData.get("key"));
                sample.setDwellTime(Double.parseDouble(sampleData.get("dwell").toString()));

                biometricRepository.save(sample);
            }
            return "Prihlásenie úspešné. Dáta pre biometriu boli zaznamenané.";
        } else {
            return "Chyba: Nesprávne meno alebo heslo!";
        }
    }

    @GetMapping("/account-info/{username}")
    public Map<String, Object> accountInfo(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            Account account = accountRepository.findByUser(user);
            if (account != null) {
                response.put("username", user.getUsername());
                response.put("balance", account.getBalance());
                response.put("iban", account.getIban());
            }
        }
        return response;
    }

    @GetMapping("/transactions/{username}")
    public ResponseEntity<?> transactionHistory(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Používateľ neexistuje."));
        }

        Account account = accountRepository.findByUser(user);
        if (account == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Účet používateľa neexistuje."));
        }

        List<Map<String, Object>> response = transactionRepository.findByAccountOrderByTransactionDateDesc(account)
                .stream()
                .map(transaction -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", transaction.getId());
                    item.put("recipientIban", transaction.getRecipientIban());
                    item.put("amount", transaction.getAmount());
                    item.put("description", transaction.getDescription());
                    item.put("transactionDate", transaction.getTransactionDate());
                    return item;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String recipientIban = (String) payload.get("recipientIban");
        Double amount = payload.get("amount") == null ? null : Double.parseDouble(payload.get("amount").toString());
        String variableSymbol = (String) payload.get("variableSymbol");
        String paymentNote = (String) payload.get("paymentNote");
        List<Map<String, Object>> biometrics = (List<Map<String, Object>>) payload.get("phraseBiometrics");

        if (biometrics == null || biometrics.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Chýbajú biometrické dáta pre potvrdenie platby."));
        }

        for (Map<String, Object> sample : biometrics) {
            VerificationSample vs = new VerificationSample();
            vs.setUsername(username);
            vs.setKeyPressed((String) sample.get("key"));
            vs.setDwellTime(Double.parseDouble(sample.get("dwell").toString()));
            verificationSampleRepository.save(vs);
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
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
