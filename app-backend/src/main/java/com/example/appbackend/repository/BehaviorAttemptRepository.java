package com.example.appbackend.repository;

import com.example.appbackend.model.BehaviorAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BehaviorAttemptRepository extends JpaRepository<BehaviorAttempt, Long> {
    List<BehaviorAttempt> findByUsernameAndAuthenticatedTrueOrderByCreatedAtDesc(String username);
    List<BehaviorAttempt> findTop20ByUsernameOrderByCreatedAtDesc(String username);
    long countByUsernameAndAttemptType(String username, String attemptType);
}
