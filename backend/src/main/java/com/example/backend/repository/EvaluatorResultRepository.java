package com.example.backend.repository;

import com.example.backend.entity.EvaluatorResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluatorResultRepository extends JpaRepository<EvaluatorResult, Integer> {
}
