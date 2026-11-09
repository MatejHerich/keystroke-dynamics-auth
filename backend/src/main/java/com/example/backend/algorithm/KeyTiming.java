package com.example.backend.algorithm;

public record KeyTiming(double downMs,  double upMs) {
    public KeyTiming{
        if(upMs<downMs){
            throw new IllegalArgumentException("A key cannot be released before it is pressed.");
        }
    }
}
