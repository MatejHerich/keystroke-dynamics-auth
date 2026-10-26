package com.example.backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "key_presses")
public class KeyPress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private BiometricAttempt attempt;

    @NotNull
    @Min(0)
    @Column(name = "key_index", nullable = false)
    private Integer keyIndex;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "down_ms", nullable = false)
    private Double downMs;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "up_ms", nullable = false)
    private Double upMs;

    @AssertTrue(message = "Kláves nemôže byť uvoľnený skôr, než bol stlačený.")
    public boolean isReleasedAfterPress() {
        return downMs == null || upMs == null || upMs >= downMs;
    }

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
