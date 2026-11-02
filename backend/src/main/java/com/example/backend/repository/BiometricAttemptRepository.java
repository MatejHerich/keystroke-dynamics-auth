package com.example.backend.repository;

import com.example.backend.entity.AttemptType;
import com.example.backend.entity.BiometricAttempt;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BiometricAttemptRepository extends JpaRepository<BiometricAttempt, Integer> {
    long countByUserAndAttemptType(User user, AttemptType attemptType);
    List<BiometricAttempt> findByUserAndAttemptTypeOrderByIdAsc(User user, AttemptType attemptType);
}
