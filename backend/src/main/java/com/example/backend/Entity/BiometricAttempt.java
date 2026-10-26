package com.example.backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_attempts")
public class BiometricAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "attempt_type", nullable = false, length = 20)
    private AttemptType attemptType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "context", nullable = false, length = 20)
    private AttemptContext attemptContext;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(name = "final_score")
    private Double finalScore;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(name = "threshold")
    private Double threshold;

    @Column(name = "authenticated")
    private Boolean authenticated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @AssertTrue(message = "Tréningový pokus nemá skóre ani rozhodnutie, ostatné pokusy ich musia mať.")
    public boolean isResultConsistentWithType() {
        boolean noResult = finalScore == null && threshold == null && authenticated == null;
        boolean fullResult = finalScore != null && threshold != null && authenticated != null;
        return attemptType == AttemptType.TRAINING ? noResult : fullResult;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AttemptType getAttemptType() {
        return attemptType;
    }

    public void setAttemptType(AttemptType attemptType) {
        this.attemptType = attemptType;
    }

    public AttemptContext getAttemptContext() {
        return attemptContext;
    }

    public void setAttemptContext(AttemptContext attemptContext) {
        this.attemptContext = attemptContext;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
