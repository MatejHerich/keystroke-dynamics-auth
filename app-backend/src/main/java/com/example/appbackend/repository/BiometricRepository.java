package com.example.appbackend.repository;

import com.example.appbackend.model.BiometricSample;
import com.example.appbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BiometricRepository extends JpaRepository<BiometricSample, Long> {
    List<BiometricSample> findByUserOrderByIdAsc(User user);
}
