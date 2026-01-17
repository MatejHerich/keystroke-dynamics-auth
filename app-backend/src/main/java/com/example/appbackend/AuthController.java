package com.example.appbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired // 1. Spring automaticky "pripojí" most k tabuľke používateľov
    private UserRepository userRepository;

    @Autowired // 2. Spring automaticky "pripojí" most k tabuľke biometrie
    private BiometricRepository biometricRepository;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        List<Map<String, Object>> biometrics = (List<Map<String, Object>>) data.get("biometrics");

        // 1. KROK: Hľadáme používateľa v databáze
        User existingUser = userRepository.findByUsername(username);

        // 2. KROK: Overenie hesla a existencie
        if (existingUser != null && existingUser.getPassword().equals(password)) {

            // 3. KROK: Ak je heslo správne, uložíme biometrické vzorky pre neskoršiu analýzu
            for (Map<String, Object> sampleData : biometrics) {
                BiometricSample sample = new BiometricSample();
                sample.setUser(existingUser); // Priradíme k nájdenému používateľovi
                sample.setFieldName((String) sampleData.get("field"));
                sample.setKeyPressed((String) sampleData.get("key"));
                sample.setDwellTime(Double.parseDouble(sampleData.get("dwell").toString()));

                biometricRepository.save(sample);
            }

            return "Prihlásenie úspešné. Dáta pre biometriu boli zaznamenané.";
        } else {
            // 4. KROK: Ak meno alebo heslo nesedí
            return "Chyba: Nesprávne meno alebo heslo!";
        }
    }
}