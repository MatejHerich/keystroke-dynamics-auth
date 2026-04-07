package com.example.appbackend.service;

import com.example.appbackend.model.Account;
import com.example.appbackend.model.Transaction;
import com.example.appbackend.model.User;
import com.example.appbackend.repository.AccountRepository;
import com.example.appbackend.repository.TransactionRepository;
import com.example.appbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResult executeTransaction(
            String username,
            String recipientIban,
            Double amount,
            String variableSymbol,
            String paymentNote
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Chýba používateľ transakcie.");
        }
        if (recipientIban == null || recipientIban.isBlank()) {
            throw new IllegalArgumentException("IBAN príjemcu je povinný.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Suma musí byť väčšia ako 0.");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Používateľ neexistuje.");
        }

        Account senderAccount = accountRepository.findByUser(user);
        if (senderAccount == null) {
            throw new IllegalArgumentException("Účet odosielateľa neexistuje.");
        }

        String normalizedRecipientIban = normalizeIban(recipientIban);
        String senderIban = normalizeIban(senderAccount.getIban());
        if (normalizedRecipientIban.equals(senderIban)) {
            throw new IllegalArgumentException("Nie je možné odoslať platbu na vlastný účet.");
        }

        if (senderAccount.getBalance() == null || senderAccount.getBalance() < amount) {
            throw new IllegalArgumentException("Nedostatočný zostatok na účte.");
        }

        senderAccount.setBalance(roundToCents(senderAccount.getBalance() - amount));
        accountRepository.save(senderAccount);

        Account recipientAccount = accountRepository.findByIban(normalizedRecipientIban);
        if (recipientAccount != null) {
            Double recipientBalance = recipientAccount.getBalance() == null ? 0.0 : recipientAccount.getBalance();
            recipientAccount.setBalance(roundToCents(recipientBalance + amount));
            accountRepository.save(recipientAccount);
        }

        String description = buildDescription(variableSymbol, paymentNote);

        Transaction transaction = new Transaction();
        transaction.setAccount(senderAccount);
        transaction.setRecipientIban(normalizedRecipientIban);
        transaction.setAmount(roundToCents(amount));
        transaction.setDescription(description);
        transaction.setTransactionType("OUTGOING");

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (recipientAccount != null) {
            Transaction incomingTransaction = new Transaction();
            incomingTransaction.setAccount(recipientAccount);
            incomingTransaction.setRecipientIban(senderIban);
            incomingTransaction.setAmount(roundToCents(amount));
            incomingTransaction.setDescription(description);
            incomingTransaction.setTransactionType("INCOMING");
            transactionRepository.save(incomingTransaction);
        }

        return new TransactionResult(
                savedTransaction.getId(),
                senderAccount.getBalance(),
                "Platba bola úspešne vykonaná."
        );
    }

    private String normalizeIban(String iban) {
        return iban.replace(" ", "").toUpperCase();
    }

    private String buildDescription(String variableSymbol, String paymentNote) {
        String normalizedVariableSymbol = variableSymbol == null ? "" : variableSymbol.trim();
        String normalizedPaymentNote = paymentNote == null ? "" : paymentNote.trim();

        if (!normalizedVariableSymbol.isEmpty() && !normalizedPaymentNote.isEmpty()) {
            return "VS: " + normalizedVariableSymbol + " | " + normalizedPaymentNote;
        }
        if (!normalizedVariableSymbol.isEmpty()) {
            return "VS: " + normalizedVariableSymbol;
        }
        if (!normalizedPaymentNote.isEmpty()) {
            return normalizedPaymentNote;
        }
        return null;
    }

    private Double roundToCents(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record TransactionResult(Long transactionId, Double updatedBalance, String message) {
    }
}
