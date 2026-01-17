package com.example.appbackend;

import jakarta.persistence.*;

@Entity
@Table(name = "biometric_samples")
public class BiometricSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "key_pressed")
    private String keyPressed;

    @Column(name = "dwell_time")
    private Double dwellTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
}
