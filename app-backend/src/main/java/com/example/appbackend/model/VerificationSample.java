package com.example.appbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "verification_samples")
public class VerificationSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String keyPressed;
    private Double dwellTime;
    private Double flightTime;
    private Integer sampleIndex;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private BehaviorAttempt attempt;

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

    public String getKeyPressed() {
        return keyPressed;
    }

    public void setKeyPressed(String keyPressed) {
        this.keyPressed = keyPressed;
    }

    public Double getDwellTime() {
        return dwellTime;
    }

    public void setDwellTime(Double dwellTime) {
        this.dwellTime = dwellTime;
    }

    public Double getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(Double flightTime) {
        this.flightTime = flightTime;
    }

    public Integer getSampleIndex() {
        return sampleIndex;
    }

    public void setSampleIndex(Integer sampleIndex) {
        this.sampleIndex = sampleIndex;
    }

    public BehaviorAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(BehaviorAttempt attempt) {
        this.attempt = attempt;
    }
}
