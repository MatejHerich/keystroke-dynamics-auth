package com.example.appbackend.repository;

import com.example.appbackend.model.VerificationSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationSampleRepository extends JpaRepository<VerificationSample, Long> {
    List<VerificationSample> findByUsernameOrderByIdAsc(String username);
    List<VerificationSample> findByAttemptIdOrderBySampleIndexAsc(Long attemptId);
}
