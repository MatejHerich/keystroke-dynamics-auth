package com.example.backend.service;

import com.example.backend.entity.AttemptType;
import com.example.backend.entity.User;
import com.example.backend.repository.BiometricAttemptRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingService {
    private final UserRepository userRepository;
    private final BiometricAttemptRepository biometricAttemptRepository;
    private final int requiredSamples;

    public TrainingService(UserRepository userRepository, BiometricAttemptRepository biometricAttemptRepository, @Value("${app.algorithm.training-samples}")  int requiredSamples) {
        this.userRepository = userRepository;
        this.biometricAttemptRepository = biometricAttemptRepository;
        this.requiredSamples = requiredSamples;

    }

    @Transactional(readOnly = true)
    public int CountSamples(String username) {
        User user =  userRepository.findByUsername(username).orElseThrow(()-> new IllegalStateException("User not found:" + username));
        return Math.toIntExact(biometricAttemptRepository.countByUserAndAttemptType(user, AttemptType.TRAINING));
    }

    public int getRequiredSamples() {
        return requiredSamples;
    }
}
