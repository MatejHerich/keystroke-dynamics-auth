package com.example.appbackend.repository;

import com.example.appbackend.model.BehavioralProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BehavioralProfileRepository extends JpaRepository<BehavioralProfile, Long> {
    BehavioralProfile findByUsername(String username);
}
