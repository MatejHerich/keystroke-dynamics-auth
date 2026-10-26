package com.example.backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private BiometricAttempt attempt;

    @NotBlank(message = "IBAN príjemcu je povinný.")
    @Pattern(regexp = "[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}", message = "IBAN musí mať 15 až 34 znakov bez medzier: kód krajiny, 2 kontrolné číslice a číslo účtu." )
    @Column(name = "recipient_iban", nullable = false, length = 34)
    private String recipientIban;

    @NotNull(message = "Suma je povinná.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Suma musí byť väčšia ako 0.")
    @Digits(integer = 13, fraction = 2, message = "Suma môže mať najviac 13 číslic pred desatinnou čiarkou a 2 za ňou.")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Size(max = 255, message = "Popis môže mať najviac 255 znakov.")
    @Column(name = "description", length = 255)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BiometricAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(BiometricAttempt attempt) {
        this.attempt = attempt;
    }

    public String getRecipientIban() {
        return recipientIban;
    }

    public void setRecipientIban(String recipientIban) {
        this.recipientIban = recipientIban;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
