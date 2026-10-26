package com.example.backend.Repository;

import com.example.backend.Entity.AttemptType;
import com.example.backend.Entity.BiometricAttempt;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BiometricAttemptRepository extends JpaRepository<BiometricAttempt, Integer> {
    long countByUserAndAttemptType(User user, AttemptType attemptType);
    List<BiometricAttempt> findByUserAndAttemptTypeOrderByIdAsc(User user, AttemptType attemptType);
}
