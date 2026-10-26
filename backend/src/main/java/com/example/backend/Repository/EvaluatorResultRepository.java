package com.example.backend.Repository;

import com.example.backend.Entity.EvaluatorResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluatorResultRepository extends JpaRepository<EvaluatorResult, Integer> {
}
