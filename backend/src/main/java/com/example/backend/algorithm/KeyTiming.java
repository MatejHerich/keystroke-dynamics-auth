package com.example.backend.algorithm;

public record KeyTiming(double downMs,  double upMs) {
    public KeyTiming{
        if(upMs<downMs){
            throw new IllegalArgumentException("Kláves nemôže byť uvoľnený skôr, než bol stlačený.");
        }
    }
}
