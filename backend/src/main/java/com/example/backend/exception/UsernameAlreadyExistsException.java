package com.example.backend.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Pseudonym " + username + " je už obsadený.");
    }
}