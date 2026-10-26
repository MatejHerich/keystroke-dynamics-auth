package com.example.backend.Repository;

import com.example.backend.Entity.BiometricAttempt;
import com.example.backend.Entity.KeyPress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyPressRepository extends JpaRepository<KeyPress, Integer> {
    List<KeyPress> findByAttemptOrderByKeyIndexAsc(BiometricAttempt attempt);
}
