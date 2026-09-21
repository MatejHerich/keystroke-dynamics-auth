package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "key_presses")
public class KeyPress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private LoginAttempt attempt;

    @Column(nullable = false)
    private int position;

    @Column(name = "down_ms", nullable = false)
    private double downMs;

    @Column(name = "up_ms", nullable = false)
    private double upMs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoginAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(LoginAttempt attempt) {
        this.attempt = attempt;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public double getDownMs() {
        return downMs;
    }

    public void setDownMs(double downMs) {
        this.downMs = downMs;
    }

    public double getUpMs() {
        return upMs;
    }

    public void setUpMs(double upMs) {
        this.upMs = upMs;
    }
}