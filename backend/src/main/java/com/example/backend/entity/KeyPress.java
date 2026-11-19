package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "key_presses")
public class KeyPress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private BiometricAttempt attempt;

    @Column(name = "key_index", nullable = false)
    private Integer keyIndex;

    @Column(name = "down_ms", nullable = false)
    private Double downMs;

    @Column(name = "up_ms", nullable = false)
    private Double upMs;

    public Integer getId() {
        return id;
    }

    public BiometricAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(BiometricAttempt attempt) {
        this.attempt = attempt;
    }

    public Integer getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(Integer keyIndex) {
        this.keyIndex = keyIndex;
    }

    public Double getDownMs() {
        return downMs;
    }

    public void setDownMs(Double downMs) {
        this.downMs = downMs;
    }

    public Double getUpMs() {
        return upMs;
    }

    public void setUpMs(Double upMs) {
        this.upMs = upMs;
    }
}