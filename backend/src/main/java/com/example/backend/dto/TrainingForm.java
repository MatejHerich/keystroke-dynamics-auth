package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TrainingForm {
    @NotBlank(message = "Zadajte heslo.")
    @Size(max = 64, message = "Heslo môže mať najviac 64 znakov.")
    private String password;

    @NotBlank(message = "Chýbajú časy písania. Napíšte heslo znova.")
    @Size(max = 10000, message = "Časy písania sú neplatné. Napíšte heslo znova.")
    private String keystrokes;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeystrokes() {
        return keystrokes;
    }

    public void setKeystrokes(String keystrokes) {
        this.keystrokes = keystrokes;
    }
}
