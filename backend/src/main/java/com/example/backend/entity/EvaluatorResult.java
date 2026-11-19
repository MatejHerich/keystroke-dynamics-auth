package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evaluator_results")
public class EvaluatorResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private BiometricAttempt attempt;

    @Column(name = "evaluator_name", nullable = false, length = 50)
    private String evaluatorName;

    @Column(name = "score", nullable = false)
    private Double score;

    public Integer getId() {
        return id;
    }

    public BiometricAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(BiometricAttempt attempt) {
        this.attempt = attempt;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}