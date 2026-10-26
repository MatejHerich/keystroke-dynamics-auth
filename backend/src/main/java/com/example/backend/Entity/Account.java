package com.example.backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "IBAN je povinný.")
    @Pattern(regexp = "[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}", message = "IBAN musí mať 15 až 34 znakov bez medzier: kód krajiny, 2 kontrolné číslice a číslo účtu.")
    @Column(name = "iban", nullable = false, unique = true, length = 34)
    private String iban;

    @NotNull
    @DecimalMin(value = "0.00", message = "Zostatok nesmie byť záporný.")
    @Digits(integer = 13, fraction = 2, message = "Zostatok môže mať najviac 13 číslic pred desatinnou čiarkou a 2 za ňou.")
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance =  BigDecimal.ZERO;

    @NotNull
    @Pattern(regexp = "[A-Z]{3}", message = "Mena musí byť trojpísmenový kód, napríklad EUR.")
    @Column(name = "currency", nullable = false, length = 5)
    private String currency = "EUR";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
