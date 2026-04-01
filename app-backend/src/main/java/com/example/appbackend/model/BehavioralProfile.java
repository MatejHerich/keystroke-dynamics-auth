package com.example.appbackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "behavioral_profiles")
public class BehavioralProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private Double averageDwellTime;

    @Column(nullable = false)
    private Double averageFlightTime;

    @Column(nullable = false)
    private Double dwellDeviation;

    @Column(nullable = false)
    private Double flightDeviation;

    @Column(nullable = false)
    private Double longPauseRatio;

    @Column(nullable = false)
    private Integer referenceAttempts;

    @Column(nullable = false)
    private Integer referenceSamples;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

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

    public Double getAverageDwellTime() {
        return averageDwellTime;
    }

    public void setAverageDwellTime(Double averageDwellTime) {
        this.averageDwellTime = averageDwellTime;
    }

    public Double getAverageFlightTime() {
        return averageFlightTime;
    }

    public void setAverageFlightTime(Double averageFlightTime) {
        this.averageFlightTime = averageFlightTime;
    }

    public Double getDwellDeviation() {
        return dwellDeviation;
    }

    public void setDwellDeviation(Double dwellDeviation) {
        this.dwellDeviation = dwellDeviation;
    }

    public Double getFlightDeviation() {
        return flightDeviation;
    }

    public void setFlightDeviation(Double flightDeviation) {
        this.flightDeviation = flightDeviation;
    }

    public Double getLongPauseRatio() {
        return longPauseRatio;
    }

    public void setLongPauseRatio(Double longPauseRatio) {
        this.longPauseRatio = longPauseRatio;
    }

    public Integer getReferenceAttempts() {
        return referenceAttempts;
    }

    public void setReferenceAttempts(Integer referenceAttempts) {
        this.referenceAttempts = referenceAttempts;
    }

    public Integer getReferenceSamples() {
        return referenceSamples;
    }

    public void setReferenceSamples(Integer referenceSamples) {
        this.referenceSamples = referenceSamples;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
