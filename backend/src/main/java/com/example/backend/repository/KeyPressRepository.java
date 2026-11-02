package com.example.backend.repository;

import com.example.backend.entity.BiometricAttempt;
import com.example.backend.entity.KeyPress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyPressRepository extends JpaRepository<KeyPress, Integer> {
    List<KeyPress> findByAttemptOrderByKeyIndexAsc(BiometricAttempt attempt);
}
