package com.example.backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_profiles")
public class BiometricProfile {
    @Id
    private Integer id;

    @NotNull
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Min(value = 5, message = "Profil musí vzniknúť aspoň z 5 tréningových vzoriek.")
    @Column(name = "training_count", nullable = false)
    private Integer trainingCount;

    @NotBlank
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "feature_stats", nullable = false)
    private String featureStats;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = LocalDateTime.now();
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
