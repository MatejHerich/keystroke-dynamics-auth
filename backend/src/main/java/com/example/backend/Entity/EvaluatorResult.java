package com.example.backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "evaluator_results")
public class EvaluatorResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private BiometricAttempt attempt;

    @NotBlank
    @Size(max = 50)
    @Column(name = "evaluator_name", nullable = false, length = 50)
    private String evaluatorName;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(name = "score", nullable = false)
    private Double score;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
