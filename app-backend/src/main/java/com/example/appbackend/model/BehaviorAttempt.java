package com.example.appbackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "behavior_attempts")
public class BehaviorAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String attemptType;

    @Column(nullable = false)
    private Boolean authenticated;

    @Column(nullable = false)
    private Double confidenceScore;

    @Column(nullable = false)
    private Double requiredThreshold;

    @Column(nullable = false)
    private Integer sampleCount;

    @Column(columnDefinition = "TEXT")
    private String evaluatorDetails;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAttemptType() {
        return attemptType;
    }

    public void setAttemptType(String attemptType) {
        this.attemptType = attemptType;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Double getRequiredThreshold() {
        return requiredThreshold;
    }

    public void setRequiredThreshold(Double requiredThreshold) {
        this.requiredThreshold = requiredThreshold;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getEvaluatorDetails() {
        return evaluatorDetails;
    }

    public void setEvaluatorDetails(String evaluatorDetails) {
        this.evaluatorDetails = evaluatorDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
