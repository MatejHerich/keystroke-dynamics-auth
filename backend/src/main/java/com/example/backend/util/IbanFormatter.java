package com.example.backend.util;

import org.springframework.stereotype.Component;

@Component
public class IbanFormatter {
    public String format(String iban) {
        if(iban == null){
            return "";
        }
        return iban.replaceAll("(.{4})(?!$)", "$1 ");
    }
}
