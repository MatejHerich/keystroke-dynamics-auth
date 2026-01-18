package com.example.appbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}