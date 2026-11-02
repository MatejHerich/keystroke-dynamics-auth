package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {
    @NotBlank(message = "Pseudonym je povinný.")
    @Size(min = 3, max = 50, message = "Pseudonym musí mať 3 až 50 znakov.")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Pseudonym môže obsahovať len písmená bez diakritiky, číslice, bodku, podčiarkovník a pomlčku.")
    private String username;

    @NotBlank(message = "Heslo je povinné.")
    @Size(min = 8, max = 64, message = "Heslo musí mať 8 až 64 znakov.")
    @Pattern(regexp = "[\\x20-\\x7E]+", message = "Heslo môže obsahovať len písmená bez diakritiky, číslice, bežné znaky a medzeru.")
    private String password;

    @NotBlank(message = "Zopakujte heslo.")
    private String passwordConfirm;

    public boolean passwordsMatch() {
        return password != null && password.equals(passwordConfirm);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
