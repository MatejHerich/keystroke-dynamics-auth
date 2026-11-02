package com.example.backend.repository;

import com.example.backend.entity.BiometricProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiometricProfileRepository extends JpaRepository<BiometricProfile, Integer> {
}
