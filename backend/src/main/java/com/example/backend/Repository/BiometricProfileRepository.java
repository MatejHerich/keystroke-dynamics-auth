package com.example.backend.Repository;

import com.example.backend.Entity.BiometricProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiometricProfileRepository extends JpaRepository<BiometricProfile, Integer> {
}
