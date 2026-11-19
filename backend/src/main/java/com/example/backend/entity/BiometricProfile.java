package com.example.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_profiles")
public class BiometricProfile {
    @Id
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "training_count", nullable = false)
    private Integer trainingCount;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "feature_stats", nullable = false)
    private String featureStats;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getTrainingCount() {
        return trainingCount;
    }

    public void setTrainingCount(Integer trainingCount) {
        this.trainingCount = trainingCount;
    }

    public String getFeatureStats() {
        return featureStats;
    }

    public void setFeatureStats(String featureStats) {
        this.featureStats = featureStats;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}